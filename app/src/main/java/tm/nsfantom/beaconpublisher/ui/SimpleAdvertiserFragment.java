/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tm.nsfantom.beaconpublisher.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;
import tm.nsfantom.beaconpublisher.R;
import tm.nsfantom.beaconpublisher.databinding.FragmentSimpleadvertiserBinding;
import tm.nsfantom.beaconpublisher.service.InformuMuTagProfile;
import tm.nsfantom.beaconpublisher.util.Constants;
import tm.nsfantom.beaconpublisher.util.PrefStorage;

/**
 * Allows user to start & stop Bluetooth LE Advertising of their device.
 */

public class SimpleAdvertiserFragment extends Fragment implements TextView.OnEditorActionListener {

    /**
     * Listens for notifications that the {@code AdvertiserService} has failed to start advertising.
     * This Receiver deals with Fragment UI elements and only needs to be active when the Fragment
     * is on-screen, so it's defined and registered in code instead of the Manifest.
     */
    private FragmentSimpleadvertiserBinding layout;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bTAdvertiser;
    private AdvertiseCallback advCallback;
    private BluetoothGattServer gattServer;
    private BluetoothGattServerCallback gattCallback;
    private boolean isAdvertised = false;
    private List<BluetoothDevice> managedDevices = new ArrayList<BluetoothDevice>();

    static final int APPLE = 0x004c;
    static final UUID uuid = UUID.fromString(Constants.DEVICEUUID);

    static int minor = 1;
    static int major = 1;

    private PrefStorage prefStorage;


