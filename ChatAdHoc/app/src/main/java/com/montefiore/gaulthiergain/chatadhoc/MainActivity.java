package com.montefiore.gaulthiergain.chatadhoc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.montefiore.gaulthiergain.adhoclibrary.appframework.AutoTransferManager;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.Config;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerAdapter;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerApp;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.BluetoothBadDuration;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.DeviceException;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.AdHocDevice;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "[ChatAdHoc]";
    private EditText editText;
    private AutoTransferManager autoTransferManager;

    private MessageAdapter messageAdapter;
    private ListView messagesView;

    private String color;
    private Set<String> broadcastId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editText);

        broadcastId = new HashSet<>();

        messageAdapter = new MessageAdapter(this);
        messagesView = findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

        Config config = new Config();
        config.setConnectionFlooding(true);
        autoTransferManager = new AutoTransferManager(true, config, initListener());

        try {
            // Start transfer manager process
            autoTransferManager.start(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (autoTransferManager.isWifiEnabled()) {
            try {
                autoTransferManager.disableWifi();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!autoTransferManager.isBluetoothEnabled()) {
            try {
                autoTransferManager.enableBluetooth(0, getApplicationContext(), new ListenerAdapter() {
                    @Override
                    public void onEnableBluetooth(boolean success) {
                        // Start the discovery
                        try {
                            autoTransferManager.startDiscovery();
                        } catch (DeviceException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onEnableWifi(boolean success) {
                        // ignored
                    }
                });
            } catch (BluetoothBadDuration e) {
                e.printStackTrace();
            }
        } else {
            // Start the discovery
            try {
                autoTransferManager.startDiscovery();
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        }


        color = getRandomColor();

        ImageButton send = findViewById(R.id.btnSend);
        send.setOnClickListener(this);
    }

    private void unpairDevices() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, DeviceException {
        /*HashMap<String, AdHocDevice> pairedDevices = autoTransferManager.getPairedDevices();
        if (pairedDevices.size() > 0) {
            for (AdHocDevice btDevice : pairedDevices.values()) {
                autoTransferManager.unpairBtDevice(btDevice);
            }
        }*/
    }

    private void updateMessageList(Message message) {
        messageAdapter.add(message);
        // scroll the ListView to the last added element
        messagesView.setSelection(messagesView.getCount() - 1);
    }

    private ListenerApp initListener() {
        return new ListenerApp() {

            @Override
            public void onReceivedData(final AdHocDevice adHocDevice, Object pdu) {
                final Message message = (Message) pdu;
                message.setBelongsToCurrentUser(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!broadcastId.contains(message.getBroadcastId())) {
                            updateMessageList(message);
                            try {
                                autoTransferManager.broadcastExcept(message, adHocDevice);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (DeviceException e) {
                                e.printStackTrace();
                            }
                            broadcastId.add(message.getBroadcastId());
                        } else {
                            Log.e(TAG, "Already receive this message " + message.toString());
                        }
                    }
                });
            }

            @Override
            public void onForwardData(AdHocDevice adHocDevice, Object pdu) {
                // Ignored
            }

            @Override
            public void onConnectionClosed(AdHocDevice adHocDevice) {
                if (adHocDevice.isDirectedConnected()) {
                    Log.d(TAG, "Connection closed with " + adHocDevice.getLabel() + "(" + adHocDevice.getDeviceName() + ")");
                    updateMessageList(new Message("Connection closed with " + adHocDevice.getDeviceName(), true));
                } else {
                    Log.d(TAG, "Device " + adHocDevice.getLabel() + "(" + adHocDevice.getDeviceName() + ") has left");
                    updateMessageList(new Message("Device " + adHocDevice.getDeviceName() + " has left", true));

                }
            }

            @Override
            public void onConnectionClosedFailed(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void processMsgException(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onConnection(AdHocDevice adHocDevice) {
                if (adHocDevice.isDirectedConnected()) {
                    Log.d(TAG, "Direct connection with " + adHocDevice.getLabel() + "(" + adHocDevice.getDeviceName() + ")");
                    updateMessageList(new Message("Connection with " + adHocDevice.getDeviceName(), true));
                } else {
                    Log.d(TAG, "Device " + adHocDevice.getLabel() + "(" + adHocDevice.getDeviceName() + ") joined");
                    updateMessageList(new Message("Device " + adHocDevice.getDeviceName() + " joined", true));
                }
            }

            @Override
            public void onConnectionFailed(Exception e) {
                Log.e(TAG, "Connection failed " + e.getMessage());
            }

        };
    }

    public void sendMessage() throws IOException, DeviceException {
        String txt = editText.getText().toString();
        if (txt.length() > 0) {

            Message message = new Message(txt,
                    new MemberData(autoTransferManager.getBluetoothAdapterName(), color), true);

            // Broadcast auto
            autoTransferManager.broadcast(message);

            broadcastId.add(message.getBroadcastId());

            updateMessageList(message);
            editText.getText().clear();
        }
    }

    private String getRandomColor() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder("#");
        while (sb.length() < 7) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:
                try {
                    sendMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        autoTransferManager.stopDiscovery();
        try {
            autoTransferManager.reset();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        finish();
    }
}
