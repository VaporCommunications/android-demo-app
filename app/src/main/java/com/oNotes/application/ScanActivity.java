package com.oNotes.application;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blescent.library.BluetoothLeService;

import java.util.UUID;


public class ScanActivity extends Activity {

    private static final String TAG = ScanActivity.class.getName();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 5000;
    private boolean mScanning;
    private Handler mHandler;


    private ProgressDialog mProgressBar;
    private static final String DEVICE_NAME = "cyrano";
    private Button retry;
    private TextView tvMessage;
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    BleDevicesAdapter bleDevicesAdapter;
    private ListView listView;
    private Activity gActivity;
    private BluetoothLeService mBluetoothLeService;
    private static String deviceAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.content_main);
        retry = (Button) findViewById(R.id.retry);
        tvMessage = (TextView) findViewById(R.id.tv);
        retry.setBackgroundColor(Color.rgb(40, 195, 213));
        if (getActionBar() != null)
            getActionBar().setTitle("Devices");

        mHandler = new Handler();
        mProgressBar = new ProgressDialog(this);
        mProgressBar.setMessage("Scanning...");
        mProgressBar.setCancelable(false);
        gActivity = this;

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvMessage.setVisibility(View.GONE);
                retry.setVisibility(View.GONE);
                scanLeDevice(true);
            }
        });
        listView = (ListView)findViewById(R.id.devicelist);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, ControlScentActivity.makeGattUpdateIntentFilter());
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if (mBluetoothLeService.isCharacteristicsPresent()) {
                    backToPreviousActivity(deviceAddress);
                } else {
                    bleDevicesAdapter.clear();
                    bleDevicesAdapter.notifyDataSetChanged();
                    retry.setVisibility(View.VISIBLE);
                    tvMessage.setVisibility(View.VISIBLE);
                    mProgressBar.hide();
                    Toast.makeText(getApplicationContext(), "characteristics does not present", Toast.LENGTH_SHORT).show();
                }
            }else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mProgressBar.hide();
                bleDevicesAdapter.clear();
                bleDevicesAdapter.notifyDataSetChanged();
                retry.setVisibility(View.VISIBLE);
                tvMessage.setVisibility(View.VISIBLE);
            }
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if(!mBluetoothLeService.initialize()){
                finish();
            }
            getPermission();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            } else {
                bleEnable();
            }
        } else {
            bleEnable();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                    bleEnable();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    private void bleEnable() {
        if (!mBluetoothLeService.isBluetoothEnable()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            init();
            scanLeDevice(true);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            bleEnable();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void init() {
        bleDevicesAdapter = new BleDevicesAdapter(getApplicationContext());
        listView.setAdapter(bleDevicesAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick((ListView) parent,view,position,id);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothLeService != null)
            mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);
        scanLeDevice(false);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        try {
            mProgressBar.setMessage("Confirming...");
            mProgressBar.show();
            final BluetoothDevice device = bleDevicesAdapter.getDevice(position);
            if (mBluetoothLeService != null && !mBluetoothLeService.initialize()) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            } else {
                deviceAddress = device.getAddress();
                mBluetoothLeService.connect(deviceAddress);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void backToPreviousActivity(String device) {
        Intent intent = new Intent(getBaseContext(), ControlScentActivity.class);
        intent.putExtra(ControlScentActivity.EXTRAS_DEVICE_ADDRESS, device);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_scan, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mProgressBar.show();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothLeService.stopScanning(mLeScanCallback);
                    if (bleDevicesAdapter.getCount() == 1) {
                        onListItemClick(null, null, 0, 0);
                    }else if(bleDevicesAdapter.getCount() == 0){
                        mProgressBar.hide();
                        tvMessage.setVisibility(View.VISIBLE);
                         retry.setVisibility(View.VISIBLE);
                    }
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothLeService.scanForDevices( mLeScanCallback);
            invalidateOptionsMenu();
        } else {
            mScanning = false;
            mBluetoothLeService.scanForDevices(mLeScanCallback);
            invalidateOptionsMenu();
        }
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.hide();
                            bleDevicesAdapter.addDevice(device, rssi);
                            bleDevicesAdapter.notifyDataSetChanged();

                        }
                    });
                }
            };


    @Override
    protected void onDestroy() {
         mProgressBar.dismiss();
        super.onDestroy();
    }


}
