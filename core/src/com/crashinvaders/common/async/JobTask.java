package com.crashinvaders.common.async;

public interface JobTask {
    /** Init task's job. */
    void execute();
    /** Request operation cancellation. JobTask may or may not respond to this and thus calling of this method is not necessary will lead to the actual job cancellation. */
    void cancel();
    /** Actual task's state. */
    Status getStatus();
    /** Setup listener for task. */
    void setListener(Listener listener);

    enum Status {
        /** Operation is prepared to launch, but not started yet. */
        PENDING,
        /** Task is processing its job. */
        RUNNING,
        /** Job is finished and task is no longer usable. */
        FINISHED
    }

    interface Listener {
        void onSucceed();
        void onFailed(String failMessage, Exception failException);
        void onCanceled();
    }
}
