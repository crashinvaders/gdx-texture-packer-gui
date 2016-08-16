package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.widget.*;
import com.crashinvaders.texturepackergui.utils.CommonUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Iterator;

public class PackDialog extends VisWindow {

    private final VisImageButton btnClose;
    private final VisLabel textOutput;
    private final VisScrollPane textScroller;
    private final VisCheckBox chbAutoClose;

    private CompletionListener completionListener;

    public PackDialog() {
        super("Processing...", "dialog");

        setModal(true);
        setMovable(false);
        setResizable(false);
        setCenterOnAdd(true);

        addCloseButton();
        btnClose = (VisImageButton) getTitleTable().getChildren().peek();
        btnClose.setColor(new Color(0xffffff44));
        btnClose.setDisabled(true);

        textOutput = new VisLabel("", "small") {
            @Override
            protected void sizeChanged() {
                super.sizeChanged();
                // Scroll down scroller
                textScroller.setScrollPercentY(1f);
            }
        };
        textOutput.setAlignment(Align.topLeft);
        textOutput.setWrap(true);

        textScroller = new VisScrollPane(textOutput, "text-output");
        textScroller.setScrollingDisabled(true, false);
        textScroller.setFadeScrollBars(false);
        textScroller.setOverscroll(false, false);
        textScroller.setFlickScroll(false);

        chbAutoClose = new VisCheckBox("Automatically close dialog on success");

        add(textScroller).width(480).height(320).fill().top();
        row().padTop(2f);
        add(chbAutoClose).left();

        pack();
    }

    public void setCompletionListener(CompletionListener completionListener) {
        this.completionListener = completionListener;
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            stage.setScrollFocus(this);
        }
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
                textOutput.setText(textOutput.getText() + stream.toString());
                stream.reset();
            }
        };
        final Timer timer = new Timer();
        timer.scheduleTask(outputUpdateTask, 0f, 0.1f);
        timer.start();

        new Thread(new Runnable() {@Override public void run() {
            for (Iterator<PackModel> iterator = packs.iterator(); iterator.hasNext();) {
                PackModel pack = iterator.next();

                boolean valid = performPacking(pack);
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
                    FocusManager.switchFocus(getStage(), btnClose);

                    closeOnEscape();

                    getTitleLabel().setText("Finished");
                    textOutput.setText(textOutput.getText() + "[output-yellow]Finished. Press [ESC] to close dialog...");

                    //TODO throw event instead
                    if (completionListener != null) {
                        completionListener.onComplete();
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

    public interface CompletionListener {
        void onComplete();
    }
}