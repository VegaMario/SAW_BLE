package com.example.saw_ble;

import com.welie.blessed.BluetoothBytesParser;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SFLOAT;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

public class HapticMeasurement implements Serializable{
    public Integer pattern;

    public HapticMeasurement(byte[] value){
        BluetoothBytesParser parser = new BluetoothBytesParser(value);

        pattern = parser.getIntValue(FORMAT_UINT8);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,"%d", pattern);
    }
}
