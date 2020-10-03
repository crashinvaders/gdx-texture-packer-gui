package com.crashinvaders.texturepackergui.desktop.launchers.awt.errorreport;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.desktop.launchers.awt.LwjglCanvasConfiguration;
import com.esotericsoftware.tablelayout.swing.Table;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;

public class ErrorReportFrame extends JDialog {
    private static final String STRING_ENCODING = "UTF-8";
    private static final String PLACEHOLDER_LOG = "$log_placeholder";

    public ErrorReportFrame(LwjglCanvasConfiguration config, final Throwable ex) {
        super((Dialog)null);

        // Set icon.
        try {
            if (config.iconFilePath != null) {
                setIconImage(ImageIO.read((ErrorReportFrame.class.getClassLoader()
                        .getResourceAsStream(config.iconFilePath))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String applicationLog = retrieveAppLog();
        final String markdownLog = prepareMarkdownLog(applicationLog);
        copyTextToClipboard(markdownLog);


        // Frame layout
        {
            setTitle("Error Report");
            setSize(400, 140);
            setResizable(false);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            final Font fontRegular = getContentPane().getFont().deriveFont(Font.PLAIN);

            JLabel lblInstructions = new JLabel("<html>Oops, a fatal error occurred!<br/>" +
                    "The crash log is copied to the clipboard. " +
                    "Please press <i>\"Report Crash\"</i> button to create a <b>GitHub</b> issue and paste the log in there.");
            lblInstructions.setFont(fontRegular);
            lblInstructions.setForeground(Color.darkGray);

            JSeparator separator0 = new JSeparator();
            separator0.setForeground(Color.gray);

            JTextArea txaAppLog = new JTextArea();
            txaAppLog.setText(applicationLog);
            txaAppLog.setLineWrap(false);
            txaAppLog.setMargin(new Insets(0, 4, 4, 4));
            txaAppLog.setCaretPosition(0);
            txaAppLog.setEditable(false);
            txaAppLog.setForeground(Color.GRAY);
            final JScrollPane spAppLog = new JScrollPane(txaAppLog);
            spAppLog.setVisible(false);

            JButton btnReport = new JButton("Report Crash");
            btnReport.addActionListener(e -> createGithubIssueWithBrowser());

            final JButton btnCopyLog = new JButton("Copy Log");
            btnCopyLog.setVisible(false);
            btnCopyLog.addActionListener(e -> copyTextToClipboard(markdownLog));

            JButton btnViewLog = new JButton("Show Log");
            btnViewLog.addActionListener(e -> {
                spAppLog.setVisible(true);
                btnCopyLog.setVisible(true);
                btnViewLog.setVisible(false);
                setSize(getWidth() + 160, getHeight() + 480);
            });

            Table actionTable = new Table();
            actionTable.addCell(btnViewLog).expandX().left();
            actionTable.addCell(btnCopyLog).padRight(8);
            actionTable.addCell(btnReport);

            Table rootTable = new Table();
            rootTable.pad(8);
            rootTable.addCell(lblInstructions).expandX().fillX();
            rootTable.row().padTop(8);
            rootTable.addCell(separator0).expandX().fillX();
            rootTable.row().padTop(8);
            rootTable.addCell("Application Log").left();
            rootTable.row().padTop(4);
            rootTable.addCell(spAppLog).expand().fill();

            rootTable.row().padTop(16);
            rootTable.addCell(actionTable).expandX().fillX();

            getContentPane().add(rootTable, BorderLayout.CENTER);
        }
    }

    private static void openLogFile() {
        if (AppConstants.logFile != null && AppConstants.logFile.exists()) {
            try {
                Desktop.getDesktop().open(AppConstants.logFile.getAbsoluteFile());
            } catch (IOException ignore) { }
        }
    }

    private static String retrieveAppLog() {
        try {

            if (AppConstants.logFile != null && AppConstants.logFile.exists()) {
                return IOUtils.toString(new FileInputStream(AppConstants.logFile.toString()), STRING_ENCODING).trim();
            } else {
                return System.err.toString();
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString().trim();
            System.err.println(stackTrace);
            return System.err.toString();
        }
    }

    private static String prepareMarkdownLog(String plainLog) {
        return "<details><summary>Application Log</summary>\n\n```\n" +
                plainLog +
                "\n```\n</details>";
    }

    private static void copyTextToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    private static void createGithubIssueWithBrowser() {
        Gdx.app.getNet().openURI("https://github.com/crashinvaders/gdx-texture-packer-gui/issues/new?template=crash-report.md&title=Crash+report");
    }
}
