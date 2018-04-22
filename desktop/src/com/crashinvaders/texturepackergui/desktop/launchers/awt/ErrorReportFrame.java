package com.crashinvaders.texturepackergui.desktop.launchers.awt;

import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.desktop.launchers.awt.swing.HintTextAreaUI;
import com.crashinvaders.texturepackergui.desktop.launchers.awt.swing.HintTextFieldUI;
import com.esotericsoftware.tablelayout.swing.Table;
import org.apache.commons.io.IOUtils;
import org.lwjgl.Sys;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URLEncoder;

public class ErrorReportFrame extends JDialog {
    private static final String STRING_ENCODING = "UTF-8";
    private static final String PLACEHOLDER_LOG = "$log_placeholder";

    private boolean gitHubLoginHintShown = false;

    public ErrorReportFrame(LwjglCanvasConfiguration config, final Throwable ex) {
        super((Dialog)null);

        try {
            setIconImage(ImageIO.read((ErrorReportFrame.class.getClassLoader().getResourceAsStream(config.iconFilePath))));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Frame layout
        {
            setTitle("Error Report");
            setSize(400, 400);
            setResizable(false);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            final Font fontRegular = getContentPane().getFont().deriveFont(Font.PLAIN);

            final Table rootTable = new Table();
            rootTable.pad(8);

            JLabel lblInstructions = new JLabel("<html>An unexpected error occurred. " +
                    "Please fill out this form to create a <b>GitHub</b> issue about the incident. " +
                    "A stack trace will be included.<html>");
            lblInstructions.setFont(fontRegular);
            lblInstructions.setForeground(Color.darkGray);

            JSeparator separator0 = new JSeparator();
            separator0.setForeground(Color.gray);

            final JTextField txfTitle = new JTextField();
            txfTitle.setUI(new HintTextFieldUI("Brief error description"));
            txfTitle.setMargin(new Insets(0, 4, 0, 4));

            final JTextArea txaComment = new JTextArea();
            txaComment.setUI(new HintTextAreaUI("Crash details / steps to reproduce"));
            txaComment.setLineWrap(true);
            txaComment.setMargin(new Insets(0, 4, 0, 4));

            JScrollPane spComment = new JScrollPane(txaComment);
            spComment.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            JButton btnReport = new JButton("Create Issue");
            btnReport.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createGitHubIssue(txfTitle.getText(), txaComment.getText(), ex);
                }
            });
            JButton btnClose = new JButton("Close");
            btnClose.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ErrorReportFrame.this.dispose();
                }
            });
            Table actionTable = new Table();
            actionTable.addCell(btnReport).padRight(8);
            actionTable.addCell(btnClose);

            JLabel lblGitHubLoginHint = new JLabel("<html><i>In case you're getting response code 500, " +
                    "make sure you're logged in to <b>GitHub</b>.</i><html>");
            lblGitHubLoginHint.setFont(fontRegular);
            lblGitHubLoginHint.setForeground(new Color(0xde7668));

            rootTable.addCell(lblInstructions).expandX().fillX();
            rootTable.row().padTop(8);
            rootTable.addCell(separator0).expandX().fillX();
            rootTable.row().padTop(16);
            rootTable.addCell("Title").left();
            rootTable.row().padTop(4);
            rootTable.addCell(txfTitle).expandX().fillX();
            rootTable.row().padTop(8);
            rootTable.addCell("Comment").left();
            rootTable.row().padTop(4);
            rootTable.addCell(spComment).expand().fill();

            rootTable.row().padTop(8);
            rootTable.addCell(lblGitHubLoginHint).expandX().fillX();

            rootTable.row().padTop(16);
            rootTable.addCell(actionTable).right();

            getContentPane().add(rootTable, BorderLayout.CENTER);
        }
    }

    private void createGitHubIssue(String title, String comment, Throwable ex) {
        try {
            InputStream templateInputStream = ErrorReportFrame.class.getClassLoader().getResourceAsStream("github-error-report-template.md");
            String bodyTemplate = IOUtils.toString(templateInputStream, STRING_ENCODING);

//            if (AppConstants.logFile != null && AppConstants.logFile.exists()) {
//                String log = IOUtils.toString(new FileInputStream(AppConstants.logFile.toString()), STRING_ENCODING).trim();
//                bodyTemplate = bodyTemplate.replace(PLACEHOLDER_LOG, log);
//                comment += bodyTemplate;
//            }

            StringWriter stringWriter = new StringWriter();
            ex.printStackTrace(new PrintWriter(stringWriter));
            String stackTrace = stringWriter.toString().trim();
            bodyTemplate = bodyTemplate.replace(PLACEHOLDER_LOG, stackTrace);
            comment += bodyTemplate;

            String titleEncoded = URLEncoder.encode(title, STRING_ENCODING);
            String commentEncoded = URLEncoder.encode(comment, STRING_ENCODING);
            String requestBody = String.format(
                    "https://github.com/crashinvaders/gdx-texture-packer-gui/issues/new?" +
                            "labels=crash+report&" +
                            "title=[Crash Report] %s&" +
                            "body=%s",
                    titleEncoded,
                    commentEncoded);

            Sys.openURL(requestBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
