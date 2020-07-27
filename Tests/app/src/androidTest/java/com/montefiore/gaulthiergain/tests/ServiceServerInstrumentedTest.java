package com.montefiore.gaulthiergain.tests;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.montefiore.gaulthiergain.adhoclibrary.datalink.bluetooth.BluetoothServer;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.Service;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.ServiceConfig;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.ServiceMessageListener;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.ServiceServer;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.wifi.WifiServer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServiceServerInstrumentedTest {

    private Context appContext;
    private boolean mHasBluetooth;
    private boolean mHasWifiDirect;

    private ServiceServer serviceServer;
    private ServiceMessageListener messageListener;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getTargetContext();
        mHasBluetooth = appContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH);
        mHasWifiDirect = appContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_WIFI_DIRECT);

        if (mHasBluetooth && mHasWifiDirect) {

            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                BluetoothAdapter.getDefaultAdapter().enable();
                UtilTests.sleep(UtilTests.WAIT_TIME);
            }

            WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && !wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
                UtilTests.sleep(UtilTests.WAIT_TIME);
            }

            MockitoAnnotations.initMocks(this);

            messageListener = Mockito.mock(ServiceMessageListener.class);
        }
    }

    @Test
    public void test01_useAppContext() {
        // Context of the app under test.
        assertEquals("com.montefiore.gaulthiergain.tests",
                appContext.getPackageName());
    }

    @Test
    public void test02_btNoListening() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        ServiceConfig serviceConfig = new ServiceConfig((short) 0,
                true, BluetoothAdapter.getDefaultAdapter(), UUID.randomUUID());
        try {
            serviceServer = new BluetoothServer(true, true,
                    messageListener);
            assertEquals(serviceServer.getState(), Service.STATE_NONE);
            serviceServer.listen(serviceConfig);
            assertEquals(serviceServer.getState(), Service.STATE_NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test03_btListen() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        ServiceConfig serviceConfig = new ServiceConfig((short) 3,
                true, BluetoothAdapter.getDefaultAdapter(), UUID.randomUUID());
        try {
            serviceServer = new BluetoothServer(true, true,
                    messageListener);
            serviceServer.listen(serviceConfig);
            assertEquals(serviceServer.getState(), Service.STATE_LISTENING);
            serviceServer.stopListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test04_btStoplistenning() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        ServiceConfig serviceConfig = new ServiceConfig((short) 3,
                true, BluetoothAdapter.getDefaultAdapter(), UUID.randomUUID());
        try {
            serviceServer = new BluetoothServer(true, true,
                    messageListener);
            serviceServer.listen(serviceConfig);
            assertEquals(serviceServer.getState(), Service.STATE_LISTENING);
            serviceServer.stopListening();
            assertEquals(serviceServer.getState(), Service.STATE_NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test05_btListenError1() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        ServiceConfig serviceConfig = new ServiceConfig((short) 3, true, null,
                UUID.randomUUID());
        try {
            serviceServer = new BluetoothServer(true, true,
                    messageListener);
            serviceServer.listen(serviceConfig);
            Assert.fail("Must throw exception");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test06_btListenError2() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        ServiceConfig serviceConfig = new ServiceConfig((short) 3,
                true, BluetoothAdapter.getDefaultAdapter(), null);
        try {
            serviceServer = new BluetoothServer(true, true,
                    messageListener);
            serviceServer.listen(serviceConfig);
            Assert.fail("Must throw exception");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test07_WifiNoListening() {

        if (!mHasWifiDirect) {
            // Skip the test if wifi direct is not present.
            return;
        }

        ServiceConfig serviceConfig = new ServiceConfig((short) 0, 52000);
        try {
            serviceServer = new WifiServer(true, true,
                    messageListener);
            assertEquals(serviceServer.getState(), Service.STATE_NONE);
            serviceServer.listen(serviceConfig);
            assertEquals(serviceServer.getState(), Service.STATE_NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test08_WifiListening() {

        if (!mHasWifiDirect) {
            // Skip the test if wifi direct is not present.
            return;
        }

        ServiceConfig serviceConfig = new ServiceConfig((short) 2, 52000);
        try {
            serviceServer = new WifiServer(true, true,
                    messageListener);
            assertEquals(serviceServer.getState(), Service.STATE_NONE);
            serviceServer.listen(serviceConfig);
            assertEquals(serviceServer.getState(), Service.STATE_LISTENING);
            serviceServer.stopListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test09_StopWifiListening() {

        if (!mHasWifiDirect) {
            // Skip the test if wifi direct is not present.
            return;
        }

        ServiceConfig serviceConfig = new ServiceConfig((short) 2, 52000);
        try {
            serviceServer = new WifiServer(true, true,
                    messageListener);
            assertEquals(serviceServer.getState(), Service.STATE_NONE);
            serviceServer.listen(serviceConfig);
            assertEquals(serviceServer.getState(), Service.STATE_LISTENING);
            serviceServer.stopListening();
            assertEquals(serviceServer.getState(), Service.STATE_NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test10_WifiListeningError1() {

        if (!mHasWifiDirect) {
            // Skip the test if wifi direct is not present.
            return;
        }

        ServiceConfig serviceConfig = new ServiceConfig((short) 2, -1);
        try {
            serviceServer = new WifiServer(true, true,
                    messageListener);
            assertEquals(serviceServer.getState(), Service.STATE_NONE);
            serviceServer.listen(serviceConfig);
            fail("Must thrown an exception");
        } catch (IOException e) {
            // ignored
        } catch (IllegalArgumentException e) {
            // ignored
        }
    }

}
