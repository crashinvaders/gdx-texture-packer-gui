package com.crashinvaders.texturepackergui.utils.packprocessing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.utils.CommonUtils;
import com.crashinvaders.texturepackergui.utils.ThreadPrintStream;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PackProcessingManager {

    private final Array<PackProcessingNode> processingNodes = new Array<>();
    private final PackProcessor processor;
    private final SyncListener listener;
    private final ExecutorService executorService;

    private boolean processing;
    private final AtomicInteger processedCount = new AtomicInteger();

    private PrintStream origStdOut;
    private PrintStream origStdErr;

    public PackProcessingManager(PackProcessor processor, Listener listener) {
        this.processor = processor;
        this.listener = new SyncListener(listener);
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public void postProcessingNode(PackProcessingNode node) {
        if (processing) {
            throw new IllegalStateException("PackProcessingManager is in processing stage. Posting is prohibited");
        }
        processingNodes.add(node);
    }

    synchronized public void execute(final ProjectModel projectModel) {
        if (processing) throw new IllegalStateException("Already in processing stage");

        processing = true;
        listener.onProcessingStarted();

        origStdOut = System.out;
        origStdErr = System.err;
        ThreadPrintStream.replaceSystemOut();

        for (final PackProcessingNode processingNode : processingNodes) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ThreadPrintStream.setThreadLocalSystemOut(new PrintStream(outputStream));
                    try {
                        listener.onBegin(processingNode);
                        processor.processPackage(processingNode);
                        processingNode.setLog(outputStream.toString());
                        listener.onSuccess(processingNode);
                    } catch (Exception e) {
                        String message = CommonUtils.fetchMessageStack(e);
                        System.err.println("[text-red]Exception occurred:[] " + message);
                        System.err.println("[text-red]Stack trace:[] ");
                        e.printStackTrace();
                        processingNode.setLog(outputStream.toString());
                        listener.onError(processingNode, e);
                    } finally {
                        nodeProcessed(processingNode);
                        IOUtils.closeQuietly(outputStream);
                    }
                }
            });
        }
        executorService.shutdown();
    }

    private synchronized void nodeProcessed(PackProcessingNode node) {
        // Check if all nodes have been processed
        if (processedCount.addAndGet(1) == processingNodes.size) {
            finishProcessing();
        }
    }

    /** Get called when all nodes are processed */
    private void finishProcessing() {
        // Restore original system output streams
        System.setOut(origStdOut);
        System.setErr(origStdErr);
        // Print out each log to the output
        for (PackProcessingNode node : processingNodes) {
            System.out.println(node.getLog());
        }
        processingNodes.clear();
        processedCount.set(0);
        processing = false;
        listener.onProcessingFinished();
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
        public void onBegin(final PackProcessingNode node) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    listener.onBegin(node);
                }
            });
        }
        @Override
        public void onError(final PackProcessingNode node, final Exception e) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    listener.onError(node, e);
                }
            });
        }
        @Override
        public void onSuccess(final PackProcessingNode node) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(node);
                }
            });
        }
    }

    public interface Listener {
        void onProcessingStarted();
        void onProcessingFinished();

        void onBegin(PackProcessingNode node);
        void onSuccess(PackProcessingNode node);
        void onError(PackProcessingNode node, Exception e);
    }
}
