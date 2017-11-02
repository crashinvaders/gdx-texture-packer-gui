package com.crashinvaders.common.async;

/** Simple synchronized job task. Override {@link #performJob()} with actual processing. */
public abstract class SyncJobTask implements JobTask {

    private Status status = Status.PENDING;
    private Listener listener;

    private boolean failed = false;
    private String failMessage = null;
    private Exception failException = null;

    @Override
    public void execute() {
        try {
            performJob();
        } catch (Exception e) {
            failed(e.getMessage(), e);
        }

        if (listener != null) {
            if (failed) {
                listener.onFailed(failMessage, failException);
            } else {
                listener.onSucceed();
            }
        }
    }

    @Override
    public void cancel() {
        // Is not supported due to single thread nature of this job
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

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

    /** Override this method to do the actual job. */
    protected abstract void performJob() throws Exception;
}
