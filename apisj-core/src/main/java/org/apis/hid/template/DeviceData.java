package org.apis.hid.template;

import javax.usb.UsbDevice;

public class DeviceData {
    private short productId;
    private short vendorId;
    private UsbDevice device;

    public DeviceData(short productId, short vendorId, UsbDevice device) {
        this.productId = productId;
        this.vendorId = vendorId;
        this.device = device;
    }


    public UsbDevice getDevice() {
        return device;
    }

    public short getProductId() {
        return productId;
    }

    public short getVendorId() {
        return vendorId;
    }

    @Override
    public String toString() {
        return "DeviceData{" +
                ", productId=" + productId +
                ", vendorId=" + vendorId +
                '}';
    }
}
