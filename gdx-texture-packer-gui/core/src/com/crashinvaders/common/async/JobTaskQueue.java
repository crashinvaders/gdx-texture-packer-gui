package com.crashinvaders.common.async;

import com.badlogic.gdx.utils.Array;

/** Composite JobTask that executes scheduled tasks in a queue one by one. */
public class JobTaskQueue implements JobTask {
    private final Array<JobTask> taskQueue = new Array<>(true, 16);
    private final InternalListener internalListener = new InternalListener();
    private JobTask activeTask = null;
    private JobTask.Listener listener;
    private String threadName;
    private boolean cancelRequested = false;
    private Status status = Status.PENDING;

    public JobTaskQueue() {
    }

    public JobTaskQueue(String threadName) {
        this.threadName = threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void addTask(JobTask task) {
        ensureNotRunning();
        taskQueue.add(task);
    }

    public boolean removeTask(JobTask task) {
        ensureNotRunning();
        return taskQueue.removeValue(task, true);
    }

    /** Total scheduled number of tasks. */
    public int getTaskCount() {
        ensureNotRunning();
        return taskQueue.size;
    }

    public boolean isRunning() {
        return status == Status.RUNNING;
    }

    @Override
    public void setListener(JobTask.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void execute() {
        ensureNotRunning();
        if (taskQueue.size == 0) throw new IllegalStateException("There are no tasks to execute");

        status = Status.RUNNING;
        tryExecuteNext();
    }

    @Override
    public void cancel() {
        if (status != Status.RUNNING) return;

        cancelRequested = true;
        activeTask.cancel();
    }

    @Override
    public Status getStatus() {
        return status;
    }

    private boolean tryExecuteNext() {
        ensureRunning();
        if (activeTask != null) {
            activeTask.setListener(internalListener);
            activeTask = null;
        }
        if (taskQueue.size > 0) {
            activeTask = taskQueue.removeIndex(0);
            activeTask.setListener(internalListener);
            if (threadName != null && activeTask instanceof AsyncJobTask) {
                ((AsyncJobTask) activeTask).setThreadName(threadName);

            }
            activeTask.execute();
            return true;
        }
        return false;
    }

    private void dispose() {
        status = Status.FINISHED;
        cancelRequested = false;
        taskQueue.clear();

        if (activeTask != null) {
            activeTask.setListener(null);
            activeTask = null;
        }
    }

    private void ensureNotRunning() {
        if (status == Status.RUNNING) throw new IllegalStateException("Operation is not permitted due to JobTaskQueue is already running.");
    }

    private void ensureRunning() {
        if (status != Status.RUNNING) throw new IllegalStateException("Operation is not permitted due to JobTaskQueue is not running.");
    }

    private class InternalListener implements JobTask.Listener {
        @Override
        public void onSucceed() {
            // JobTask may be requested for cancel, but not necessary will have a chance to handle it and thus finish as succeeded. So we have to double check cancel request in-between the tasks.
            if (cancelRequested) {
                onCanceled();
                return;
            }

            if (!tryExecuteNext()) {
                dispose();
                if (listener != null) {
                    listener.onSucceed();
                }
            }
        }

        @Override
        public void onFailed(String failMessage, Exception failException) {
            dispose();
            if (listener != null) {
                listener.onFailed(failMessage, failException);
            }
        }

        @Override
        public void onCanceled() {
            dispose();
            if (listener != null) {
                listener.onCanceled();
            }
        }
    }
}
