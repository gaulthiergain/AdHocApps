package com.montefiore.gaulthiergain.slidesadhoc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerAction;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerAdapter;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerApp;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.TransferManager;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.DeviceException;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.GroupOwnerBadValue;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.AdHocDevice;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.DiscoveryListener;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.wifi.WifiAdHocManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[MainActivity]";
    public static final String GO = "[GO]";

    ProgressDialog progressDialog;
    TransferManager transferManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btnTeacher = findViewById(R.id.btnTeacher);
        btnTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                transferManager = new TransferManager(true, initListener());
                try {
                    transferManager.start(MainActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (transferManager.isBluetoothEnabled()) {
                    try {
                        transferManager.disableBluetooth();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (!transferManager.isWifiEnabled()) {
                    transferManager.enableWifi(getApplicationContext(), new ListenerAdapter() {
                        @Override
                        public void onEnableBluetooth(boolean success) {
                            // Ignored
                        }

                        @Override
                        public void onEnableWifi(boolean success) {
                            Toast.makeText(getApplicationContext(), "Wifi is now enabled", Toast.LENGTH_SHORT).show();
                            try {
                                transferManager.setWifiGroupOwnerValue(15);
                            } catch (GroupOwnerBadValue groupOwnerBadValue) {
                                groupOwnerBadValue.printStackTrace();
                            } catch (DeviceException e) {
                                e.printStackTrace();
                            }
                            ManagerHandler.setTransferManager(transferManager);
                            Intent intent = new Intent(MainActivity.this, TeacherActivity.class);
                            startActivity(intent);
                        }
                    });
                } else {
                    try {
                        transferManager.setWifiGroupOwnerValue(15);
                    } catch (GroupOwnerBadValue e) {
                        e.printStackTrace();
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }
                    ManagerHandler.setTransferManager(transferManager);
                    Intent intent = new Intent(MainActivity.this, TeacherActivity.class);
                    startActivity(intent);
                }
            }
        });

        Button btnStudent = findViewById(R.id.btnStudent);
        btnStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                transferManager = new TransferManager(true, initListener());
                try {
                    transferManager.getConfig().setNbThreadWifi(0);
                    transferManager.start(MainActivity.this);
                    transferManager.setWifiGroupOwnerValue(0);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GroupOwnerBadValue groupOwnerBadValue) {
                    groupOwnerBadValue.printStackTrace();
                } catch (DeviceException e) {
                    e.printStackTrace();
                }

                if (transferManager.isBluetoothEnabled()) {
                    try {
                        transferManager.disableBluetooth();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (!transferManager.isWifiEnabled()) {
                    transferManager.enableWifi(getApplicationContext(), new ListenerAdapter() {
                        @Override
                        public void onEnableBluetooth(boolean success) {
                            // Ignored
                        }

                        @Override
                        public void onEnableWifi(boolean success) {
                            try {
                                runDiscovery();
                            } catch (DeviceException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getApplicationContext(), "Wifi is now enabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    try {
                        runDiscovery();
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private DiscoveryListener initListenerDiscovery() {
        return new DiscoveryListener() {
            @Override
            public void onDeviceDiscovered(AdHocDevice device) {
                Log.d(TAG, "Discovery device " + device.getDeviceName());
            }

            @Override
            public void onDiscoveryStarted() {
                Log.d(TAG, "Discovery Started");
            }

            @Override
            public void onDiscoveryFailed(Exception e) {
                Log.e(TAG, "Discovery Failed " + e.getMessage());
                progressDialog.setProgress(0);
                progressDialog.dismiss();
            }

            @Override
            public void onDiscoveryCompleted(HashMap<String, AdHocDevice> mapAddressDevice) {

                boolean found = false;
                progressDialog.dismiss();

                for (Map.Entry<String, AdHocDevice> entry : mapAddressDevice.entrySet()) {
                    AdHocDevice device = entry.getValue();
                    Log.d(TAG, device.getDeviceName());
                    if (device.getDeviceName() != null && device.getDeviceName().contains(GO)) {
                        try {
                            progressDialog = new ProgressDialog(MainActivity.this);
                            progressDialog.setIndeterminate(true);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.setTitle("Connecting to " + device.getDeviceName());
                            progressDialog.show();
                            transferManager.connect(3, device);
                            found = true;
                        } catch (DeviceException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!found) {
                    Toast.makeText(getApplicationContext(), "Teacher device was not found, " +
                            "try discovery again", Toast.LENGTH_LONG).show();
                    progressDialog.setProgress(0);
                }
            }
        };
    }

    private ListenerApp initListener() {
        return new ListenerApp() {

            @Override
            public void onReceivedData(AdHocDevice adHocDevice, Object pdu) {

            }

            @Override
            public void onForwardData(AdHocDevice adHocDevice, Object pdu) {
                // Ignored
            }

            @Override
            public void onConnection(AdHocDevice adHocDevice) {

                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                ManagerHandler.setTransferManager(transferManager);

                Intent intent = new Intent(MainActivity.this, StudentActivity.class);
                startActivity(intent);
            }

            @Override
            public void onConnectionFailed(Exception e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }

            @Override
            public void onConnectionClosed(AdHocDevice adHocDevice) {

            }

            @Override
            public void onConnectionClosedFailed(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void processMsgException(Exception e) {
                e.printStackTrace();
            }
        };
    }

    private void runDiscovery() throws DeviceException {

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Searching teacher app");
        progressDialog.setMessage("Please Wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressDialog.show();
        new Thread() {
            @Override
            public void run() {
                int jumpTime = 0;

                while (jumpTime < WifiAdHocManager.DISCOVERY_TIME) {
                    try {
                        sleep(100);
                        jumpTime++;
                        progressDialog.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        transferManager.discovery(initListenerDiscovery());
    }
}
