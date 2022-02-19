package com.example.saw_ble;
import com.welie.blessed.BluetoothBytesParser;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SFLOAT;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

public class AlertSMMeasurement implements Serializable{
    public Integer command;

    public AlertSMMeasurement(byte[] value){
        BluetoothBytesParser parser = new BluetoothBytesParser(value);

        command = parser.getIntValue(FORMAT_UINT8);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,"%d", command);
    }
}
