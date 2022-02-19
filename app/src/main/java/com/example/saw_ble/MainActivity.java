package com.example.saw_ble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.saw_ble.BluetoothHandler;

import com.example.saw_ble.databinding.ActivityMainBinding;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothPeripheral;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    public static boolean connected = false;
    public static boolean writeMSChar = false;
    public static boolean writeHapticChar = false;
    public static boolean writeNeopixChar = false;

    private TextView measurementValue;
    private TextView measurementValue3;
    private TextView measurementValue4;
    private TextView measurementValue5;
    private TextView measurementValue6;
    private TextView measurementValue7;
    private TextView measurementValue8;
    private TextView measurementValue9;
    public static Button pair;
    public static Button actionNeopix;
    public static Button actionHaptic;
    public static Button actionMS;
    public static TextView iNeopix;
    public static TextView iHaptic;
    public static TextView iMS;

    public static BluetoothPeripheral peripheral_instance;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int ACCESS_LOCATION_REQUEST = 2;
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        pair = (Button) findViewById(R.id.button);
        actionNeopix = (Button) findViewById(R.id.button2);
        actionHaptic = (Button) findViewById(R.id.button3);
        actionMS = (Button) findViewById(R.id.button4);

        iNeopix = (TextView) findViewById(R.id.neopixInput);
        iHaptic = (TextView) findViewById(R.id.hapticInput);
        iMS = (TextView) findViewById(R.id.msInput);

        measurementValue = (TextView) findViewById(R.id.textView);
        measurementValue3 = (TextView) findViewById(R.id.textView3);
        measurementValue4 = (TextView) findViewById(R.id.textView4);
        measurementValue5 = (TextView) findViewById(R.id.textView5);
        measurementValue6 = (TextView) findViewById(R.id.textView6);
        measurementValue7 = (TextView) findViewById(R.id.textView7);
        measurementValue8 = (TextView) findViewById(R.id.textView8);
        measurementValue9 = (TextView) findViewById(R.id.textView9);

        pair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!connected) {
                    pair.setText("Searching For SAW");
                    BluetoothHandler.getInstance(getApplicationContext());
                }
            }
        });

        registerReceiver(locationServiceStateReceiver, new IntentFilter((LocationManager.MODE_CHANGED_ACTION)));
        registerReceiver(neopixDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_NEOPIXEL));
        registerReceiver(micDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_AUDIO));
        registerReceiver(hapticDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_HAPTIC));
        registerReceiver(axDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_AX));
        registerReceiver(ayDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_AY));
        registerReceiver(azDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_AZ));
        registerReceiver(gxDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_GX));
        registerReceiver(gyDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_GY));
        registerReceiver(gzDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_GZ));
        registerReceiver(alertMSDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_MSALERT));
        registerReceiver(alertSMDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_SMALERT));

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getBluetoothManager().getAdapter() != null) {
            if (!isBluetoothEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                checkPermissions();
            }
        } else {
            Timber.e("This device has no Bluetooth hardware");
        }
    }

    private boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = getBluetoothManager().getAdapter();
        if(bluetoothAdapter == null) return false;

        return bluetoothAdapter.isEnabled();
    }

    private void initBluetoothHandler()
    {
        //empty but don't delete
        peripheral_instance = BluetoothHandler.getPeriheralAccess();
    }

    @NotNull
    private BluetoothManager getBluetoothManager() {
        return Objects.requireNonNull((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE),"cannot get BluetoothManager");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(locationServiceStateReceiver);
        unregisterReceiver(neopixDataReceiver);
        unregisterReceiver(micDataReceiver);
        unregisterReceiver(hapticDataReceiver);
        unregisterReceiver(axDataReceiver);
        unregisterReceiver(ayDataReceiver);
        unregisterReceiver(azDataReceiver);
        unregisterReceiver(gxDataReceiver);
        unregisterReceiver(gyDataReceiver);
        unregisterReceiver(gzDataReceiver);
        unregisterReceiver(alertMSDataReceiver);
        unregisterReceiver(alertSMDataReceiver);
    }

    // next setup all the broadcast receivers for the gui interface
    private final BroadcastReceiver locationServiceStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(LocationManager.MODE_CHANGED_ACTION)){
                boolean isEnabled = areLocationServicesEnabled();
                Timber.i("Location service state changed to: %s", isEnabled ? "on" : "off");
                checkPermissions();
            }
        }
    };

    private final BroadcastReceiver neopixDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            NeopixMeasurement measurement = (NeopixMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_NEOPIXEL_EXTRA);
            if (measurement == null) return;

            measurementValue.setText(String.format(Locale.ENGLISH, "%d from %s", measurement.color, peripheral.getName()));
        }
    };

    private final BroadcastReceiver micDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            MicMeasurement measurement = (MicMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_AUDIO_EXTRA);
            if (measurement == null) return;


            measurementValue.setText(String.format(Locale.ENGLISH, "Volume Level: %.2f", measurement.volume));
        }
    };

    private final BroadcastReceiver hapticDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            HapticMeasurement measurement = (HapticMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_HAPTIC_EXTRA);
            if (measurement == null) return;

            measurementValue.setText(String.format(Locale.ENGLISH, "%d from %s", measurement.pattern, peripheral.getName()));
        }
    };

    private final BroadcastReceiver axDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            AXMeasurement measurement = (AXMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_AX_EXTRA);
            if (measurement == null) return;

            measurementValue3.setText(String.format(Locale.ENGLISH, "AX: %.2f", measurement.ax));
        }
    };

    private final BroadcastReceiver ayDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            AYMeasurement measurement = (AYMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_AY_EXTRA);
            if (measurement == null) return;

            measurementValue4.setText(String.format(Locale.ENGLISH, "AY: %.2f", measurement.ay));
        }
    };

    private final BroadcastReceiver azDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            AZMeasurement measurement = (AZMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_AZ_EXTRA);
            if (measurement == null) return;

            measurementValue5.setText(String.format(Locale.ENGLISH, "AZ: %.2f", measurement.az));
        }
    };

    private final BroadcastReceiver gxDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            GXMeasurement measurement = (GXMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_GX_EXTRA);
            if (measurement == null) return;

            measurementValue6.setText(String.format(Locale.ENGLISH, "GX: %.2f", measurement.gx));
        }
    };

    private final BroadcastReceiver gyDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            GYMeasurement measurement = (GYMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_GY_EXTRA);
            if (measurement == null) return;

            measurementValue7.setText(String.format(Locale.ENGLISH, "GY: %.2f", measurement.gy));
        }
    };

    private final BroadcastReceiver gzDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            GZMeasurement measurement = (GZMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_GZ_EXTRA);
            if (measurement == null) return;

            measurementValue8.setText(String.format(Locale.ENGLISH, "GZ: %.2f", measurement.gz));
        }
    };

    private final BroadcastReceiver alertMSDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            AlertMSMeasurement measurement = (AlertMSMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_MSALERT_EXTRA);
            if (measurement == null) return;

            measurementValue.setText(String.format(Locale.ENGLISH, "%d from %s", measurement.command, peripheral.getName()));
        }
    };

    private final BroadcastReceiver alertSMDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            AlertSMMeasurement measurement = (AlertSMMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_SMALERT_EXTRA);
            if (measurement == null) return;

            measurementValue9.setText(String.format(Locale.ENGLISH, "AlertSM: %d", measurement.command));
        }
    };


    private BluetoothPeripheral getPeripheral(String peripheralAddress) {
        BluetoothCentralManager central = BluetoothHandler.getInstance(getApplicationContext()).central;
        return central.getPeripheral(peripheralAddress);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] missingPermissions = getMissingPermissions(getRequiredPermissions());
            if (missingPermissions.length > 0) {
                requestPermissions(missingPermissions, ACCESS_LOCATION_REQUEST);
            } else {
                permissionsGranted();
            }
        }
    }

    private String[] getMissingPermissions(String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String requiredPermission : requiredPermissions) {
                if (getApplicationContext().checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission);
                }
            }
        }
        return missingPermissions.toArray(new String[0]);
    }

    private String[] getRequiredPermissions() {
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        } else return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    private void permissionsGranted() {
        // Check if Location services are on because they are required to make scanning work for SDK < 31
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && targetSdkVersion < Build.VERSION_CODES.S) {
            if (checkLocationServices()) {
                initBluetoothHandler();
            }
        } else {
            initBluetoothHandler();
        }
    }

    private boolean areLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Timber.e("could not get location manager");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locationManager.isLocationEnabled();
        } else {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            return isGpsEnabled || isNetworkEnabled;
        }
    }

    private boolean checkLocationServices() {
        if (!areLocationServicesEnabled()) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Location services are not enabled")
                    .setMessage("Scanning for Bluetooth peripherals requires locations services to be enabled.") // Want to enable?
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if all permission were granted
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            permissionsGranted();
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Permission is required for scanning Bluetooth peripherals")
                    .setMessage("Please grant permissions")
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            checkPermissions();
                        }
                    })
                    .create()
                    .show();
        }
    }


}