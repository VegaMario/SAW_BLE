package com.example.saw_ble;
import com.welie.blessed.BluetoothBytesParser;

import java.io.Serializable;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.nio.ByteBuffer;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_FLOAT;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SFLOAT;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT32;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

public class AZMeasurement implements Serializable{
    public Float az;

    public AZMeasurement(byte[] value){
        BluetoothBytesParser parser = new BluetoothBytesParser(value);

        az = parser.getFloatValue(FORMAT_SFLOAT);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,"%.2f", az);
    }
}
