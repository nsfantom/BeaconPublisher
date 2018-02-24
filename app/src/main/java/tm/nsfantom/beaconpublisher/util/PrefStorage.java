package tm.nsfantom.beaconpublisher.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class PrefStorage {
    private final String DEVICE_MAJOR = "device_major";
    private final String DEVICE_MINOR = "device_minor";
    private final String DEVICE_NAME = "device_name";
    private final String DEVICE_BATTERY = "battery_level";


    private SharedPreferences sharedPreferences;
    private Context context;

    public PrefStorage(Context context) {
        this.context = context;
    }

    public void saveMinor(int minor) {
        getPrefs().edit()
                .putInt(DEVICE_MINOR, minor)
                .apply();
    }

    public void saveMajor(int major) {
        getPrefs().edit()
                .putInt(DEVICE_MAJOR, major)
                .apply();
    }

    public void saveDeviceName(String deviceName) {
        getPrefs().edit()
                .putString(DEVICE_NAME, deviceName)
                .apply();
    }

    public void saveBatteryLevel(int level){
        getPrefs().edit()
                .putInt(DEVICE_BATTERY, level)
                .apply();
    }

    public int getMajor() {
        return getPrefs().getInt(DEVICE_MAJOR, 0);
    }

    public int getMinor() {
        return getPrefs().getInt(DEVICE_MINOR, 0);
    }

    public String getDeviceName(){return getPrefs().getString(DEVICE_NAME,"Informu Mu Tag");}

    SharedPreferences getPrefs() {
        if (sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PrefStorage.class.getSimpleName(), Context.MODE_PRIVATE);
        return sharedPreferences;
    }
}
