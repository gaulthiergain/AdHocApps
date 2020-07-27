package com.montefiore.gaulthiergain.tests;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerAdapter;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.bluetooth.BluetoothAdHocDevice;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.bluetooth.BluetoothAdHocManager;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.BluetoothBadDuration;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.exceptions.DeviceException;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.AdHocDevice;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.DiscoveryListener;
import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.Service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
public class BluetoothInstrumentedTest {

    private Context appContext;
    private boolean mHasBluetooth;
    private BluetoothAdHocManager bluetoothAdHocManager;

    @Before
    public void setUp() {
        appContext = InstrumentationRegistry.getTargetContext();
        mHasBluetooth = appContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH);

        if (mHasBluetooth) {

            MockitoAnnotations.initMocks(this);

            bluetoothAdHocManager = new BluetoothAdHocManager(true, appContext);
        }
    }

    @Test
    public void test01_useAppContext() {
        // Context of the app under test.
        assertEquals("com.montefiore.gaulthiergain.tests",
                appContext.getPackageName());
    }

    @Test
    public void test02_getDefaultAdapter() {
        if (mHasBluetooth) {
            assertNotNull(BluetoothAdapter.getDefaultAdapter());
        } else {
            assertNull(BluetoothAdapter.getDefaultAdapter());
        }
    }

    @Test
    public void test03_enableBluetooth() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        if (BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_ON) {
            assertTrue(BluetoothAdapter.getDefaultAdapter().isEnabled());
            return;
        }

        ListenerAdapter listenerAdatper = Mockito.mock(ListenerAdapter.class);
        bluetoothAdHocManager.onEnableBluetooth(listenerAdatper);

        bluetoothAdHocManager.enable();

        for (int i = 0; i < UtilTests.ENABLE_TIMEOUT / UtilTests.POLL_TIME; i++) {
            switch (BluetoothAdapter.getDefaultAdapter().getState()) {
                case BluetoothAdapter.STATE_ON:
                    assertTrue(BluetoothAdapter.getDefaultAdapter().isEnabled());
                    break;
            }
            UtilTests.sleep(UtilTests.POLL_TIME);
        }

        verify(listenerAdatper, times(1)).onEnableBluetooth(true);
        bluetoothAdHocManager.unregisterAdapter();
    }

    @Test
    public void test04_badBluetoothDuration1() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        try {
            bluetoothAdHocManager.enableDiscovery(appContext, -20);
            Assert.fail("Should have thrown BluetoothBadDuration");
        } catch (BluetoothBadDuration bluetoothBadDuration) {
            //Success
        }
    }

    @Test
    public void test05_badBluetoothDuration2() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        try {
            bluetoothAdHocManager.enableDiscovery(appContext, 4998);
            Assert.fail("Should have thrown BluetoothBadDuration");
        } catch (BluetoothBadDuration bluetoothBadDuration) {
            //Success
        }
    }

    @Test
    public void test06_goodBluetoothDuration() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.getState() == BluetoothAdapter.STATE_OFF) {
            assertFalse(adapter.isEnabled());
            return;
        }

        try {
            bluetoothAdHocManager.enableDiscovery(appContext, 0);
        } catch (BluetoothBadDuration bluetoothBadDuration) {
            //Ignored
        }

        for (int i = 0; i < UtilTests.ENABLE_TIMEOUT / UtilTests.POLL_TIME; i++) {
            switch (adapter.getState()) {
                case BluetoothAdapter.STATE_ON:
                    assertTrue(BluetoothAdapter.getDefaultAdapter().isEnabled());
                    return;
            }
            UtilTests.sleep(UtilTests.POLL_TIME);
        }
    }

    @Test
    public void test07_getName() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        String name = bluetoothAdHocManager.getAdapterName();
        assertNotNull(name);
    }

    @Test
    public void test08_updateName() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        String name = bluetoothAdHocManager.getAdapterName();
        assertNotNull(name);
        bluetoothAdHocManager.updateDeviceName("NewName");
        UtilTests.sleep(UtilTests.WAIT_TIME);
        assertEquals(bluetoothAdHocManager.getAdapterName(), "NewName");
        bluetoothAdHocManager.updateDeviceName(name);
        UtilTests.sleep(UtilTests.WAIT_TIME);
        assertEquals(bluetoothAdHocManager.getAdapterName(), name);
    }

    @Test
    public void test09_nameBondedDevices() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        HashMap<String, BluetoothAdHocDevice> pairedDevices = bluetoothAdHocManager.getPairedDevices();
        assertNotNull(pairedDevices);
        for (BluetoothAdHocDevice device : pairedDevices.values()) {
            assertNotNull(device.getDeviceName());
        }
    }

    @Test
    public void test10_macBondedDevices() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        HashMap<String, BluetoothAdHocDevice> pairedDevices = bluetoothAdHocManager.getPairedDevices();
        assertNotNull(pairedDevices);
        for (BluetoothAdHocDevice device : pairedDevices.values()) {
            assertTrue(UtilTests.validateMacAddress(device.getMacAddress()));
        }
    }

    @Test
    public void test11_uUIDBondedDevices() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        HashMap<String, BluetoothAdHocDevice> pairedDevices = bluetoothAdHocManager.getPairedDevices();
        assertNotNull(pairedDevices);
        for (BluetoothAdHocDevice device : pairedDevices.values()) {
            assertTrue(UtilTests.validateUUIDAddress(device.getUuid()));
        }
    }

    @Test
    public void test12_labelBondedDevices() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        HashMap<String, BluetoothAdHocDevice> pairedDevices = bluetoothAdHocManager.getPairedDevices();
        assertNotNull(pairedDevices);
        for (BluetoothAdHocDevice device : pairedDevices.values()) {
            assertNull(device.getLabel());
        }
    }

    @Test
    public void test13_typeBondedDevices() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        HashMap<String, BluetoothAdHocDevice> pairedDevices = bluetoothAdHocManager.getPairedDevices();
        assertNotNull(pairedDevices);
        for (BluetoothAdHocDevice device : pairedDevices.values()) {
            assertEquals(device.getType(), Service.BLUETOOTH);
        }
    }

    @Test
    public void test14_rssiBondedDevices() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        HashMap<String, BluetoothAdHocDevice> pairedDevices = bluetoothAdHocManager.getPairedDevices();
        assertNotNull(pairedDevices);
        for (BluetoothAdHocDevice device : pairedDevices.values()) {
            assertEquals(device.getRssi(), -1);
        }
    }

    @Test
    public void test15_removeBondedDevices() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        HashMap<String, BluetoothAdHocDevice> pairedDevices = bluetoothAdHocManager.getPairedDevices();
        assertNotNull(pairedDevices);
        for (BluetoothAdHocDevice device : pairedDevices.values()) {
            try {
                bluetoothAdHocManager.unpairDevice(device);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        pairedDevices = bluetoothAdHocManager.getPairedDevices();
        assertNotNull(pairedDevices);
        assertEquals(pairedDevices.size(), 0);
    }

    @Test
    public void test16_discovery() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }
        DiscoveryListener discoveryListener = Mockito.mock(DiscoveryListener.class);

        bluetoothAdHocManager.discovery(discoveryListener);
        UtilTests.sleep(UtilTests.WAIT_TIME);
        verify(discoveryListener, times(1)).onDiscoveryStarted();
        UtilTests.sleep(UtilTests.DISCOVERY_TIME);
        verify(discoveryListener, times(1)).onDiscoveryCompleted((HashMap<String, AdHocDevice>) any());
        UtilTests.sleep(UtilTests.WAIT_TIME);
        bluetoothAdHocManager.unregisterDiscovery();
    }

    @Test
    public void test17_disableBluetooth() {

        if (!mHasBluetooth) {
            // Skip the test if bluetooth is not present.
            return;
        }

        if (BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_OFF) {
            assertFalse(BluetoothAdapter.getDefaultAdapter().isEnabled());
            return;
        }

        ListenerAdapter listenerAdatper = Mockito.mock(ListenerAdapter.class);
        bluetoothAdHocManager.onEnableBluetooth(listenerAdatper);
        bluetoothAdHocManager.disable();

        for (int i = 0; i < UtilTests.DISABLE_TIMEOUT / UtilTests.POLL_TIME; i++) {
            switch (BluetoothAdapter.getDefaultAdapter().getState()) {
                case BluetoothAdapter.STATE_OFF:
                    assertFalse(BluetoothAdapter.getDefaultAdapter().isEnabled());
                    break;
            }
            UtilTests.sleep(UtilTests.POLL_TIME);
        }

        verify(listenerAdatper, times(1)).onEnableBluetooth(false);
        bluetoothAdHocManager.unregisterAdapter();
    }
}
