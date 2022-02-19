package com.example.saw_ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.BondState;
import com.welie.blessed.ConnectionPriority;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;
import com.example.saw_ble.MainActivity;

import com.welie.blessed.ScanFailure;
import com.welie.blessed.WriteType;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import timber.log.Timber;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_SINT16;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT16;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT8;
import static com.welie.blessed.BluetoothBytesParser.bytes2String;

import static java.lang.Math.abs;

import androidx.annotation.NonNull;

public class BluetoothHandler {
    // intent constants
    public static final String MEASUREMENT_NEOPIXEL = "blessed.measurement.neopixel";
    public static final String MEASUREMENT_NEOPIXEL_EXTRA = "blessed.measurement.neopixel.extra";
    public static final String MEASUREMENT_HAPTIC = "blessed.measurement.haptic";
    public static final String MEASUREMENT_HAPTIC_EXTRA = "blessed.measurement.haptic.extra";
    public static final String MEASUREMENT_AX = "blessed.measurement.ax";
    public static final String MEASUREMENT_AX_EXTRA = "blessed.measurement.ax.extra";
    public static final String MEASUREMENT_AY = "blessed.measurement.ay";
    public static final String MEASUREMENT_AY_EXTRA = "blessed.measurement.ay.extra";
    public static final String MEASUREMENT_AZ = "blessed.measurement.az";
    public static final String MEASUREMENT_AZ_EXTRA = "blessed.measurement.az.extra";
    public static final String MEASUREMENT_GX = "blessed.measurement.gx";
    public static final String MEASUREMENT_GX_EXTRA = "blessed.measurement.gx.extra";
    public static final String MEASUREMENT_GY = "blessed.measurement.gy";
    public static final String MEASUREMENT_GY_EXTRA = "blessed.measurement.gy.extra";
    public static final String MEASUREMENT_GZ = "blessed.measurement.gz";
    public static final String MEASUREMENT_GZ_EXTRA = "blessed.measurement.gz.extra";
    public static final String MEASUREMENT_AUDIO = "blessed.measurement.audio";
    public static final String MEASUREMENT_AUDIO_EXTRA = "blessed.measurement.audio.extra";
    public static final String MEASUREMENT_MSALERT = "blessed.measurement.msalert";
    public static final String MEASUREMENT_MSALERT_EXTRA = "blessed.measurement.msalert.extra";
    public static final String MEASUREMENT_SMALERT = "blessed.measurement.smalert";
    public static final String MEASUREMENT_SMALERT_EXTRA = "blessed.measurement.smalert.extra";
    public static final String MEASUREMENT_EXTRA_PERIPHERAL = "blessed.measurement.peripheral";

    // UUIDs for the neopixel service
    private static final UUID NEOPIXEL_UUID = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214");
    private static final UUID NEOPIXEL_CHARACTERISTIC_UUID = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1214");

    // UUIDs for the mic service
    private static final UUID MIC_UUID = UUID.fromString("19B11000-E8F2-537E-4F6C-D104768A1214");
    private static final UUID MIC_CHARACTERISTIC_UUID = UUID.fromString("19B11001-E8F2-537E-4F6C-D104768A1214");

    // UUIDs for the haptic service
    private static final UUID HAPTIC_UUID = UUID.fromString("19B12000-E8F2-537E-4F6C-D104768A1214");
    private static final UUID HAPTIC_CHARACTERISTIC_UUID = UUID.fromString("19B12001-E8F2-537E-4F6C-D104768A1214");

    // UUIDs for the accel service
    private static final UUID ACCEL_UUID = UUID.fromString("19B13000-E8F2-537E-4F6C-D104768A1214");
    private static final UUID AX_CHARACTERISTIC_UUID = UUID.fromString("19B13001-E8F2-537E-4F6C-D104768A1214");
    private static final UUID AY_CHARACTERISTIC_UUID = UUID.fromString("19B13002-E8F2-537E-4F6C-D104768A1214");
    private static final UUID AZ_CHARACTERISTIC_UUID = UUID.fromString("19B13003-E8F2-537E-4F6C-D104768A1214");

