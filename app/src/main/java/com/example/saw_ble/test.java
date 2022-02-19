package com.example.saw_ble;

import com.welie.blessed.BluetoothPeripheral;

public class test {
    private static BluetoothPeripheral peripheral_instance;
    public void execute_command(){
        // this should work
        peripheral_instance = BluetoothHandler.getPeriheralAccess();
        BluetoothHandler.writeToHapticChar(peripheral_instance, 123);
    }
}
