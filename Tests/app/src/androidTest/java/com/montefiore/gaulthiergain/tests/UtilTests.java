package com.montefiore.gaulthiergain.tests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class UtilTests {

    static final int WAIT_TIME = 1500;
    static final int DISCOVERY_TIME = 15000;
    static final int DISABLE_TIMEOUT = 5000;
    static final int ENABLE_TIMEOUT = 10000;
    static final int POLL_TIME = 100;

    static void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static boolean validateMacAddress(String mac) {
        Pattern p = Pattern.compile("^([0-9A-Fa-f]{2}[:]){5}([0-9A-Fa-f]{2})$");
        Matcher m = p.matcher(mac);
        return m.find();
    }

    static boolean validateUUIDAddress(String uuid) {
        Pattern p = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        Matcher m = p.matcher(uuid);
        return m.find();
    }
}
