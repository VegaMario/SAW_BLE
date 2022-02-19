package com.example.saw_ble;

import com.welie.blessed.BluetoothBytesParser;

import java.io.Serializable;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SFLOAT;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT32;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

import android.util.Log;

import timber.log.Timber;

public class MicMeasurement implements Serializable{
    public Float volume;

    public MicMeasurement(byte[] value){

        BluetoothBytesParser parser = new BluetoothBytesParser(value);

        // get the volume level
        volume = parser.getFloatValue(FORMAT_SFLOAT);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,"%.2f", volume);
    }
}
