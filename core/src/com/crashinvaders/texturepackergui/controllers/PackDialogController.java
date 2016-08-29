package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.events.PackAtlasUpdatedEvent;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.utils.CommonUtils;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.widget.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Iterator;

@ViewDialog(id = "dialog_packing", value = "lml/dialogPacking.lml")
public class PackDialogController implements ActionContainer {
    private static final String PREF_KEY_AUTO_CLOSE = "auto_close_pack_dialog";

    @Inject InterfaceService interfaceService;
    @Inject EventDispatcher eventDispatcher;

    @ViewStage Stage stage;
    private Preferences prefs;

    @LmlActor("window") VisDialog window;
    @LmlActor("scrOutput") VisScrollPane scrOutput;
    @LmlActor("containerOutput") Container containerOutput;
    @LmlActor("chbAutoClose") VisCheckBox chbAutoClose;
    private VisImageButton btnClose;
    private VisLabel lblOutput;

    @Initiate
    public void initialize() {
        prefs = Gdx.app.getPreferences(AppConstants.PREF_NAME);
    }

    @LmlAfter
    public void initView() {
        btnClose = (VisImageButton) window.getTitleTable().getChildren().peek();
        btnClose.setColor(new Color(0xffffff44));
        btnClose.setDisabled(true);

        lblOutput = new VisLabel("", "small") {
            @Override
            protected void sizeChanged() {
                super.sizeChanged();
                // Scroll down scroller
                scrOutput.setScrollPercentY(1f);
            }
        };
        lblOutput.setAlignment(Align.topLeft);
        lblOutput.setWrap(true);
        containerOutput.setActor(lblOutput);

        stage.setScrollFocus(scrOutput);

        chbAutoClose.setChecked(prefs.getBoolean(PREF_KEY_AUTO_CLOSE, false));
    }

    @LmlAction("onAutoCloseChecked") void onAutoCloseChecked(VisCheckBox chbAutoClose) {
        prefs.putBoolean(PREF_KEY_AUTO_CLOSE, chbAutoClose.isChecked());
        prefs.flush();
    }

    public void launchPack(final PackModel pack) {
        launchPack(Array.with(pack));
    }

    public void launchPack(final Array<PackModel> packs) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stream, true));
        System.setErr(new PrintStream(stream, true));

        final Timer.Task outputUpdateTask = new Timer.Task() {
            @Override
            public void run() {
                lblOutput.setText(lblOutput.getText() + stream.toString());
                stream.reset();
            }
        };
        final Timer timer = new Timer();
        timer.scheduleTask(outputUpdateTask, 0f, 0.1f);
        timer.start();

        new Thread(new Runnable() {@Override public void run() {
            for (Iterator<PackModel> iterator = packs.iterator(); iterator.hasNext();) {
                final PackModel pack = iterator.next();

                boolean valid = performPacking(pack);
                // Notification
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        eventDispatcher.postEvent(new PackAtlasUpdatedEvent(pack));
                    }
                });

                if (!valid) break;

                if (!iterator.hasNext()) {
                    System.out.println("\n");
                }
            }

            // Since timer's thread will be killed, force update output from stream
            Gdx.app.postRunnable(outputUpdateTask);
            IOUtils.closeQuietly(stream);
            timer.clear();

            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));

            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    btnClose.setDisabled(false);
                    btnClose.setColor(Color.WHITE);

                    FocusManager.switchFocus(stage, btnClose);

                    window.closeOnEscape();

                    window.getTitleLabel().setText("Finished");
                    lblOutput.setText(lblOutput.getText() + "[output-yellow]Finished. Press [ESC] to close dialog...");

                    if (chbAutoClose.isChecked()) {
                        window.hide();
                    }
                }
            });

        }}).start();
    }

    /** BEWARE: called in background thread
     * @return false if there was any errors
     */
    private boolean performPacking(PackModel pack) {

        System.out.println("-----------------------------------------------------------");
        System.out.println("-- Starting TexturePacker for pack '" + pack.getName() + "'");
        System.out.println("-----------------------------------------------------------");

        if (!new File(pack.getInputDir()).isDirectory()) {
            System.err.println("[output-red]Input directory is not valid:[] "+pack.getInputDir());
            return false;
        }

        try {
            String filename = pack.getCanonicalFilename();
            TexturePacker.process(pack.getSettings(), pack.getInputDir(), pack.getOutputDir(), filename);
            System.out.println("Done!");
            return true;
        } catch (RuntimeException ex) {
            String message = CommonUtils.fetchMessageStack(ex);
            System.err.println("[output-red]Exception occured:[] " + message);
            return false;
        }
    }
}