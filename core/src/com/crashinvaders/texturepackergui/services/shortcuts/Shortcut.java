package com.crashinvaders.texturepackergui.services.shortcuts;


import com.badlogic.gdx.Input;

@SuppressWarnings("PointlessBitwiseExpression")
public class Shortcut {
    private static int FLAG_SHIFT = 1<<0;
    private static int FLAG_CONTROL = 1<<1;
    private static int FLAG_ALT = 1<<2;
    private static int FLAG_SYM = 1<<23;

    private final String actionName;
    private int keyCode;

    /** Represents priority for handling order */
    private int flags = 0;

    public Shortcut(String actionName) {
        this.actionName = actionName;
    }

    Shortcut setKey(int keyCode) {
        if (keyCode == Input.Keys.SHIFT_LEFT || keyCode == Input.Keys.SHIFT_RIGHT) {
            flags ^= FLAG_SHIFT;
            return this;
        }
        if (keyCode == Input.Keys.CONTROL_LEFT || keyCode == Input.Keys.CONTROL_RIGHT) {
            flags ^= FLAG_CONTROL;
            return this;
        }
        if (keyCode == Input.Keys.ALT_LEFT || keyCode == Input.Keys.ALT_RIGHT) {
            flags ^= FLAG_ALT;
            return this;
        }
        if (keyCode == Input.Keys.SYM) {
            flags ^= FLAG_SYM;
            return this;
        }

        this.keyCode = keyCode;
        return this;
    }

    public String getActionName() {
        return actionName;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isShift() {
        return (flags & FLAG_SHIFT) != 0;
    }
    public boolean isControl() {
        return (flags & FLAG_CONTROL) != 0;
    }
    public boolean isAlt() {
        return (flags & FLAG_ALT) != 0;
    }
    public boolean isSym() {
        return (flags & FLAG_SYM) != 0;
    }

    public int getPriority() {
        int i = this.flags;
        // Bit count algorithm from here http://stackoverflow.com/a/109025/3802890
        i = i - ((i >> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
        return (((i + (i >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
    }
}
