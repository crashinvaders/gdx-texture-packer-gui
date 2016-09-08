package com.crashinvaders.texturepackergui.utils.packprocessing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.utils.CommonUtils;
import com.crashinvaders.texturepackergui.utils.ThreadPrintStream;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

//TODO add max threads limitation and queue
public class PackProcessingManager {

    private final Array<PackModel> packModels = new Array<>();
    private final PackProcessor processor;
    private final SyncListener listener;
    private boolean processing;

    public PackProcessingManager(PackProcessor processor, Listener listener) {
        this.processor = processor;
        this.listener = new SyncListener(listener);
    }

    public void postPack(PackModel pack) {
        if (processing) {
            throw new IllegalStateException("PackProcessingManager is in processing stage. Posting not supported");
        }
        packModels.add(pack);
    }

    synchronized public void execute() {
        if (processing) throw new IllegalStateException("Already in processing stage");

        processing = true;
        listener.onProcessingStarted();

        ThreadPrintStream.replaceSystemOut();

        for (final PackModel packModel : packModels) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ThreadPrintStream.setThreadLocalSystemOut(new PrintStream(outputStream));
                    try {
                        listener.onBegin(packModel);
                        processor.processPackage(packModel);
                        listener.onSuccess(packModel, outputStream.toString());
                        packProcessed(packModel);
                    } catch (Exception e) {
                        String message = CommonUtils.fetchMessageStack(e);
                        System.err.println("[output-red]Exception occurred:[] " + message);
                        System.err.println("[output-red]Stack trace:[] ");
                        e.printStackTrace();

                        listener.onError(packModel, outputStream.toString(), e);
                        packProcessed(packModel);
                    } finally {
                        IOUtils.closeQuietly(outputStream);
                    }
                }
            }, "pack-processing-"+packModel.getName()).start();
        }
    }

    private void packProcessed(PackModel packModel) {
        synchronized (packModels) {
            packModels.removeValue(packModel, true);

            if (packModels.size == 0) {
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));

                processing = false;
                listener.onProcessingFinished();
            }
        }
    }

    private static class SyncListener implements Listener {
        private final Listener listener;

        public SyncListener(Listener listener) {
            this.listener = listener;
        }

        @Override
        public void onProcessingStarted() {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    listener.onProcessingStarted();
                }
            });
        }
        @Override
        public void onProcessingFinished() {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    listener.onProcessingFinished();
                }
            });
        }

        @Override
        public void onBegin(final PackModel pack) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    listener.onBegin(pack);
                }
            });
        }
        @Override
        public void onError(final PackModel pack, final String log, final Exception e) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    listener.onError(pack, log, e);
                }
            });
        }
        @Override
        public void onSuccess(final PackModel pack, final String log) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(pack, log);
                }
            });
        }
    }

    public interface Listener {
        void onProcessingStarted();
        void onProcessingFinished();

        void onBegin(PackModel pack);
        void onError(PackModel pack, String log, Exception e);
        void onSuccess(PackModel pack, String log);
    }
}
