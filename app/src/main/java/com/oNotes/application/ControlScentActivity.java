package com.oNotes.application;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blescent.library.BluetoothLeService;

public class ControlScentActivity extends FragmentActivity {

    private static final String TAG = ControlScentActivity.class.getName();
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private MainFragment mainFragment;
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;
    private ProgressDialog mProgressBar;
    private Button retry;
    private boolean isServicesDiscovered;
    public static boolean mConnected;
    private AlertDialog alert11;
    public static boolean commandSend = false;

    private RelativeLayout rlRetry;
    private byte intensity = 50;
    private short duration = 20;
    private int count = 0;
    private Button batteryStatus;
    private EditText response;
    private Button writeTrack;
    private Button readTrack;
    private Button clear;
    StringBuilder responseString ;
    private byte[] writeData= {0x65,0x38,0x37,0x64, 0x33,0x62,0x66,0x31, 0x63,0x30,0x62,0x30, 0x34,0x37,0x32,0x34,
            0x62,0x35,0x37,0x35, 0x36,0x34,0x38,0x61, 0x64,0x32,0x38,0x37, 0x38,0x62,0x37,0x66, 0x0b,0x00,0x01,0x05,
            0x3c,0x00,0x2d,0x00, 0x14,0x01,0x44,0x3c, 0x00,0x2d,0x00,0x14, 0x01,0x4e,0x3c,0x00, 0x2d, 0x00,0x14,0x01,
            0x45,0x3c,0x00,0x2d, 0x00,0x14,0x01,0x43, 0x3c,0x00,0x2d,0x00, 0x14,0x01,0x49};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_control_scent);
        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        count = intent.getIntExtra("COUNT",0);
        retry = (Button) findViewById(R.id.retry);
        rlRetry = (RelativeLayout) findViewById(R.id.rlRetry);
        batteryStatus = (Button) findViewById(R.id.batteryStatus);
        response = (EditText) findViewById(R.id.response) ;
        writeTrack = (Button) findViewById(R.id.writeTrack);
        readTrack = (Button) findViewById(R.id.readTrack);
        clear = (Button) findViewById(R.id.clear);
        isServicesDiscovered = false;
        mProgressBar = new ProgressDialog(this);
        mProgressBar.setMessage("Connecting...");
        mProgressBar.setCancelable(false);
        mProgressBar.show();
        responseString = new StringBuilder();


      /*  AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Connection to device is lost...");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Exit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        builder1.setNegativeButton(
                "Reconnect",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mBluetoothLeService.initialize()) {
                            //rlRetry.setVisibility(View.GONE);
                            mProgressBar.show();
                            commandSend = true;
                            mBluetoothLeService.connect(mDeviceAddress);

                        } else {
                            Toast.makeText(getApplicationContext(), "Service not Initialize", Toast.LENGTH_SHORT).show();
                            refreshActivity();
                            finish();
                        }
                        dialog.cancel();
                    }
                });

        alert11 = builder1.create();
*/
          /*retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if (mBluetoothLeService.initialize()) {
                     //rlRetry.setVisibility(View.GONE);
                     mProgressBar.show();
                     mBluetoothLeService.connect(mDeviceAddress);
                }else{
                     Toast.makeText(getApplicationContext(),"Service not Initialize",Toast.LENGTH_SHORT).show();
                     startMainActivity();
                     finish();
                 }

                // Automatically connects to the device upon successful start-up initialization.

                //startMainActivity();
            }
        });*/
        batteryStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.queryForStatus();
                mBluetoothLeService.queryForOfflineAnalytics();
                mBluetoothLeService.enableTimeout(true);
                mBluetoothLeService.clearOfflineAnalytics();
                mBluetoothLeService.writeSettingsWithFanSpeed(80,true,5);
            }
        });

        writeTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.writeTrackPayload(writeData);
            }
        });
        readTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.readTrackPayload();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                responseString.setLength(0);
                response.setText(responseString);
            }
        });

    }

    private void refreshActivity() {
        Intent intent;
        if (count <= 3) {
            intent = getIntent();
            intent.putExtra(EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
            intent.putExtra("COUNT",count);
            finish();
        } else {
            intent = new Intent(getApplicationContext(), ScanActivity.class);
        }
        startActivity(intent);

    }


    @Override
    protected void onResume() {
        super.onResume();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLeService = null;
        mProgressBar.dismiss();
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            count = 0;
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Log.i(TAG, "Service Connected---->>>>>>>");
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            count++;
            Log.d(TAG, "inside onServiceDisconnected-----");
            mBluetoothLeService = null;
            mProgressBar.hide();
            refreshActivity();
        }
    };


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
/*
                mConnected = true;
                mProgressBar.hide();*/

                if (mainFragment != null) {
                    return;
                }
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                mainFragment = new MainFragment();
                fragmentTransaction.add(R.id.fragment_container, mainFragment);
                fragmentTransaction.commit();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isServicesDiscovered = false;
                mConnected = false;
                // rlRetry.setVisibility(View.VISIBLE);
            /*    alert11.show();
                alert11.setCancelable(false);*/
                mProgressBar.setMessage("reconnecting...");
                mProgressBar.show();
                mBluetoothLeService.connect(mDeviceAddress);
                if (MainFragment.isCommandSend) {
                    MainFragment.isCommandSend = false;
                    MainFragment.status = true;
                    MyListItem myListItem = MainFragment.list.get(MainFragment.lastScentPlayed);
                    myListItem.setState(false);
                    MainFragment.list.set(MainFragment.lastScentPlayed, myListItem);
                    MainFragment.customAdapter.notifyDataSetInvalidated();
                }

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                isServicesDiscovered = true;
                mProgressBar.hide();
                mConnected = true;
               /* if (commandSend) {
                    Log.i(TAG, "Inside my method--->");
                    if (MainFragment.list.size() != 0 && CustomAdapter.commandToExecute)
                        playScent(MainFragment.list.get(MainFragment.lastScentPlayed).getScentCode());
                    else
                        stopScent();
                    commandSend = false;
                } else
                    stopScent();*/
               // stopScent();

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "data:>>" + intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                mProgressBar.hide();
                mConnected = true;
                MainFragment.isCommandSend = false;
                if (MainFragment.status) {
                    /*new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MyListItem myListItem = MainFragment.list.get(MainFragment.lastScentPlayed);
                            myListItem.setState(MainFragment.status);
                            MainFragment.list.set(MainFragment.lastScentPlayed,myListItem);

                            MainFragment.customAdapter.notifyDataSetInvalidated();
                        }
                    },2000);*/

                }
                MyListItem myListItem = MainFragment.list.get(MainFragment.lastScentPlayed);
                myListItem.setState(MainFragment.status);
                MainFragment.list.set(MainFragment.lastScentPlayed, myListItem);

                MainFragment.customAdapter.notifyDataSetInvalidated();

            }else if("PARSE_RESPONSE".equals(action)){
                mProgressBar.hide();
                mConnected = true;
                byte[] data=intent.getByteArrayExtra("RESPONSE");
                if (data != null && data.length > 0) {
                    for (byte byteChar : data)
                        responseString.append(String.format("%02X ", byteChar));
                    responseString.append("\n\n");
                    response.setText(responseString);
                    response.setSelection(responseString.length());
                }
            }else if("OPBTPeripheralFirmwareRevisionNotification".equals(action)){
                stopScent();
                /*commandSend = true;
                MainFragment.status = true;
                MyListItem myListItem = MainFragment.list.get(MainFragment.lastScentPlayed);
                myListItem.setState(false);
                MainFragment.list.set(MainFragment.lastScentPlayed, myListItem);
                MainFragment.customAdapter.notifyDataSetInvalidated();*/

            }
            Log.i(TAG, "inside reciever--->");

        }
    };


    static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction("PARSE_RESPONSE");
        intentFilter.addAction("OPBTPeripheralFirmwareRevisionNotification");
        return intentFilter;
    }

    public void playScent(String scentCode) {
        mBluetoothLeService.playScent(duration, intensity, scentCode);
    }

    public void stopScent() {
        mBluetoothLeService.stopScent();
    }


}

