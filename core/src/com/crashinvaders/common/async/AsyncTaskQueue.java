package com.crashinvaders.common.async;

import com.badlogic.gdx.utils.Array;

public class AsyncTaskQueue {

    private final Array<AsyncTask> taskQueue = new Array<>(true, 16);
    private final InternalListener internalListener = new InternalListener();
    private final String threadName;
    private AsyncTask activeTask = null;
    private AsyncTask.Listener listener;
    private boolean running = false;
    private boolean cancelRequested = false;

    public AsyncTaskQueue(String threadName) {
        this.threadName = threadName;
    }

    public void setListener(AsyncTask.Listener listener) {
        this.listener = listener;
    }

    public void putTask(AsyncTask task) {
        ensureNotRunning();
        taskQueue.add(task);
    }

    public void execute() {
        ensureNotRunning();
        if (taskQueue.size == 0) throw new IllegalStateException("There are no tasks to execute");

        running = true;
        tryExecuteNext();
    }

    public boolean isRunning() {
        return running;
    }

    public void requestCancel() {
        if (!running) return;

        cancelRequested = true;
        activeTask.cancel();
    }

    private boolean tryExecuteNext() {
        ensureRunning();
        if (activeTask != null) {
            activeTask.removeListener(internalListener);
            activeTask = null;
        }
        if (taskQueue.size > 0) {
            activeTask = taskQueue.removeIndex(0);
            activeTask.addListener(internalListener);
            activeTask.execute(threadName);
            return true;
        }
        return false;
    }

    private void reset() {
        running = false;
        cancelRequested = false;
        taskQueue.clear();

        if (activeTask != null) {
            activeTask.removeListener(internalListener);
            activeTask = null;
        }
    }

    private void ensureNotRunning() {
        if (running) throw new IllegalStateException("Operation is not permitted due to AsyncTaskQueue is already running.");
    }

    private void ensureRunning() {
        if (!running) throw new IllegalStateException("Operation is not permitted due to AsyncTaskQueue is not running.");
    }

    private class InternalListener implements AsyncTask.Listener {
        @Override
        public void onSucceed() {
            // AsyncTask may be requested for cancel, but not necessary will have a chance to handle it and thus finish as succeeded. So we have to double check cancel request in-between the tasks.
            if (cancelRequested) {
                onCanceled();
                return;
            }

            if (!tryExecuteNext()) {
                if (listener != null) {
                    listener.onSucceed();
                }
                reset();
            }
        }

        @Override
        public void onFailed(String failMessage, Exception failException) {
            if (listener != null) {
                listener.onFailed(failMessage, failException);
            }
            reset();
        }

        @Override
        public void onCanceled() {
            if (listener != null) {
                listener.onCanceled();
            }
            reset();
        }
    }
}
