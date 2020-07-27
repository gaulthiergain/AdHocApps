package com.montefiore.gaulthiergain.tests;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerAdapter;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.DeviceException;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.AdHocDevice;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.DiscoveryListener;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.wifi.ConnectionWifiListener;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.wifi.WifiAdHocManager;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WifiInstrumentedTest {


    private Context appContext;
    private boolean mHasWifiDirect;
    private WifiAdHocManager wifiAdHocManager;

    private ConnectionWifiListener connectionWifiListener;
    private WifiAdHocManager.WifiDeviceInfosListener wifiDeviceInfosListener;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getTargetContext();
        mHasWifiDirect = appContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_WIFI_DIRECT);

        if (mHasWifiDirect) {
            MockitoAnnotations.initMocks(this);
            connectionWifiListener = Mockito.mock(ConnectionWifiListener.class);
            wifiDeviceInfosListener = Mockito.mock(WifiAdHocManager.WifiDeviceInfosListener.class);
                /*wifiAdHocManager = new WifiAdHocManager(true, appContext,
                        wifiDeviceInfosListener, connectionWifiListener);*/
        }
    }

    @Test
    public void test01_useAppContext() {
        // Context of the app under test.
        assertEquals("com.montefiore.gaulthiergain.tests",
                appContext.getPackageName());
    }

    @Test
    public void test02_enableWifi() {

        if (!mHasWifiDirect) {
            // Skip the test if wifi is not present.
            return;
        }

        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            assertTrue(wifiManager.isWifiEnabled());
            return;
        }

        ListenerAdapter listenerAdapter = Mockito.mock(ListenerAdapter.class);
        wifiAdHocManager.onEnableWifi(listenerAdapter);
        wifiAdHocManager.enable();

        UtilTests.sleep(UtilTests.WAIT_TIME);

        verify(listenerAdapter, times(1)).onEnableWifi(true);
    }

    @Test
    public void test03_getName() {

        if (!mHasWifiDirect) {
            // Skip the test if bluetooth is not present.
            return;
        }

        UtilTests.sleep(UtilTests.WAIT_TIME);
        verify(wifiDeviceInfosListener, times(1)).getDeviceInfos((String) any(), (String) any());

        String name = wifiAdHocManager.getAdapterName();
        assertNotNull(name);
    }

    @Test
    public void test04_updateName() {

        if (!mHasWifiDirect) {
            // Skip the test if bluetooth is not present.
            return;
        }

        UtilTests.sleep(UtilTests.WAIT_TIME);
        verify(wifiDeviceInfosListener, times(1)).getDeviceInfos((String) any(), (String) any());

        String name = wifiAdHocManager.getAdapterName();
        assertNotNull(name);
        wifiAdHocManager.updateDeviceName("NewName");
        UtilTests.sleep(UtilTests.WAIT_TIME);
        assertEquals(wifiAdHocManager.getAdapterName(), "NewName");
        wifiAdHocManager.updateDeviceName(name);
        UtilTests.sleep(UtilTests.WAIT_TIME);
        assertEquals(wifiAdHocManager.getAdapterName(), name);
    }


    @Test
    public void test05_discovery() {

        if (!mHasWifiDirect) {
            // Skip the test if bluetooth is not present.
            return;
        }
        DiscoveryListener discoveryListener = Mockito.mock(DiscoveryListener.class);

        wifiAdHocManager.discovery(discoveryListener);
        UtilTests.sleep(UtilTests.WAIT_TIME);
        verify(discoveryListener, times(1)).onDiscoveryStarted();
        UtilTests.sleep(UtilTests.DISCOVERY_TIME);
        verify(discoveryListener, times(1)).onDiscoveryCompleted((HashMap<String, AdHocDevice>) any());
        UtilTests.sleep(UtilTests.WAIT_TIME);
        wifiAdHocManager.unregisterDiscovery();
    }

    @Test
    public void test06_disableWifi() {

        if (!mHasWifiDirect) {
            // Skip the test if wifi is not present.
            return;
        }

        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            assertFalse(wifiManager.isWifiEnabled());
            return;
        }

        wifiAdHocManager.disable();
        UtilTests.sleep(UtilTests.WAIT_TIME);
        assertFalse(WifiAdHocManager.isWifiEnabled(appContext));
    }
}