    // UUIDs for the gyro service
    private static final UUID GYRO_UUID = UUID.fromString("19B14000-E8F2-537E-4F6C-D104768A1214");
    private static final UUID GX_CHARACTERISTIC_UUID = UUID.fromString("19B14001-E8F2-537E-4F6C-D104768A1214");
    private static final UUID GY_CHARACTERISTIC_UUID = UUID.fromString("19B14002-E8F2-537E-4F6C-D104768A1214");
    private static final UUID GZ_CHARACTERISTIC_UUID = UUID.fromString("19B14003-E8F2-537E-4F6C-D104768A1214");

    // UUIDs for the alert Service
    private static final UUID ALERT_UUID = UUID.fromString("19B15000-E8F2-537E-4F6C-D104768A1214");
    private static final UUID ALERTMS_CHARACTERISTIC_UUID = UUID.fromString("19B15002-E8F2-537E-4F6C-D104768A1214");
    private static final UUID ALERTSM_CHARACTERISTIC_UUID = UUID.fromString("19B15001-E8F2-537E-4F6C-D104768A1214");

    // UUID for device
    private static final UUID DEVICE_UUID = UUID.fromString("67A2D57D-2B0E-BEC7-AC26-CCF8411DD4DE");


    // local variables
    public BluetoothCentralManager central;
    private static BluetoothPeripheral peripheralInstance;
    public BluetoothPeripheral selectedPeripheral;
    private static BluetoothHandler instance = null;
    private final Context context;
    private final Handler handler = new Handler();


