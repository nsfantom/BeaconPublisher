package tm.nsfantom.beaconpublisher.service;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;

/**
 * Created by user on 2/24/18.
 */

public class InformuMuTagProfile {

    public static final ParcelUuid GENERIC_ACCESS_SERVICE = ParcelUuid.fromString("00001800-0000-1000-8000-00805F9B34FB");
    // Expected Value: Informu Mu Tag (14 bytes)
    public static final ParcelUuid DEVICE_NAME_UUID = ParcelUuid.fromString("00002A00-0000-1000-8000-00805F9B34FB");
    // Expected Value: (  bytes)
    public static final ParcelUuid APPEARANCE_UUID = ParcelUuid.fromString("00002A01-0000-1000-8000-00805F9B34FB");

    /**
     * Mu Tag Device Info Service
     */

    // Expected Value:(11bytes)
    public static final ParcelUuid MANUFACTURER_NAME_UUID = ParcelUuid.fromString("00002A29-0000-1000-8000-00805F9B34FB");

    // Expected Value:(3bytes)
    public static final ParcelUuid FIRMWARE_REVISION_STRING_UUID = ParcelUuid.fromString("00002A26-0000-1000-8000-00805F9B34FB");

    // Expected Value:(8bytes)
    public static final ParcelUuid MODEL_NUMBER_STRING_UUID = ParcelUuid.fromString("00002A24-0000-1000-8000-00805F9B34FB");


    // Expected Value: (6 bytes)
    public static final ParcelUuid SYSTEM_ID_UUID = ParcelUuid.fromString("00002A23-0000-1000-8000-00805F9B34FB");

    //Expected Value: (1  bytes)
    public static final ParcelUuid BATTERY_LEVEL_UUID = ParcelUuid.fromString("00002A19-0000-1000-8000-00805F9B34FB");

    /**
     * Mu Tag Configuration Service
     */
    public static final ParcelUuid MU_TAG_CONFIGURATION_SERVICE = ParcelUuid.fromString("a173424a-9708-4c4c-aeed-0ab1af539797");
    //    Permission: RW
    //    Expected Value: (16+2 bytes)
    public static final ParcelUuid DEVICE_UUID = ParcelUuid.fromString("ac9b44ea-aa5e-40f4-888a-c2637573ab01");

    //    Permission: RW
    //    Expected Value: (2+2 bytes)
    public static final ParcelUuid DEVICE_MAJOR_UUID = ParcelUuid.fromString("ac9b44ea-aa5e-40f4-888a-c2637573ab02");

    //    Permission: RW
    //    Expected Value: (2+2 bytes)
    public static final ParcelUuid DEVICE_MINOR_UUID = ParcelUuid.fromString("ac9b44ea-aa5e-40f4-888a-c2637573ab03");

    //    Permission: RW
    //    Expected Value: (1+2 bytes)
    public static final ParcelUuid TX_POWER_UUID = ParcelUuid.fromString("ac9b44ea-aa5e-40f4-888a-c2637573ab04");

    //    Permission: W (without response)
    //    Expected Value: (1 byte)
    public static final ParcelUuid AUTHENTICATE_CONNECTION_UUID = ParcelUuid.fromString("ac9b44ea-aa5e-40f4-888a-c2637573ab05");

    //    Permission; RW
    //    Expected Value: (1+2 bytes)
    public static final ParcelUuid TAG_COLOR_UUID = ParcelUuid.fromString("ac9b44ea-aa5e-40f4-888a-c2637573ab06");

    //    Permission; W
    //    Expected Value: (1+2 bytes)
    public static final ParcelUuid DEEP_SLEEP_UUID = ParcelUuid.fromString("ac9b44ea-aa5e-40f4-888a-c2637573ab07");

    //    Permission; RW
    //    Expected Value: (1+2 bytes)
    public static final ParcelUuid PROVISIONED_UUID = ParcelUuid.fromString("ac9b44ea-aa5e-40f4-888a-c2637573ab08");