    public static SimpleAdvertiserFragment newInstance(BluetoothAdapter bluetoothAdapter) {

        SimpleAdvertiserFragment advertiserFragment = new SimpleAdvertiserFragment();
        advertiserFragment.bluetoothAdapter = bluetoothAdapter;
        return advertiserFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_simpleadvertiser, container, false);
        init();
        prefStorage = new PrefStorage(getContext());
        return layout.getRoot();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layout.switchAdvertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startAdvertise();
                startGattServer();
            } else {
                stopGattServer();
                stopAdvertise();
            }
        });
        layout.etMajor.setText(String.valueOf(prefStorage.getMajor()));
        major = prefStorage.getMajor();
        layout.etMinor.setText(String.valueOf(prefStorage.getMinor()));
        minor = prefStorage.getMinor();
        layout.etMajor.setOnEditorActionListener(this);
        layout.etMinor.setOnEditorActionListener(this);
        layout.etDeviceName.setText(prefStorage.getDeviceName());
        layout.etDeviceName.setOnEditorActionListener(this);
    }

    private void init() {

        advCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                if (settingsInEffect != null) {
                    appendStatus(settingsInEffect.toString());
                } else {
                    appendStatus("onStartSuccess: settingInEffect = null");
                }
            }

            @Override
            public void onStartFailure(int errorCode) {
                appendStatus("onStartFailure: errorCode = " + errorCode);
            }
        };

        gattCallback = new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                Timber.d("onConnectionStateChange: " + device.getName() + " status=" + status + "->" + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    if (!managedDevices.contains(device) && (!device.getAddress().equals(bluetoothAdapter.getAddress()))) {
                        managedDevices.add(device);
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    managedDevices.remove(device);
                }

            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    appendStatus("onServiceAdded: status=GATT_SUCCESS service=" + service.getUuid().toString());
                    Timber.d("onServiceAdded: status=GATT_SUCCESS service=%s", service.getUuid().toString());
                } else {
                    appendStatus("onServiceAdded: status!=GATT_SUCCESS");
                    Timber.d("onServiceAdded: status!=GATT_SUCCESS");
                }

            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                Timber.d("onCharacteristicReadRequest: requestId=" + requestId + " offset=" + offset);
                Timber.d("uuid: %s", characteristic.getUuid());
                if (characteristic.getUuid().equals(InformuMuTagProfile.DEVICE_NAME_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue(layout.etDeviceName.getText().toString());
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(InformuMuTagProfile.DEVICE_MAJOR_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue(layout.etMajor.getText().toString());
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(InformuMuTagProfile.DEVICE_MINOR_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue(layout.etMinor.getText().toString());
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                } else if (characteristic.getUuid().equals(InformuMuTagProfile.TAG_COLOR_UUID.getUuid())) {
                    Timber.d("%s is reading characteristic device name", device.getName());
                    characteristic.setValue("1");
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                }
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                Timber.d("onCharacteristicWriteRequest: requestId=" + requestId + " preparedWrite="
                        + Boolean.toString(preparedWrite) + " responseNeeded="
                        + Boolean.toString(responseNeeded) + " offset=" + offset);
                if(characteristic.getUuid().equals(InformuMuTagProfile.DEVICE_MAJOR_UUID.getUuid())){
                    Timber.d("%s is writing characteristic", device.getName());
                    if (value != null && value.length > 0) {
                        String str = new String(value);
                        Timber.d("data: %s", str);
                        getActivity().runOnUiThread(()-> layout.etMajor.setText(str));
                        appendStatus(str);
                    } else {
                        Timber.d("Invalid value.");
                    }
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
                } else if (characteristic.getUuid().equals(InformuMuTagProfile.DEVICE_MINOR_UUID.getUuid())){
                    Timber.d("%s is writing characteristic", device.getName());
                    if (value != null && value.length > 0) {
                        String str = new String(value);
                        Timber.d("data: %s", str);
                        getActivity().runOnUiThread(()-> layout.etMinor.setText(str));
                        appendStatus(str);
                    } else {
                        Timber.d("Invalid value.");
                    }
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
                }
            }

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                Timber.d("onDescriptorReadRequest: ");
            }

            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                Timber.d("onDescriptorWriteRequest: ");
            }

            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                Timber.d("onExecuteWrite: ");
            }

            @Override
            public void onNotificationSent(BluetoothDevice device, int status) {
                Timber.d("onNotificationSent: %s", device.getName());
            }
        };

        Timber.d(getString(R.string.ble_initialized));
    }

    @Override
    public void onResume() {
        super.onResume();
        layout.llControls.setKeepScreenOn(true);
    }

    /**
     * When app goes off screen, unregister the Advertising failure Receiver to stop memory leaks.
     * (and because the app doesn't care if Advertising fails while the UI isn't active)
     */
    @Override
    public void onPause() {
        super.onPause();
        layout.llControls.setKeepScreenOn(false);
    }

    private void startAdvertise() {
        if (bluetoothAdapter != null && !isAdvertised) {
            if (bTAdvertiser == null) {
                bTAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            }
            bTAdvertiser.startAdvertising(createAdvSettings(), createAdvData(), advCallback);
            appendStatus(getString(R.string.ble_start_adv));
        }
    }

    private void stopAdvertise() {
        if (bTAdvertiser != null) {
            bTAdvertiser.stopAdvertising(advCallback);
            isAdvertised = false;
            bTAdvertiser = null;
            appendStatus(getString(R.string.ble_stop_adv));
        }
    }

    /**
     * get BluetoothManager
     */
    public BluetoothManager getBTManager() {
        return (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
    }

    private void startGattServer() {
        gattServer = getBTManager().openGattServer(getContext(), gattCallback);
        gattServer.addService(InformuMuTagProfile.createInformuGenericAccessService());
        gattServer.addService(InformuMuTagProfile.createConfigurationService());
    }

    private void stopGattServer() {
        if (gattServer != null) {
            gattServer.clearServices();
            gattServer.close();
            gattServer = null;
            appendStatus(getString(R.string.stop_gatt_server));
        }
    }

    public void notifyCharacteristicChanged() {
        if (managedDevices.isEmpty()) return;
        BluetoothGattService service = gattServer.getService(InformuMuTagProfile.GENERIC_ACCESS_SERVICE.getUuid());
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(InformuMuTagProfile.DEVICE_NAME_UUID.getUuid());
        characteristic.setValue(layout.etDeviceName.getText().toString());


        for (BluetoothDevice device : managedDevices) {
            Timber.d("Going to notify to %s", device.getName());
            gattServer.notifyCharacteristicChanged(device, characteristic, false);
        }
    }

    private static AdvertiseData createAdvData() {
        final byte[] manufacturerData = createManufactureData();
        AdvertiseData.Builder builder = new AdvertiseData.Builder()
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(APPLE, manufacturerData);
        //.addServiceUuid(ParcelUuid.fromString("00001802-0000-1000-8000-00805f9b34fb"));
        return builder.build();
    }

    private static AdvertiseSettings createAdvSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder()
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        return builder.build();
    }

    private static byte[] createManufactureData() {
        ByteBuffer bb = ByteBuffer.allocate(23);

        bb.putShort((short) 0x0215); //iBeacon
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
//        bb.putShort((short) 0x0001); //major
//        bb.putShort((short) 0x0001); //minor
        bb.putShort((short) major); //major
        bb.putShort((short) minor); //minor
        bb.put((byte) 0xc5); //Tx Power

        return bb.array();
    }

    public void appendStatus(final String status) {
        getActivity().runOnUiThread(() -> {
            String current = layout.tvLogger.getText().toString();
            layout.tvLogger.setText(current + "\n" + status);
        });
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            switch (v.getId()) {
                case R.id.etMajor:
                    prefStorage.saveMajor(Integer.parseInt(v.getText().toString()));
                    major = Integer.parseInt(v.getText().toString());
                    break;
                case R.id.etMinor:
                    prefStorage.saveMinor(Integer.parseInt(v.getText().toString()));
                    minor = Integer.parseInt(v.getText().toString());
                    break;
                case R.id.etDeviceName:
                    prefStorage.saveDeviceName(layout.etDeviceName.getText().toString());
//                    notifyCharacteristicChanged();
                    break;

            }
            notifyCharacteristicChanged();
        }

        return false;
    }
}