package com.montefiore.gaulthiergain.tests;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.montefiore.gaulthiergain.adhoclibrary.appframework.Config;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerAdapter;
import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerApp;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.BluetoothBadDuration;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.Service;
import com.montefiore.gaulthiergain.adhoclibrary.network.datalinkmanager.DataLinkManager;
import com.montefiore.gaulthiergain.adhoclibrary.network.datalinkmanager.ListenerDataLink;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataLinkBtInstrumentedTest {

    private Context appContext;
    private boolean mHasBluetooth;
    private DataLinkManager dataLinkManager;

    private ListenerApp listenerApp;
    private ListenerDataLink listenerDataLink;
    private int type = Service.BLUETOOTH;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getTargetContext();
        mHasBluetooth = appContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH);

        if (mHasBluetooth) {

            try {
                listenerApp = Mockito.mock(ListenerApp.class);
                listenerDataLink = Mockito.mock(ListenerDataLink.class);
                dataLinkManager = new DataLinkManager(true, appContext, new Config(),
                        listenerApp, listenerDataLink);

                WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null && wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            MockitoAnnotations.initMocks(this);
        }
    }

    @Test
    public void test01_useAppContext() {
        // Context of the app under test.
        assertEquals("com.montefiore.gaulthiergain.tests",
                appContext.getPackageName());
    }


    @Test
    public void test02_enableBtDataLink() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        if (BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_ON) {
            assertTrue(BluetoothAdapter.getDefaultAdapter().isEnabled());
            return;
        }

        ListenerAdapter listenerAdapter = Mockito.mock(ListenerAdapter.class);
        try {
            dataLinkManager.enable(0, appContext, type, listenerAdapter);
        } catch (BluetoothBadDuration bluetoothBadDuration) {
            bluetoothBadDuration.printStackTrace();
        }

        for (int i = 0; i < UtilTests.ENABLE_TIMEOUT / UtilTests.POLL_TIME; i++) {
            switch (BluetoothAdapter.getDefaultAdapter().getState()) {
                case BluetoothAdapter.STATE_ON:
                    assertTrue(dataLinkManager.isEnabled(type));
                    break;
            }
            UtilTests.sleep(UtilTests.POLL_TIME);
        }

        verify(listenerAdapter, times(1)).onEnableBluetooth(true);
    }

    @Test
    public void test03_checkBtConnectivity() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        assertTrue(dataLinkManager.isEnabled(type));
        assertFalse(dataLinkManager.isEnabled(Service.WIFI));
    }

}
