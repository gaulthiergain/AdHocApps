package com.montefiore.gaulthiergain.slidesadhoc;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerAdapter;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerApp;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.TransferManager;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.DeviceException;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.AdHocDevice;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TeacherActivity extends AppCompatActivity {

    private static final int BACK = 1;
    private static final int NEXT = 2;

    private static final int MIN = 0;
    private static final int MAX = 5;
    private static final String TAG = "[AdHoc]";

    private final String extension = ".jpg";

    private TransferManager transferManager;
    private int slide = 1;
    private Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        transferManager = ManagerHandler.getTransferManager(TeacherActivity.this);

        transferManager.updateListenerApp(initListener());

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
                }
            });
        }


        String name = transferManager.getWifiAdapterName();
        if (name == null) {
            try {
                transferManager.updateWifiAdapterName(MainActivity.GO);
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        } else if (!name.contains(MainActivity.GO)) {
            try {
                transferManager.updateWifiAdapterName(name + MainActivity.GO);
            } catch (DeviceException e) {
                e.printStackTrace();
            }
        }

        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateImageView(NEXT);
                try {
                    transferManager.broadcast("slide" + slide + extension);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DeviceException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnBack = findViewById(R.id.btnPrevious);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateImageView(BACK);
                try {
                    transferManager.broadcast("slide" + slide + extension);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DeviceException e) {
                    e.printStackTrace();
                }
            }
        });

        uri = getUri(R.drawable.slide1);
    }

    private void updateImageView(int type) {

        if (type == BACK) {
            slide--;
            if (slide == MIN) {
                slide = MAX - 1;
            }
        } else {
            slide++;
            if (slide == MAX) {
                slide = MIN + 1;
            }
        }

        ImageView img = findViewById(R.id.imageView);


        switch (slide) {
            case 1:
                img.setImageResource(R.drawable.slide1);
                uri = getUri(R.drawable.slide1);
                break;
            case 2:
                img.setImageResource(R.drawable.slide2);
                uri = getUri(R.drawable.slide2);
                break;
            case 3:
                img.setImageResource(R.drawable.slide3);
                uri = getUri(R.drawable.slide3);
                break;
            case 4:
                img.setImageResource(R.drawable.slide4);
                uri = getUri(R.drawable.slide4);
                break;
        }

    }

    private Uri getUri(int id) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + getResources().getResourcePackageName(id)
                + '/' + getResources().getResourceTypeName(id)
                + '/' + getResources().getResourceEntryName(id));
    }

    private ListenerApp initListener() {
        return new ListenerApp() {

            @Override
            public void onReceivedData(AdHocDevice adHocDevice, Object pdu) {
                Log.d(TAG, "Received from" + adHocDevice.getDeviceName());

                if (pdu instanceof String) {
                    if (uri != null) {
                        try {
                            ContentResolver cR = getApplication().getContentResolver();
                            ObjectURI objectURI = new ObjectURI("slide" + slide,
                                    cR.openInputStream(uri), extension);
                            Log.d(TAG, "Test: " + objectURI);
                            transferManager.sendMessageTo(objectURI, adHocDevice);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (DeviceException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, "Pdu is null");
                    }
                }

            }

            @Override
            public void onForwardData(AdHocDevice adHocDevice, Object pdu) {
                // Ignored
            }

            @Override
            public void onConnection(AdHocDevice adHocDevice) {
                Toast.makeText(getApplicationContext(), "Connected with " + adHocDevice.getDeviceName(), Toast.LENGTH_SHORT).show();
                try {
                    transferManager.sendMessageTo("slide" + slide + extension, adHocDevice);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DeviceException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionFailed(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onConnectionClosed(AdHocDevice adHocDevice) {
                Toast.makeText(getApplicationContext(), "Connection Closed with " + adHocDevice.getDeviceName(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onStop() {
        super.onStop();
        try {
            transferManager.resetWifiAdapterName();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }
}
