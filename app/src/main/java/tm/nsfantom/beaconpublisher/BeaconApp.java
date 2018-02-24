package tm.nsfantom.beaconpublisher;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by user on 2/23/18.
 */

public class BeaconApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if(BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
    }
}
