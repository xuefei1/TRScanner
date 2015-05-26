package trlabs.trscanner.cameras.rgbcamera;


public final class AsyncTaskExecManager extends PlatformSupportManager<AsyncTaskExecInterface> {

    public AsyncTaskExecManager() {
        super(AsyncTaskExecInterface.class, new DefaultAsyncTaskExecInterface());
    }
}

