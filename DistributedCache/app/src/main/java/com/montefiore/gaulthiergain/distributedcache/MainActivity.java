package com.montefiore.gaulthiergain.distributedcache;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerApp;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.TransferManager;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.exceptions.BadServerPortException;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.DeviceException;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.AdHocDevice;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.DiscoveryListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[AdHoc][Main]";

    private static final int REQUEST_CODE_LOC = 1;
    private static final long WAIT = 2000;

    private static final int EVENT = 1;
    private static final int SEARCH = 2;
    private static final int RESULT = 3;
    private static final int MP3_CHOICE = 4;
    private static final int MP3_DATA = 5;

    public TextView songName, duration;
    private double timeElapsed = 0, finalTime = 0;
    private int forwardTime = 2000, backwardTime = 2000;
    private Handler durationHandler = new Handler();
    private SeekBar seekbar;

    private ProgressDialog dialog;
    private CustomAdapter dataAdapter;
    private TransferManager transferManager;

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ArrayList<String> allMp3 = new ArrayList<>();
    private ArrayList<String> listNames = new ArrayList<>();
    private ArrayList<AdHocDevice> allPeers = new ArrayList<>();
    private HashMap<String, File> mp3files = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            transferManager = new TransferManager(true, initListener());
            transferManager.getConfig().setJson(false);
            transferManager.getConfig().setConnectionFlooding(true);
            transferManager.start(getApplicationContext());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                accessLocationPermission();
            }

            Button btnScan = findViewById(R.id.btnScan);
            btnScan.setVisibility(View.VISIBLE);
            btnScan.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    try {
                        transferManager.discovery(initDiscoveryListener());
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }


                    dialog = ProgressDialog.show(MainActivity.this,
                            "Discovery in progress", "Please wait...", true);
                }
            });

            Button btnPaired = findViewById(R.id.btnPaired);
            btnPaired.setVisibility(View.VISIBLE);
            btnPaired.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    HashMap<String, AdHocDevice> mapAdhocDevices = transferManager.getPairedBluetoothDevices();
                    updateList(mapAdhocDevices);
                }
            });

            EditText editTextSearch = findViewById(R.id.editTextSearch);
            editTextSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (s.length() > 0) {
                        allMp3.clear();
                        listNames.clear();
                        for (AdHocDevice device : allPeers) {
                            try {
                                transferManager.sendMessageTo(new Message(SEARCH, s.toString()), device);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (DeviceException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        allMp3.clear();
                        listNames.clear();
                        updateListMp3();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            checkButtonClick();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                Message msg = (Message) pdu;
                Log.d(TAG, "Receive message: " + msg);
                switch (msg.getType()) {
                    case SEARCH: {
                        String searchMp3 = (String) msg.getPdu();

                        File directory = new File(String.valueOf(Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));

                        File[] files = directory.listFiles();

                        List<String> paths = searchMp3File(searchMp3, files);

                        try {
                            transferManager.sendMessageTo(new Message(RESULT, Build.MANUFACTURER, paths), adHocDevice);
                            Log.d(TAG, "Send paths: " + paths);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (DeviceException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case RESULT: {
                        final List<String> paths = (List) msg.getPdu();
                        if (paths == null || paths.size() == 0) {
                            Log.d(TAG, "Not Found");
                        } else {
                            for (int i = 0; i < paths.size(); i++) {
                                if (!allMp3.contains(paths.get(i))) {
                                    allMp3.add(paths.get(i));
                                    listNames.add(msg.getMobile());
                                }
                            }
                            updateListMp3();
                        }
                        break;
                    }
                    case MP3_CHOICE:

                        String askedMp3 = (String) msg.getPdu();

                        if (mp3files.containsKey(askedMp3)) {
                            try {
                                InputStream targetStream = new FileInputStream(mp3files.get(askedMp3));
                                Message msgToSend = new Message(MP3_DATA, new ObjectURI(askedMp3, targetStream, "mp3"));

                                transferManager.sendMessageTo(msgToSend, adHocDevice);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (DeviceException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            File directory = new File(String.valueOf(Environment
                                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));

                            File[] files = directory.listFiles();
                            File mp3File = getMp3File(askedMp3, files);

                            if (mp3File != null) {
                                try {
                                    InputStream targetStream = new FileInputStream(mp3File);
                                    Message msgToSend = new Message(MP3_DATA, new ObjectURI(mp3File.getName(), targetStream, "mp3"));

                                    transferManager.sendMessageTo(msgToSend, adHocDevice);
                                    Log.d(TAG, "Send mp3: " + msgToSend);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (DeviceException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        break;
                    case MP3_DATA:
                        try {
                            ObjectURI objectURI = (ObjectURI) msg.getPdu();

                            File tempMp3 = File.createTempFile(objectURI.getName(), objectURI.getExtension(), getCacheDir());
                            tempMp3.deleteOnExit();

                            mp3files.put(objectURI.getName(), tempMp3);

                            FileOutputStream fos = new FileOutputStream(tempMp3);
                            fos.write(objectURI.getInputData());
                            fos.close();

                            updateMP3gui(objectURI.getName(), tempMp3);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                }
            }

            @Override
            public void onForwardData(AdHocDevice adHocDevice, Object pdu) {
                Log.d(TAG, "Message forwarded by " + adHocDevice.toString() + " - " + pdu.toString());

                Message msg = (Message) pdu;
                if (msg.getType() == MP3_DATA) {
                    try {
                        ObjectURI objectURI = (ObjectURI) msg.getPdu();
                        File tempMp3 = File.createTempFile(objectURI.getName(), objectURI.getExtension(), getCacheDir());
                        tempMp3.deleteOnExit();

                        mp3files.put(objectURI.getName(), tempMp3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onConnectionClosed(AdHocDevice adHocDevice) {


                Log.d(TAG, "Disconnect with " + adHocDevice.getLabel() + "(" + adHocDevice.getDeviceName() + ")");
                Toast.makeText(getApplicationContext(), "Disconnect with device: " + adHocDevice.getDeviceName(),
                        Toast.LENGTH_SHORT).show();

                allPeers.remove(adHocDevice);

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


                if (!allPeers.contains(adHocDevice) && !transferManager.getConfig().getLabel().equals(adHocDevice.getLabel())) {
                    allPeers.add(adHocDevice);
                }

                if (allPeers.size() == 1) {
                    ViewFlipper vf = findViewById(R.id.viewFlipper);
                    vf.showNext();
                }


                for (AdHocDevice d : allPeers) {
                    Log.d(TAG, "ALL peers " + d);
                }
            }

            @Override
            public void onConnectionFailed(Exception e) {
                Log.e(TAG, "Connection failed: " + e.getMessage());
            }

        };
    }

    @Nullable
    private File getMp3File(String askedMp3, File[] files) {
        File mp3File = null;
        for (File file : files) {
            if (file.isDirectory()) {
                mp3File = getMp3File(askedMp3, file.listFiles());
                if (mp3File != null) {
                    break;
                }
            } else if (file.isFile() && file.getName().equals(askedMp3)) {
                mp3File = file;
                break;
            }
        }
        return mp3File;
    }

    @NonNull
    private List<String> searchMp3File(String searchMp3, File[] files) {
        List<String> paths = new ArrayList<>();
        for (File file : files) {

            if (file.isDirectory()) {
                paths.addAll(searchMp3File(searchMp3, file.listFiles()));
            } else if (file.isFile() && file.getName().toLowerCase().contains(searchMp3)) {
                paths.add(file.getName());
            }
        }
        return paths;
    }


    private void updateListMp3() {

        ListView lv = findViewById(R.id.listview);
        lv.setVisibility(View.VISIBLE);
        lv.setAdapter(new MyListAdaper(getApplicationContext(), R.layout.list_item, allMp3,
                listNames));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (AdHocDevice adHocDevice : allPeers) {
                    try {
                        if (mp3files.containsKey(allMp3.get(position))) {
                            File mp3 = mp3files.get(allMp3.get(position));
                            updateMP3gui(mp3.getName(), mp3);
                            break;
                        } else {
                            transferManager.sendMessageTo(new Message(MP3_CHOICE, allMp3.get(position)), adHocDevice);
                            Toast.makeText(MainActivity.this,
                                    "Requesting song. Please wait", Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (DeviceException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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

                boolean previous = false;
                ArrayList<SelectedDevice> devices = dataAdapter.getDeviceList();
                for (int i = 0; i < devices.size(); i++) {
                    SelectedDevice device = devices.get(i);
                    if (device.isSelected()) {
                        try {
                            if (previous) {
                                Log.d(TAG, "WAIT");
                                Thread.sleep(WAIT);
                            }
                            transferManager.connect(device);
                            previous = true;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void accessLocationPermission() {
        int accessCoarseLocation = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFineLocation = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        int accessReadHdd = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        int accessWriteHdd = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> listRequestPermission = new ArrayList<String>();

        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (accessReadHdd != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (accessWriteHdd != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listRequestPermission.isEmpty()) {
            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
            requestPermissions(strRequestPermission, REQUEST_CODE_LOC);
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
                }
                break;
            default:
                return;
        }
    }

    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            //get current position
            timeElapsed = mediaPlayer.getCurrentPosition();
            //set seekbar progress
            seekbar.setProgress((int) timeElapsed);
            //set time remaing
            double timeRemaining = finalTime - timeElapsed;
            duration.setText(String.format(Locale.ENGLISH,
                    "%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining), TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));

            //repeat yourself that again in 100 miliseconds
            durationHandler.postDelayed(this, 100);
        }
    };

    private void updateMP3gui(String name, File mp3) throws IOException {

        LinearLayout layout = findViewById(R.id.linearLayout3);
        layout.setVisibility(View.VISIBLE);

        mediaPlayer.reset();

        FileInputStream fis = new FileInputStream(mp3);
        mediaPlayer.setDataSource(fis.getFD());

        mediaPlayer.prepare();
        mediaPlayer.start();

        songName = findViewById(R.id.songName);
        finalTime = mediaPlayer.getDuration();
        duration = findViewById(R.id.songDuration);
        seekbar = findViewById(R.id.seekBar);
        songName.setText(name);

        seekbar.setMax((int) finalTime);
        seekbar.setClickable(false);

        timeElapsed = mediaPlayer.getCurrentPosition();
        seekbar.setProgress((int) timeElapsed);
        durationHandler.postDelayed(updateSeekBarTime, 100);

        ImageButton media_play = findViewById(R.id.media_play);
        media_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                timeElapsed = mediaPlayer.getCurrentPosition();
                seekbar.setProgress((int) timeElapsed);
                durationHandler.postDelayed(updateSeekBarTime, 100);
            }
        });

        ImageButton media_pause = findViewById(R.id.media_pause);
        media_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
            }
        });

        ImageButton media_rew = findViewById(R.id.media_rew);
        media_rew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if we can go back at backwardTime seconds after song starts
                if ((timeElapsed - backwardTime) > 0) {
                    timeElapsed = timeElapsed - backwardTime;

                    //seek to the exact second of the track
                    mediaPlayer.seekTo((int) timeElapsed);
                }
            }
        });

        ImageButton media_ff = findViewById(R.id.media_ff);
        media_ff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if we can go forward at forwardTime seconds before song endes
                if ((timeElapsed + forwardTime) <= finalTime) {
                    timeElapsed = timeElapsed + forwardTime;

                    //seek to the exact second of the track
                    mediaPlayer.seekTo((int) timeElapsed);
                }
            }
        });

        ImageButton media_stop = findViewById(R.id.media_stop);
        media_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                LinearLayout layout = findViewById(R.id.linearLayout3);
                layout.setVisibility(View.GONE);
            }
        });

    }

}