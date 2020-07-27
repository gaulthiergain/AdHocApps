package com.montefiore.gaulthiergain.simongameadhoc;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerAction;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerApp;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.TransferManager;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.DeviceException;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.GroupOwnerBadValue;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.AdHocDevice;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.DiscoveryListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.telephony.PhoneNumberUtils.WAIT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[AdHoc][Main]";
    private static final CharSequence SERVER_NAME = "Archos";

    public static final int RUN_GAME = 0;
    private static final int REQUEST_CODE_LOC = 1;

    private Game game;

    private boolean server;
    private ProgressDialog dialog;
    private CustomAdapter dataAdapter;
    private TransferManager transferManager;
    private HashMap<String, AdHocDevice> mapAdhocDevices;

    protected void showDialogPopup(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapAdhocDevices = new HashMap<>();

        try {
            setup();
        } catch (GroupOwnerBadValue groupOwnerBadValue) {
            groupOwnerBadValue.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.score:

                if (game != null) {
                    StringBuilder str = new StringBuilder();
                    for (Map.Entry<String, String> entry : game.getScores().entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        str.append("Player: ").append(key).append("Score: ").append(value).append("\n");
                    }

                    showDialogPopup(str.toString());
                }

                return true;
            case R.id.action_disconnect:
                Log.d(TAG, "Disconnect");
                try {
                    transferManager.disconnectAll();

                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setup() throws GroupOwnerBadValue, IOException {

        transferManager = new TransferManager(true, initListener());
        transferManager.start(getApplicationContext());

        Button btnScan = findViewById(R.id.btnScan);
        btnScan.setVisibility(View.VISIBLE);
        btnScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                server = false;
                for (String name : transferManager.getActifAdapterNames().values()) {
                    Log.d(TAG, "NAME: " + name + " ---> " + name.contains(SERVER_NAME));
                    if (name.contains(SERVER_NAME)) {
                        server = true;
                        break;
                    }
                }

                try {
                    if (server) {
                        transferManager.setWifiGroupOwnerValue(15);
                    } else {
                        transferManager.setWifiGroupOwnerValue(0);
                    }
                } catch (GroupOwnerBadValue groupOwnerBadValue) {
                    groupOwnerBadValue.printStackTrace();
                } catch (DeviceException e) {
                    e.printStackTrace();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    accessLocationPermission();
                } else {
                    try {
                        transferManager.discovery(initDiscoveryListener());
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }
                }

                dialog = ProgressDialog.show(MainActivity.this,
                        "Discovery in progress", "Please wait...", true);

            }
        });

        checkButtonClick();

        Button btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    transferManager.broadcast(RUN_GAME);

                    ViewFlipper vf = findViewById(R.id.viewFlipper);
                    vf.showNext();

                    game = new Game(MainActivity.this, transferManager, mapAdhocDevices.size());
                    game.init();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DeviceException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private DiscoveryListener initDiscoveryListener() {
        return new DiscoveryListener() {
            @Override
            public void onDeviceDiscovered(AdHocDevice device) {
                dialog.setMessage(
                        "Device found " + device.getDeviceName() + " " + device.getStringType());
            }

            @Override
            public void onDiscoveryStarted() {
                Log.d(TAG, "start Discovery");

            }

            @Override
            public void onDiscoveryFailed(Exception exception) {

                dialog.dismiss();

                Log.e(TAG, "On discovery failed " + exception.getMessage());
            }

            @Override
            public void onDiscoveryCompleted(HashMap<String, AdHocDevice> mapAddressDevice) {

                Log.d(TAG, "On discovery completed");

                updateList(mapAddressDevice);

                dialog.dismiss();
            }
        };
    }

    private void resetView() {

        //create an ArrayAdaptar from the String Array
        dataAdapter = new CustomAdapter(MainActivity.this,
                R.layout.row, new ArrayList<SelectedDevice>());
        ListView listView = findViewById(R.id.listViewDiscoveredDevice);
        listView.setVisibility(View.VISIBLE);

        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

        dataAdapter.notifyDataSetChanged();

        Button btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setVisibility(View.GONE);
    }

    private ListenerApp initListener() {
        return new ListenerApp() {

            @Override
            public void onReceivedData(final AdHocDevice adHocDevice, Object pdu) {
                if (pdu instanceof Integer) {
                    if ((Integer) pdu == RUN_GAME) {
                        dialog.dismiss();

                        ViewFlipper vf = findViewById(R.id.viewFlipper);
                        vf.showNext();

                        game = new Game(MainActivity.this, transferManager, adHocDevice,
                                new ConnectionClosedListener() {
                                    @Override
                                    public void connectionClosed() {
                                        ViewFlipper vf = findViewById(R.id.viewFlipper);
                                        vf.showPrevious();
                                        resetView();
                                        game = null;
                                        transferManager.updateListenerApp(initListener());
                                    }
                                });
                        game.init();
                    }
                } else if (pdu instanceof String) {
                    Toast.makeText(getApplicationContext(), "Game server is " + pdu.toString() +
                            " must disconnect with " + adHocDevice.getDeviceName(), Toast.LENGTH_LONG).show();
                    try {
                        transferManager.disconnect(adHocDevice);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onForwardData(AdHocDevice adHocDevice, Object pdu) {
                // Ignored
            }

            @Override
            public void onConnectionClosed(AdHocDevice adHocDevice) {


                Log.d(TAG, "Disconnect with " + adHocDevice.getLabel() + "(" + adHocDevice.getDeviceName() + ")");
                Toast.makeText(getApplicationContext(), "Disconnect with device: " + adHocDevice.getDeviceName(),
                        Toast.LENGTH_SHORT).show();

                if (dialog != null) {
                    dialog.dismiss();
                }
            }

            @Override
            public void onConnectionClosedFailed(Exception e) {
                Log.e(TAG, "Closing connection failed: " + e.getMessage());
            }

            @Override
            public void processMsgException(Exception e) {
                Log.e(TAG, "Processing message failed: " + e.getMessage());
            }

            @Override
            public void onConnection(AdHocDevice adHocDevice) {

                Log.d(TAG, "Direct connection with " + adHocDevice.getLabel() + "(" + adHocDevice.getDeviceName() + ")");

                if (server) {
                    Button btnPlay = findViewById(R.id.btnPlay);
                    btnPlay.setVisibility(View.VISIBLE);
                    mapAdhocDevices.put(adHocDevice.getMacAddress(), adHocDevice);
                } else {
                    dialog = ProgressDialog.show(MainActivity.this,
                            "Waiting game", "Please wait...", true);
                }
            }

            @Override
            public void onConnectionFailed(Exception e) {
                Log.e(TAG, "Connection failed: " + e.getMessage());
            }

        };
    }

    private void updateList(HashMap<String, AdHocDevice> mapAddressDevice) {
        ArrayList<SelectedDevice> listOfDevices = new ArrayList<>();

        for (AdHocDevice device : mapAddressDevice.values()) {
            listOfDevices.add(new SelectedDevice(device));
        }

        if (listOfDevices.size() == 0) {
            Toast.makeText(getApplicationContext(),
                    "Not devices found", Toast.LENGTH_LONG).show();
        } else {
            //create an ArrayAdaptar from the String Array
            dataAdapter = new CustomAdapter(MainActivity.this,
                    R.layout.row, listOfDevices);
            ListView listView = findViewById(R.id.listViewDiscoveredDevice);
            listView.setVisibility(View.VISIBLE);

            // Assign adapter to ListView
            listView.setAdapter(dataAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // Ignored
                }
            });

            Button btnConnect = findViewById(R.id.btnConnect);
            btnConnect.setVisibility(View.VISIBLE);
        }
    }

    private void checkButtonClick() {


        Button btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                ArrayList<SelectedDevice> devices = dataAdapter.getDeviceList();
                for (int i = 0; i < devices.size(); i++) {
                    SelectedDevice device = devices.get(i);
                    if (device.isSelected() && !mapAdhocDevices.containsKey(device.getMacAddress())) {
                        mapAdhocDevices.put(device.getMacAddress(), device);
                        try {
                            transferManager.connect(device);
                            if (i != devices.size() - 1) {
                                Log.d(TAG, "WAIT");
                                Thread.sleep(WAIT);
                            }
                        } catch (DeviceException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void onStop() {
        try {
            transferManager.stopListening();
            if (server) {
                transferManager.removeWifiGroup(new ListenerAction() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Remove group");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "Failed to remove group");
                    }
                });

            }

            if (transferManager.isWifiEnabled()) {
                transferManager.disableWifi();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void accessLocationPermission() {
        int accessCoarseLocation = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFineLocation = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listRequestPermission = new ArrayList<String>();

        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listRequestPermission.isEmpty()) {
            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
            requestPermissions(strRequestPermission, REQUEST_CODE_LOC);
        } else {
            try {
                transferManager.discovery(initDiscoveryListener());
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOC:
                if (grantResults.length > 0) {
                    for (int gr : grantResults) {
                        // Check if request is granted or not
                        if (gr != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }

                    try {
                        transferManager.discovery(initDiscoveryListener());
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                return;
        }
    }

}