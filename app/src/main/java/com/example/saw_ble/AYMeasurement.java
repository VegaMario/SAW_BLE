package com.example.saw_ble;
import com.welie.blessed.BluetoothBytesParser;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SFLOAT;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

public class AYMeasurement implements Serializable{
    public Float ay;

    public AYMeasurement(byte[] value){
        BluetoothBytesParser parser = new BluetoothBytesParser(value);

        ay = parser.getFloatValue(FORMAT_SFLOAT);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,"%.2f", ay);
    }
}
