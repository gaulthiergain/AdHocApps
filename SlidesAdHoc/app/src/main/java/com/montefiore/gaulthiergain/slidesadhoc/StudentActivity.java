package com.montefiore.gaulthiergain.slidesadhoc;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerAdapter;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerApp;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.TransferManager;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.DeviceException;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.AdHocDevice;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class StudentActivity extends AppCompatActivity {

    private static final String TAG = "[AdHoc]";

    private TransferManager transferManager;
    private static final String GO = "[GO]";

    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        filename = Environment.getExternalStorageDirectory() + "/"
                + getApplicationContext().getPackageName();

        if (setupFolder()) {
            Log.d(TAG, filename + " is created");
        }

        transferManager = ManagerHandler.getTransferManager(getApplicationContext());

        transferManager.updateListenerApp(initListener());

    }

    private boolean setupFolder() {
        final File dirs = new File(filename);

        if (!dirs.exists()) {
            return dirs.mkdirs();
        } else {
            String[] files;
            files = dirs.list();
            for (String file : files) {
                File slides = new File(dirs, file);
                if (slides.delete()) {
                    System.out.println("file Deleted :" + file);
                } else {
                    System.out.println("file not Deleted :" + file);
                }
            }
        }

        return true;
    }

    private String addFile(ObjectURI data) {
        //ObjectURI data = (ObjectURI) messageAdHoc.getPdu();

        Log.d(TAG, "EXTENSION: " + data.getExtension());

        final File f = new File(filename + "/wifip2pshared-" + data.getName()
                + data.getExtension());

        try {
            if (!f.createNewFile())
                return null;

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
            bos.write(data.getInputData());
            bos.flush();
            bos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return f.getAbsolutePath();
    }

    private ListenerApp initListener() {
        return new ListenerApp() {

            @Override
            public void onReceivedData(AdHocDevice adHocDevice, Object pdu) {

                if (pdu instanceof String) {
                    File myFile = new File(filename + "/wifip2pshared-" + pdu);
                    if (myFile.exists()) {
                        Log.d(TAG, "this file already exist");
                        displayImage(myFile.getAbsolutePath());
                    } else {
                        try {
                            transferManager.sendMessageTo("EMPTY", adHocDevice);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (DeviceException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.d(TAG, "Received Uri");
                    ObjectURI objectURI = (ObjectURI) pdu;
                    String absolutePath = addFile(objectURI);
                    if (absolutePath == null) {
                        Toast.makeText(getApplicationContext(), "ERROR WHILE TRANSFERING FILE", Toast.LENGTH_LONG).show();
                    } else {
                        displayImage(absolutePath);
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

    private void displayImage(String absolutePath) {
        Bitmap myBitmap = BitmapFactory.decodeFile(absolutePath);
        ImageView imgview = findViewById(R.id.imagePreview);
        imgview.setImageBitmap(myBitmap);
        imgview.setScaleType(ImageView.ScaleType.FIT_XY);
    }
}
