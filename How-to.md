
##Setting
copy com.bleScent.library-debug.aar file in libs folder

for adding com.bleScent.library-debug.aar in project, write compile(name:'com.bleScent.library-debug', ext:'aar') in build.gradle

##connect with gatt server with device address
Bind BluetoothLeService in your activity when it starts. It means bind in onResume method of activity

#Java-Code
	Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
	bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
#Java-Code

callback of bind service get in 'ServiceConnection'. In onServiceConnected, call mBluetoothLeService.initialize(). if it returns true, then only go for other methods.

#Java-Code
  private final ServiceConnection mServiceConnection = new ServiceConnection()
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        //after connecting to service call initialize method it compulsory.
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if(!mBluetoothLeService.initialize()){
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
#Java-Code

Register receiver in your onResume method

#Java-Code
    registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
#Java-Code

Pass the below callback to registerReceiver

#Java-Code
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
			//after connecting to gatt server it response here
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                // if app disconnected from device response here
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // after gatt services descored response here
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // play or stop sent data response is here
            }     
        }
    };
#Java-Code
    
To check, if bluetooth is enabled, call 

#Java-Code
	mBluetoothLeService.isBluetoothEnable()
#Java-Code

Before call of below method make sure your device has location permission and bluetooth is enable

#Java-Code - for location permission
   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
               if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                   requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
               } else {
                  //TO-DO
               }
           }
#Java-Code


response of above permission request in "onRequestPermissionsResult"

#Java-Code
     @Override
        public void onRequestPermissionsResult(int requestCode,
                                               String permissions[],
                                               int[] grantResults) {
            switch (requestCode) {
                case PERMISSION_REQUEST_COARSE_LOCATION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                       //permission granted
                       //TO-DO
                    } else {
                       //permission not granted
                       //TO-DO
                    }
                    return;
                }
            }
        }
#Java-Code


#Java-Code to enable bluetooth
       if (!mBluetoothLeService.isBluetoothEnable()) {
                   Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                   startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
               } else {
                   //bluetooth is enable
                   //TO-DO
               }

#Java-Code

response from user catch in "onActivityResult" method
      
#Java-Code      
       @Override
          protected void onActivityResult(int requestCode, int resultCode, Intent data) {
              // User chose not to enable Bluetooth.
              if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
                  //bluetooth not enable
                  //TO-DO
              }
              if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
                  //bluetooth enabled by user
                  //TO-DO
              }
              super.onActivityResult(requestCode, resultCode, data);
          }
#Java-Code

To scan BLE devices with service id "B8E06067-62AD-41BA-9231-206AE80AB550"

  mBluetoothLeService.scanForDevices(BluetoothAdapter.LeScanCallback)


To stop scanning of BLE devices

  mBluetoothLeService.stopScanning(BluetoothAdapter.LeScanCallback)


To play scent

 mBluetoothLeService.playScent(duration, intensity, scentCode);


To stop scent play

 mBluetoothLeService.stopScent();


To connecting to gatt server (device)

 mBluetoothLeService.connect(mDeviceAddress);


To check if characteristics is present

 mBluetoothLeService.isCharacteristicsPresent()


To get all services after it discovered

 mBluetoothLeService.getSupportedGattServices()


To disconnect from server (device)
  mBluetoothLeService.disconnect()

To Other command methods :
  queryForStatus(),
  writeTrackPayload(byte[]<payload>),
  readTrackPayload(),
  queryForRFID(),
  queryForOfflineAnalytics(),
  clearOfflineAnalytics(),
  enableTimeout(boolean<isEnable>),
  writeSettingsWithFanSpeed(int<fanSpeedPercentage>, boolean<isTimeoutOn>, int<timeoutMinutes>)


  for receiving any valid response it will accepted in receiver at action "PARSE_RESPONSE"

#Java-Code
 private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if("PARSE_RESPONSE".equals(action)){
                           byte[] data=intent.getByteArrayExtra("RESPONSE");
                           //data is valid response
                           }
                       };
 #Java-Code


       

 