    // Callback for peripheral
    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(@NonNull BluetoothPeripheral peripheral) {
            // Request a higher MTU, iOS always asks for 185
            peripheral.requestMtu(185);

            // Request a new connection priority
            peripheral.requestConnectionPriority(ConnectionPriority.HIGH);

            // TRY TO TURN ON NOTIFICATION FOR OTHER CHARACTERISTICS
            peripheral.setNotify(MIC_UUID, MIC_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(ACCEL_UUID, AX_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(ACCEL_UUID, AY_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(ACCEL_UUID, AZ_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(GYRO_UUID, GX_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(GYRO_UUID, GY_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(GYRO_UUID, GZ_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(ALERT_UUID, ALERTSM_CHARACTERISTIC_UUID, true);
        }

        @Override
        public void onNotificationStateUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status){
            if (status == GattStatus.SUCCESS) {
                final boolean isNotifying = peripheral.isNotifying(characteristic);
                Timber.i("SUCCESS: Notify set to '%s' for %s", isNotifying, characteristic.getUuid());
            } else {
                Timber.e("ERROR: Changing notification state failed for %s (%s)", characteristic.getUuid(), status);
            }
        }

        @Override
        public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                Timber.i("SUCCESS: Writing <%s> to <%s>", bytes2String(value), characteristic.getUuid());
            } else {
                Timber.i("ERROR: Failed writing <%s> to <%s> (%s)", bytes2String(value), characteristic.getUuid(), status);
            }
        }

        @Override
        public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status != GattStatus.SUCCESS) return;

            UUID characteristicUUID = characteristic.getUuid();
            BluetoothBytesParser parser = new BluetoothBytesParser(value);

            if (characteristicUUID.equals(NEOPIXEL_CHARACTERISTIC_UUID)){
                NeopixMeasurement measurement = new NeopixMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_NEOPIXEL);
                intent.putExtra(MEASUREMENT_NEOPIXEL_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Timber.d("%s", measurement);
            }
            else if (characteristicUUID.equals(MIC_CHARACTERISTIC_UUID)){
                MicMeasurement measurement = new MicMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_AUDIO);
                intent.putExtra(MEASUREMENT_AUDIO_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Timber.d("%s", measurement);
            }
            else if (characteristicUUID.equals(HAPTIC_CHARACTERISTIC_UUID)){
                HapticMeasurement measurement = new HapticMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_HAPTIC);
                intent.putExtra(MEASUREMENT_HAPTIC_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Timber.d("%s", measurement);
            }
            else if (characteristicUUID.equals(AX_CHARACTERISTIC_UUID)){
                AXMeasurement measurement = new AXMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_AX);
                intent.putExtra(MEASUREMENT_AX_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Timber.d("%s", measurement);
            }
            else if (characteristicUUID.equals(AY_CHARACTERISTIC_UUID)){
                AYMeasurement measurement = new AYMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_AY);
                intent.putExtra(MEASUREMENT_AY_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Timber.d("%s", measurement);
            }
            else if (characteristicUUID.equals(AZ_CHARACTERISTIC_UUID)){
                AZMeasurement measurement = new AZMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_AZ);
                intent.putExtra(MEASUREMENT_AZ_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Timber.d("%s", measurement);
            }
            else if (characteristicUUID.equals(GX_CHARACTERISTIC_UUID)){
                GXMeasurement measurement = new GXMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_GX);
                intent.putExtra(MEASUREMENT_GX_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Timber.d("%s", measurement);
            }
            else if (characteristicUUID.equals(GY_CHARACTERISTIC_UUID)){
                GYMeasurement measurement = new GYMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_GY);
                intent.putExtra(MEASUREMENT_GY_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Timber.d("%s", measurement);
            }
            else if (characteristicUUID.equals(GZ_CHARACTERISTIC_UUID)){
                GZMeasurement measurement = new GZMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_GZ);
                intent.putExtra(MEASUREMENT_GZ_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Timber.d("%s", measurement);
            }
            else if (characteristicUUID.equals(ALERTMS_CHARACTERISTIC_UUID)){
                AlertMSMeasurement measurement = new AlertMSMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_MSALERT);
                intent.putExtra(MEASUREMENT_MSALERT_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Timber.d("%s", measurement);
            }
            else if (characteristicUUID.equals(ALERTSM_CHARACTERISTIC_UUID)){
                AlertSMMeasurement measurement = new AlertSMMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_SMALERT);
                intent.putExtra(MEASUREMENT_SMALERT_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Timber.d("%s", measurement);
            }
        }

        @Override
        public void onMtuChanged(@NotNull BluetoothPeripheral peripheral, int mtu, @NotNull GattStatus status) {
            Timber.i("new MTU set: %d", mtu);
        }

        private void sendMeasurement(@NotNull Intent intent, @NotNull BluetoothPeripheral peripheral ) {
            intent.putExtra(MEASUREMENT_EXTRA_PERIPHERAL, peripheral.getAddress());
            context.sendBroadcast(intent);
        }
    };

    // Callback for central
    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
        @Override
        public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
            Timber.i("connected to '%s'", peripheral.getName());
            MainActivity.connected = true;
            MainActivity.pair.setText("Disconnect");
            selectedPeripheral = peripheral;
            peripheralInstance = peripheral;
            initFunctions();
        }

