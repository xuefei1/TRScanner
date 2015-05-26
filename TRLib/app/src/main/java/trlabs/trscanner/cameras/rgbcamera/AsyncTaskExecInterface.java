package trlabs.trscanner.cameras.rgbcamera;

import android.os.AsyncTask;

public interface AsyncTaskExecInterface {
    <T> void execute(AsyncTask<T, ?, ?> task, T... args);

}