    //    Permission; RW
    //    Expected Value: (1+2 bytes)
    public static final ParcelUuid ADVERTISING_INTERVAL_UUID = ParcelUuid.fromString("ac9b44ea-aa5e-40f4-888a-c2637573ab09");


    public static final ParcelUuid SILICON_LABS_OTA = ParcelUuid.fromString("1D14D6EE-FD63-4FA1-BFA4-8F47B42119F0");

    /**
     * Return a configured {@link BluetoothGattService} instance for the
     * Current Time Service.
     */
    public static BluetoothGattService createConfigurationService() {
        BluetoothGattService service = new BluetoothGattService(MU_TAG_CONFIGURATION_SERVICE.getUuid(), BluetoothGattService.SERVICE_TYPE_SECONDARY);

        BluetoothGattCharacteristic gcDU = new BluetoothGattCharacteristic(DEVICE_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        BluetoothGattCharacteristic gcMajor = new BluetoothGattCharacteristic(DEVICE_MAJOR_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        BluetoothGattCharacteristic gcMinor = new BluetoothGattCharacteristic(DEVICE_MINOR_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        BluetoothGattCharacteristic gcTxPower = new BluetoothGattCharacteristic(TX_POWER_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        BluetoothGattCharacteristic gcAuthConn = new BluetoothGattCharacteristic(AUTHENTICATE_CONNECTION_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        BluetoothGattCharacteristic gcTagColor = new BluetoothGattCharacteristic(TAG_COLOR_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        BluetoothGattCharacteristic gcDeepSleep = new BluetoothGattCharacteristic(DEEP_SLEEP_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        BluetoothGattCharacteristic gcProvisioned = new BluetoothGattCharacteristic(PROVISIONED_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        BluetoothGattCharacteristic gcAdvertisingInterval = new BluetoothGattCharacteristic(ADVERTISING_INTERVAL_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE
        );

        service.addCharacteristic(gcDU);
        service.addCharacteristic(gcMajor);
        service.addCharacteristic(gcMinor);
        service.addCharacteristic(gcTxPower);
        service.addCharacteristic(gcAuthConn);
        service.addCharacteristic(gcTagColor);
        service.addCharacteristic(gcDeepSleep);
        service.addCharacteristic(gcProvisioned);
        service.addCharacteristic(gcAdvertisingInterval);
        return service;
    }

    public static BluetoothGattService createInformuGenericAccessService() {
        BluetoothGattService service = new BluetoothGattService(GENERIC_ACCESS_SERVICE.getUuid(), BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic gcDN = new BluetoothGattCharacteristic(DEVICE_NAME_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
        BluetoothGattCharacteristic gcAppearance = new BluetoothGattCharacteristic(APPEARANCE_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
        BluetoothGattCharacteristic gcManufactureName = new BluetoothGattCharacteristic(MANUFACTURER_NAME_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        );

        BluetoothGattCharacteristic gcFirm = new BluetoothGattCharacteristic(FIRMWARE_REVISION_STRING_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
        BluetoothGattCharacteristic gcModel = new BluetoothGattCharacteristic(MODEL_NUMBER_STRING_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
        BluetoothGattCharacteristic gcSystemId= new BluetoothGattCharacteristic(SYSTEM_ID_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        );
        BluetoothGattCharacteristic gcBattery = new BluetoothGattCharacteristic(BATTERY_LEVEL_UUID.getUuid(),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ
        );

        service.addCharacteristic(gcDN);
        service.addCharacteristic(gcAppearance);
        service.addCharacteristic(gcManufactureName);
        service.addCharacteristic(gcFirm);
        service.addCharacteristic(gcModel);
        service.addCharacteristic(gcSystemId);
        service.addCharacteristic(gcBattery);
        return service;
    }

    public static BluetoothGattService createOTAService() {
        return new BluetoothGattService(SILICON_LABS_OTA.getUuid(), BluetoothGattService.SERVICE_TYPE_SECONDARY);
    }



}
