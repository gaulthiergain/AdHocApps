package com.montefiore.gaulthiergain.distributedcache;


import com.montefiore.gaulthiergain.adhoclibrary.datalink.service.AdHocDevice;

/**
 * <p>This class represents a Selected device in the discovery listView.</p>
 *
 * @author Gaulthier Gain
 * @version 1.0
 */
public class SelectedDevice extends AdHocDevice {

    private boolean selected;

    SelectedDevice(AdHocDevice device) {
        super(device.getMacAddress(), device.getDeviceName(), device.getType());
        this.selected = false;
    }

    public SelectedDevice(String address, String name, byte type) {
        super(address, name, type);
        this.selected = false;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return deviceName + " (" + macAddress + ") - " + display(type);
    }
}
