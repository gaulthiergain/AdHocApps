package com.montefiore.gaulthiergain.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Runs all unit tests.
@RunWith(Suite.class)
@Suite.SuiteClasses({BluetoothInstrumentedTest.class, WifiInstrumentedTest.class,
        ServiceServerInstrumentedTest.class})
public class AllTests {
}
