package tm.nsfantom.beaconpublisher.util;

import android.os.ParcelUuid;

public class Constants {

    /**
     * UUID identified with this app - set as Service UUID for BLE Advertisements.
     * <p>
     * Bluetooth requires a certain format for UUIDs associated with Services.
     * The official specification can be found here:
     * {@link https://www.bluetooth.org/en-us/specification/assigned-numbers/service-discovery}
     */
    public static final ParcelUuid Service_UUID = ParcelUuid.fromString("0000b81d-0000-1000-8000-00805f9b34fb");

    public static final String DEVICEUUID = "DE7EC7ED-1055-B055-C0DE-DEFEA7EDFA7E";
    public static final ParcelUuid DEVICE_UUID = ParcelUuid.fromString(DEVICEUUID);

    public static final String DeviceName = "Informu Mu Tag";



    public static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    public static final long SCAN_PERIOD = 10000;

    public static final int ADVERTISE_TIMEOUT = 500;
}
