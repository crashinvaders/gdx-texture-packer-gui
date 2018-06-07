package com.crashinvaders.texturepackergui.desktop.launchers.awt;

import com.crashinvaders.texturepackergui.desktop.launchers.awt.swing.HintTextAreaUI;
import com.crashinvaders.texturepackergui.desktop.launchers.awt.swing.HintTextFieldUI;
import com.esotericsoftware.tablelayout.swing.Table;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.lwjgl.Sys;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorReportFrame extends JDialog {

    //TODO replace this token with a personalized token of gdx-texture-packer-gui repo admin. This token will be deleted soon.
    private static final String OAUTHTOKEN = "0f74dfb346cf6f021e05f78abd9a4495985c35a1";

    public ErrorReportFrame(LwjglCanvasConfiguration config, final Throwable ex) {
        super((Dialog) null);

        try {
            setIconImage(ImageIO.read((ErrorReportFrame.class.getClassLoader().getResourceAsStream(config.iconFilePath))));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Frame layout
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

        rootTable.row().padTop(16);
        rootTable.addCell(actionTable).right();

        getContentPane().add(rootTable, BorderLayout.CENTER);
    }

    private void createGitHubIssue(String title, String comment, Throwable ex) {
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(OAUTHTOKEN);
        IssueService issueService = new IssueService(client);
        Issue issue = new Issue();
        issue.setTitle("[Crash Report] " + title);

        StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        String stackTrace = stringWriter.toString().trim();
        issue.setBody(comment + "\\n\\n<details><summary>Stack trace</summary>\\n```\\n" + stackTrace + "\\n```");
        try {
            Issue createdIssue = issueService.createIssue("crashinvaders", "gdx-texture-packer-gui", issue);
            Sys.openURL(createdIssue.getHtmlUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
