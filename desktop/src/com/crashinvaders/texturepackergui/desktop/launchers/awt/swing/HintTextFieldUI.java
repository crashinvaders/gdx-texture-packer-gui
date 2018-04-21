package com.crashinvaders.texturepackergui.desktop.launchers.awt.swing;

import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/** https://stackoverflow.com/a/4962829/3802890 */
public class HintTextFieldUI extends BasicTextFieldUI implements FocusListener {

    private String hint;
    private boolean hideOnFocus;
    private Color color;

    public HintTextFieldUI(String hint) {
        this(hint, false);
    }

    public HintTextFieldUI(String hint, boolean hideOnFocus) {
        this(hint, hideOnFocus, null);
    }

    public HintTextFieldUI(String hint, boolean hideOnFocus, Color color) {
        this.hint = hint;
        this.hideOnFocus = hideOnFocus;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        repaint();
    }

    private void repaint() {
        if (getComponent() != null) {
            getComponent().repaint();
        }
    }

    public boolean isHideOnFocus() {
        return hideOnFocus;
    }

    public void setHideOnFocus(boolean hideOnFocus) {
        this.hideOnFocus = hideOnFocus;
        repaint();
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
        repaint();
    }

    @Override
    protected void paintSafely(Graphics g) {
        super.paintSafely(g);
        JTextComponent comp = getComponent();
        if (hint != null && comp.getText().length() == 0 && (!(hideOnFocus && comp.hasFocus()))) {
            if (color != null) {
                g.setColor(color);
            } else {
                g.setColor(comp.getForeground().brighter().brighter().brighter());
            }
            Insets margin = comp.getMargin();
            int padding = (comp.getHeight() - comp.getFont().getSize()) / 2;
            //TODO Apply vertical margin as well
            g.drawString(hint, margin.left + 2, comp.getHeight() - padding - 1);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (hideOnFocus) repaint();

    }

    @Override
    public void focusLost(FocusEvent e) {
        if (hideOnFocus) repaint();
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        getComponent().addFocusListener(this);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        getComponent().removeFocusListener(this);
    }
}
