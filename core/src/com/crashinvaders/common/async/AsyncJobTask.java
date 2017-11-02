package com.crashinvaders.common.async;

import com.badlogic.gdx.Gdx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Inspired by {{@link com.kotcrab.vis.ui.util.async.AsyncTask}}
 */
public abstract class AsyncJobTask implements JobTask {
    private static int treadIndex = 0;

    private Status status = Status.PENDING;
    private Listener listener = null;
    private String threadName = null;

    private volatile boolean cancelRequested = false;
    private boolean canceled = false;
    private boolean failed = false;
    private String failMessage;
    private Exception failException;

    public AsyncJobTask() {
    }

    public AsyncJobTask(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void execute() {
        if (status == Status.RUNNING) throw new IllegalStateException("Task is already running.");
        if (status == Status.FINISHED)
            throw new IllegalStateException("Task has been already executed and can't be reused.");
        status = Status.RUNNING;

        if (threadName == null) {
            threadName = "AsyncJobTask" + treadIndex++;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                executeInBackground();
            }
        }, threadName).start();
    }

    private void executeInBackground() {
        try {
            doInBackground();
        } catch (Exception e) {
            failed(e);
        }

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                status = Status.FINISHED;

                if (listener != null) {
                    if (failed) {
                        listener.onFailed(failMessage, failException);
                    } else if (canceled) {
                        listener.onCanceled();
                    } else {
                        listener.onSucceed();
                    }
                }
            }
        });
    }

    /**
     * Called when this task should execute some action in background. This is always called from non-main thread.
     */
    protected abstract void doInBackground() throws Exception;

    protected void failed(String message) {
        failed(message, new IllegalStateException(message));
    }

    protected void failed(Exception exception) {
        failed(exception.getMessage(), exception);
    }

    protected void failed(final String message, final Exception exception) {
        this.failed = true;
        this.failMessage = message;
        this.failException = exception;
    }

    /**
     * Executes runnable on main GDX thread. This methods blocks until runnable has finished executing. Note that this
     * runnable will also block main render thread.
     */
    protected final void executeOnGdx(final Runnable runnable) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        final AtomicReference<Exception> exceptionAt = new AtomicReference<Exception>();

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    exceptionAt.set(e);
                } finally {
                    latch.countDown();
                }
            }
        });

        try {
            latch.await();

            final Exception e = exceptionAt.get();
            if (e != null) {
                throw e;
            }
        } catch (InterruptedException e) {
            throw e;
        }
    }

    /**
     * Use this method to check if cancel was requested. If it returns true, than you have to stop the job and call return.
     * After this call, task will become canceled, but if you have a reason to ignore cancel request
     * and finish the job, than call {@link #discardCancel()} and continue execution without return.
     */
    protected final boolean checkCanceled() {
        if (cancelRequested) {
            canceled = true;
        }
        return canceled;
    }

    /**
     * @see #checkCanceled()
     */
    protected final void discardCancel() {
        cancelRequested = false;
        canceled = false;
    }

    /**
     * Set cancel request for the task. It doesn't mean that task will be canceled for sure.
     *
     * @see #checkCanceled()
     */
    @Override
    public void cancel() {
        this.cancelRequested = true;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }
}
