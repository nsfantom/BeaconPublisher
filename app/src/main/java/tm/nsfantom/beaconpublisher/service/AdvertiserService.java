package tm.nsfantom.beaconpublisher.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;
import tm.nsfantom.beaconpublisher.R;
import tm.nsfantom.beaconpublisher.ui.MainActivity;
import tm.nsfantom.beaconpublisher.util.Constants;
import tm.nsfantom.beaconpublisher.util.PrefStorage;
import tm.nsfantom.beaconpublisher.util.UuidUtil;

/**
 * Manages BLE Advertising independent of the main app.
 * If the app goes off screen (or gets killed completely) advertising can continue because this
 * Service is maintaining the necessary Callback in memory.
 */
public class AdvertiserService extends Service {

    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    /**
     * A global variable to let AdvertiserFragment check if the Service is running without needing
     * to start or bind to it.
     * This is the best practice method as defined here:
     * https://groups.google.com/forum/#!topic/android-developers/jEvXMWgbgzE
     */
    public static boolean running = false;

    public static final String ADVERTISING_FAILED = "tm.nsfantom.beaconpublisher.advertising_failed";

    public static final String ADVERTISING_FAILED_EXTRA_CODE = "failureCode";

    public static final int ADVERTISING_TIMED_OUT = 6;

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private AdvertiseCallback mAdvertiseCallback;

    private Handler mHandler;

    private Runnable timeoutRunnable;

    /**
     * Length of time to allow advertising before automatically shutting off. (10 minutes)
     */
    private long TIMEOUT = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

    PrefStorage prefStorage;

    @Override
    public void onCreate() {
        running = true;
        prefStorage = new PrefStorage(getApplicationContext());
        initialize();
        startAdvertising();
        setTimeout();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        /**
         * Note that onDestroy is not guaranteed to be called quickly or at all. Services exist at
         * the whim of the system, and onDestroy can be delayed or skipped entirely if memory need
         * is critical.
         */
        running = false;
        stopAdvertising();
        mHandler.removeCallbacks(timeoutRunnable);
        stopForeground(true);
        super.onDestroy();
    }

