package com.kotcrab.vis.ui.widget;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.github.czyzby.kiwi.util.gdx.reflection.Reflection;
import com.kotcrab.vis.ui.VisUI;

public class MenuBarX extends MenuBar {

    private final MenuBarTable mainTable;

    public MenuBarX() {
        this("default");
    }

    public MenuBarX(String styleName) {
        this(VisUI.getSkin().get(styleName, MenuBarStyle.class));
    }

    public MenuBarX(MenuBarStyle style) {
        super(style);

        // Extract menuItems private field value from super class through reflection.
        Table menuItems;
        try {
            menuItems = (Table) Reflection.getFieldValue(
                    ClassReflection.getDeclaredField(MenuBar.class, "menuItems"), this);
        } catch (ReflectionException e) {
            throw new RuntimeException(e);
        }

        mainTable = new MenuBarTable();

        mainTable.left();
        mainTable.add(menuItems);
        mainTable.setBackground(style.background);

        mainTable.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (getCurrentMenu() != null) {
                    if (keycode == Input.Keys.ESCAPE) {
                        closeMenu();
                        mainTable.getStage().setKeyboardFocus(null);
                        return true;
                    }
                }
                return super.keyDown(event, keycode);
            }
        });
    }

    // Replace original mainTable with customized one.
    @Override
    public Table getTable() {
        return mainTable;
    }

    // Make super.getCurrentMenu() available as public.
    @Override
    public Menu getCurrentMenu() {
        return super.getCurrentMenu();
    }

    // Make super.getCurrentMenu() available as public.
    // And set mainTable as a keyboard focus to handle ESC key press.
    @Override
    public void setCurrentMenu(Menu newMenu) {
        super.setCurrentMenu(newMenu);

        Stage stage = mainTable.getStage();
        if (newMenu != null) {
            stage.setKeyboardFocus(mainTable);
        } else {
            stage.setKeyboardFocus(null);
        }
    }

    public class MenuBarTable extends VisTable {
        public MenuBarX getMenuBar() {
            return MenuBarX.this;
        }

        @Override
        protected void sizeChanged () {
            super.sizeChanged();
            closeMenu();
        }
    }
}
