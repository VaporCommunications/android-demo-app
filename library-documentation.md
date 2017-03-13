#BlueTooth

##Used Keys

####Services Key
```Android
 public static final UUID kOPhoneServiceUUID = UUID.fromString("B8E06067-62AD-41BA-9231-206AE80AB550");
```

####Characteristics Key

```Android
 public static final UUID kOPhoneTXCharacteristicUUID = UUID.fromString("BF45E40A-DE2A-4BC8-BBA0-E5D6065F1B4B");
```

##Basic Read Workflow

####Step 1:
Get all peripherals which is having 'kOPhoneServiceUUID' and scan for additional peripherals .
 UUID services[]={BluetoothLeService.kOPhoneServiceUUID};
 mBluetoothAdapter.startLeScan(services,mLeScanCallback);

####Step 2:
If a user selects one of these peripherals, we attempt to connect which sets off a chain of callbacks.

####Step 3:
On successful connection via a `BluetoothGattCallback` method, we attempt to discover the oPhone service on the peripheral:

```Android
     public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            Log.e(TAG,"inside statechange method----"+newState);
            if(newState == BluetoothProfile.STATE_DISCONNECTING)
            {
                Log.d(TAG,"Disconnecting from server----");
            }
            if(newState == BluetoothProfile.STATE_CONNECTING){
                Log.d(TAG,"newState is connecting-----");
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                 Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" );
                        mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                 Log.i(TAG, "Disconnected from GATT server.");
            }
        }
```

####Step 4:

Now we listen to the `BluetoothGattCallback` Methods to see if services were discovered and we can get characteristics from here:

```Android
 public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
               	     if(gatt.getService(kOPhoneServiceUUID).getCharacteristic(kOPhoneTXCharacteristicUUID)==null){
			//characteristics not present. This is not our device.	
		     }else{
	                //characteristics present. Store the BluetoothGatt object.
			this.mBluetoothGatt = gatt;
		     }
	    } else {
                //onServicesDiscovered failed
            }
        }
```


##Basic Write Workflow

Once there is a peripheral connected, we can write data.

####Step 1: Convert scentCode,intensity,duration to byte[]

```Android
   public byte[] getByteArray(String scentCode,short duration,byte intensity){
        byte code[] = scentCode.getBytes();
        byte[] command = {'V', 'C', 0x01, 0x01, command};
        byte[] finalBytes = new byte[command.length + 2+4 +code.length+ 1];

        short payloadLength = (short) (code.length+4);
        System.arraycopy(command, 0, finalBytes, 0, command.length);

        finalBytes[command.length] = (byte)(payloadLength & 0xff);//
        finalBytes[command.length+1] = (byte)((payloadLength >> 8) & 0xff);
        finalBytes[command.length+2] = intensity;
        finalBytes[command.length+3] = (byte)(duration & 0xff);
        finalBytes[command.length+4] = (byte)((duration >> 8) & 0xff);
        finalBytes[command.length+5] = (byte)(code.length);
        System.arraycopy(code, 0, finalBytes,command.length+6 , code.length);
       return finalBytes;
    }
```

####Step 2: Transmit this data payload to the device

If there is an active peripheral, it has a valid oPhone characteristic, and it is currently connected, we transmit the data.  Here's the current implementation:

```Android
             BluetoothGattCharacteristic mNotifyCharacteristic = mBluetoothGatt.getService(kOPhoneServiceUUID)
                        .getCharacteristic(kOPhoneTXCharacteristicUUID);
             mNotifyCharacteristic.setValue(byteTosend);
             mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);

```

