package com.crashinvaders.texturepackergui.desktop.launchers.awt;

import com.esotericsoftware.tablelayout.swing.Table;
import org.eclipse.egit.github.core.client.GitHubClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GithubCredentialsDialog extends JDialog {

    private boolean canceled;
    private String userName;
    private String password;

    GithubCredentialsDialog() {
        super((Dialog)null);

        setTitle("Provide Github Credentials.");
        setSize(400, 400);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final Font fontRegular = getContentPane().getFont().deriveFont(Font.PLAIN);

        final Table rootTable = new Table();
        rootTable.pad(8);
        JLabel userNameLabel = new JLabel("Username: ");
        JLabel passwordLabel = new JLabel("Passowrd: ");

        JTextField userNameTextField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userName = userNameTextField.getText();
                char[] password = passwordField.getPassword();
                GithubCredentialsDialog.this.password = new String(password);
                //clearing password for security reasons as mentiond in JPasswordField.getPassword()
                for (char p : password) {
                    p = 0;
                }
                setVisible(false);
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userName = null;
                password = null;
                setVisible(false);
            }
        });

    }

    public String getUsername() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isCanceled() {
        return userName == null && password == null;
    }
}