        @Override
        public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, final @NotNull HciStatus status) {
            Timber.e("connection '%s' failed with status %s", peripheral.getName(), status);
            MainActivity.connected = false;
            MainActivity.pair.setText("Try Connecting Again");
        }

        @Override
        public void onDisconnectedPeripheral(@NotNull final BluetoothPeripheral peripheral, final @NotNull HciStatus status) {
            Timber.i("disconnected '%s' with status %s", peripheral.getName(), status);
            MainActivity.connected = false;
            MainActivity.pair.setText("Connect to SAW");
            //selectedPeripheral = null;
        }

        @Override
        public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull ScanResult scanResult) {
            Timber.i("Found peripheral '%s'", peripheral.getName());
            MainActivity.pair.setText("Found SAW");
            central.stopScan();

            central.connectPeripheral(peripheral, peripheralCallback);
        }

        @Override
        public void onBluetoothAdapterStateChanged(int state) {
            Timber.i("bluetooth adapter changed state to %d", state);
            if (state == BluetoothAdapter.STATE_ON) {
                // Bluetooth is on now, start scanning again
                // Scan for peripherals with a certain service UUIDs
                central.startPairingPopupHack();
                startScan();
            }
        }

        @Override
        public void onScanFailed(@NotNull ScanFailure scanFailure) {
            Timber.i("scanning failed with error %s", scanFailure);
        }

    };

    public static synchronized BluetoothHandler getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothHandler(context.getApplicationContext());
        }
        return instance;
    }


    private BluetoothHandler(Context context) {
        this.context = context;

        // Plant a tree
        Timber.plant(new Timber.DebugTree());

        // Create BluetoothCentral
        central = new BluetoothCentralManager(context, bluetoothCentralManagerCallback, new Handler());

        // Scan for peripherals with a certain service UUIDs
        central.startPairingPopupHack();
        startScan();
    }

    private void startScan() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                central.scanForPeripheralsWithServices(new UUID[]{NEOPIXEL_UUID, MIC_UUID, HAPTIC_UUID, ACCEL_UUID, GYRO_UUID, ALERT_UUID});
            }
        },1000);
    }

    private void initFunctions(){
        if(MainActivity.connected){
            MainActivity.pair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(MainActivity.connected) {
                        MainActivity.pair.setText("Disconnecting");
                        disconnect(selectedPeripheral);
                    }
                    else{
                        MainActivity.pair.setText("Searching For SAW");
                        connect(selectedPeripheral);
                    }
                }
            });

            MainActivity.actionNeopix.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String in = MainActivity.iNeopix.getText().toString();
                    Integer input;
                    if (in.equals("")){
                        input = 0;
                    }
                    else{
                        input = Integer.parseInt(in);
                    }
                    writeToNeopixChar(selectedPeripheral, input);
                }
            });

            MainActivity.actionHaptic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String in = MainActivity.iHaptic.getText().toString();
                    Integer input;
                    if (in.equals("")){
                        input = 0;
                    }
                    else{
                        input = Integer.parseInt(in);
                    }
                    writeToHapticChar(selectedPeripheral, input);
                }
            });

            MainActivity.actionMS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String in = MainActivity.iMS.getText().toString();
                    Integer input;
                    if (in.equals("")){
                        input = 0;
                    }
                    else{
                        input = Integer.parseInt(in);
                    }
                    writeToMSChar(selectedPeripheral, input);
                }
            });
        }
    }

    public static void writeToNeopixChar(@NotNull BluetoothPeripheral peripheral, @NotNull Integer input){
        BluetoothBytesParser parser = new BluetoothBytesParser(ByteOrder.LITTLE_ENDIAN);
        parser.setIntValue(input, FORMAT_UINT8);
        peripheral.writeCharacteristic(NEOPIXEL_UUID, NEOPIXEL_CHARACTERISTIC_UUID, parser.getValue(), WriteType.WITH_RESPONSE);
        MainActivity.writeNeopixChar = false;
    }

    public static void writeToHapticChar(@NotNull BluetoothPeripheral peripheral, @NotNull Integer input){
        BluetoothBytesParser parser = new BluetoothBytesParser(ByteOrder.LITTLE_ENDIAN);
        parser.setIntValue(input, FORMAT_UINT8);
        peripheral.writeCharacteristic(HAPTIC_UUID, HAPTIC_CHARACTERISTIC_UUID, parser.getValue(), WriteType.WITH_RESPONSE);
        MainActivity.writeHapticChar = false;
    }

    public static void writeToMSChar(@NotNull BluetoothPeripheral peripheral, @NotNull Integer input){
        BluetoothBytesParser parser = new BluetoothBytesParser(ByteOrder.LITTLE_ENDIAN);
        parser.setIntValue(input, FORMAT_UINT8);
        peripheral.writeCharacteristic(ALERT_UUID, ALERTMS_CHARACTERISTIC_UUID, parser.getValue(), WriteType.WITH_RESPONSE);
        MainActivity.writeMSChar = false;
    }

    private void connect(@NotNull BluetoothPeripheral peripheral){
        central.scanForPeripheralsWithServices(new UUID[]{NEOPIXEL_UUID, MIC_UUID, HAPTIC_UUID, ACCEL_UUID, GYRO_UUID, ALERT_UUID});
    }

    private void disconnect(@NotNull BluetoothPeripheral peripheral){
        MainActivity.connected = false;
        central.cancelConnection(peripheral);
    }

    public static synchronized BluetoothPeripheral getPeriheralAccess(){
        return peripheralInstance;
    }

}