    /**
     * Required for extending service, but this will be a Started Service only, so no need for
     * binding.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Get references to system Bluetooth objects if we don't have them already.
     */
    private void initialize() {
        if (mBluetoothLeAdvertiser == null) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
                if (mBluetoothAdapter != null) {
                    mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
                } else {
                    Toast.makeText(this, getString(R.string.bt_null), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.bt_null), Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * Starts a delayed Runnable that will cause the BLE Advertising to timeout and stop after a
     * set amount of time.
     */
    private void setTimeout() {
        mHandler = new Handler();
        timeoutRunnable = () -> {
            Timber.d("AdvertiserService has reached timeout of " + TIMEOUT + " milliseconds, stopping advertising.");
            sendFailureIntent(ADVERTISING_TIMED_OUT);
            stopSelf();
        };
        mHandler.postDelayed(timeoutRunnable, TIMEOUT);
    }

    /**
     * Starts BLE Advertising.
     */
    private void startAdvertising() {
        goForeground();

        Timber.d("Service: Starting Advertising");

        if (mAdvertiseCallback == null) {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData data = buildAdvertiseData();
            mAdvertiseCallback = new SampleAdvertiseCallback();

            if (mBluetoothLeAdvertiser != null) {
                mBluetoothLeAdvertiser.startAdvertising(settings, data,
                        mAdvertiseCallback);
            }
        }
    }

    /**
     * Move service to the foreground, to avoid execution limits on background processes.
     * <p>
     * Callers should call stopForeground(true) when background work is complete.
     */
    private void goForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification n = new Notification.Builder(this)
                .setContentTitle("Advertising device via Bluetooth")
                .setContentText("This device is discoverable to others nearby.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(FOREGROUND_NOTIFICATION_ID, n);
    }

    /**
     * Stops BLE Advertising.
     */
    private void stopAdvertising() {
        Timber.d("Service: Stopping Advertising");
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertiseCallback = null;
        }
    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name.
     */
    private AdvertiseData buildAdvertiseData() {

        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         *  This includes everything put into AdvertiseData including UUIDs, device info, &
         *  arbitrary service or manufacturer data.
         *  Attempting to send packets over this limit will result in a failure with error code
         *  AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         *  onStartFailure() method of an AdvertiseCallback implementation.
         */

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(Constants.DEVICE_UUID);
        dataBuilder.setIncludeTxPowerLevel(true);
//        dataBuilder.addServiceUuid(Constants.Generic_Access_Service);
//        dataBuilder.addServiceData(Constants.Device_Name, Constants.DeviceName.getBytes());
//        dataBuilder.addServiceData(Constants.Firmware_Revision_String, "001".getBytes());
//        dataBuilder.addServiceData(Constants.Model_Number_String, "00000001".getBytes());
//        dataBuilder.addServiceData(Constants.System_ID, "droid1".getBytes());
//        dataBuilder.addServiceData(Constants.Battery_Level, "5".getBytes());
//
//
//        dataBuilder.addServiceData(Constants.Device_UUID, UuidUtil.asBytes(UUID.fromString("DE7EC7ED-1055-B055-C0DE-DEFEA7EDFA7E")));
//        dataBuilder.addServiceData(Constants.Device_Major, "0001".getBytes());
//        dataBuilder.addServiceData(Constants.Device_Minor, "1000".getBytes());
//        dataBuilder.addServiceData(Constants.TX_Power, new byte[]{(byte) 0xB5});
//        dataBuilder.addServiceUuid(Constants.Service_UUID);
//        dataBuilder.setIncludeDeviceName(true);
//        dataBuilder.addServiceUuid(Constants.Generic_Access_Service);
        ByteBuffer mManufacturerData = ByteBuffer.allocate(24);
        byte[] uuid = UuidUtil.asBytes(UUID.fromString("DE7EC7ED-1055-B055-C0DE-DEFEA7EDFA7E"));
        mManufacturerData.put(0, (byte) 0xBE); // Beacon Identifier
        mManufacturerData.put(1, (byte) 0xAC); // Beacon Identifier
        for (int i = 2; i <= 17; i++) {
            mManufacturerData.put(i, uuid[i - 2]); // adding the UUID
        }
        mManufacturerData.put(18, (byte) 0x00); // first byte of Major
        mManufacturerData.put(19, (byte) 0x09); // second byte of Major
        mManufacturerData.put(20, (byte) 0x00); // first minor
        mManufacturerData.put(21, (byte) 0x06); // second minor
        mManufacturerData.put(22, (byte) 0xB5); // txPower
//        dataBuilder.addManufacturerData(224, mManufacturerData.array()); // using google's company ID
        /* For example - this will cause advertising to fail (exceeds size limit) */
        //String failureData = "asdghkajsghalkxcjhfa;sghtalksjcfhalskfjhasldkjfhdskf";
        //dataBuilder.addServiceData(Constants.Service_UUID, failureData.getBytes());

        return dataBuilder.build();
    }

    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life)
     * and disable the built-in timeout since this code uses its own timeout runnable.
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        settingsBuilder.setTimeout(0);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
        settingsBuilder.setConnectable(false);
        return settingsBuilder.build();
    }

    /**
     * Custom callback after Advertising succeeds or fails to start. Broadcasts the error code
     * in an Intent to be picked up by AdvertiserFragment and stops this Service.
     */
    private class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Timber.d("Advertising failed");
            sendFailureIntent(errorCode);
            stopSelf();

        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Timber.d("Advertising successfully started");
        }
    }

    /**
     * Builds and sends a broadcast intent indicating Advertising has failed. Includes the error
     * code as an extra. This is intended to be picked up by the {@code AdvertiserFragment}.
     */
    private void sendFailureIntent(int errorCode) {
        Intent failureIntent = new Intent();
        failureIntent.setAction(ADVERTISING_FAILED);
        failureIntent.putExtra(ADVERTISING_FAILED_EXTRA_CODE, errorCode);
        sendBroadcast(failureIntent);
    }

//    private static AdvertiseData createAdvData() {
//        final byte[] manufacturerData = createManufactureData();
//        AdvertiseData.Builder builder = new AdvertiseData.Builder()
//                .setIncludeTxPowerLevel(false)
//                .addManufacturerData(APPLE, manufacturerData);
//        //.addServiceUuid(ParcelUuid.fromString("00001802-0000-1000-8000-00805f9b34fb"));
//        return builder.build();
//    }
//
//    private static AdvertiseSettings createAdvSettings() {
//        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder()
//                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
//                .setConnectable(true)
//                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
//        return builder.build();
//    }
//
//    private static byte[] createManufactureData() {
//        ByteBuffer bb = ByteBuffer.allocate(23);
//
//        bb.putShort((short) 0x0215); //iBeacon
//        bb.putLong(uuid.getMostSignificantBits());
//        bb.putLong(uuid.getLeastSignificantBits());
//        bb.putShort((short) 0x0001); //major
//        bb.putShort((short) 0x0001); //minor
//        bb.put((byte) 0xc5); //Tx Power
//
//        return bb.array();
//    }

}
