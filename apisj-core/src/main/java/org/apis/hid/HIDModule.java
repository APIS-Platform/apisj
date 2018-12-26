package org.apis.hid;



import org.apis.hid.template.DeviceData;

import javax.usb.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HIDModule {

    private static HIDModule sHIDModule = null;

    private final HashMap<String, HIDDevice> hidDevices = new HashMap<>();

    private UsbHub usbHub;

    private static final short VENDOR_ID1 = 0x2581;
    private static final short VENDOR_ID2 = 0x2c97;
    private static final short PRODUCT_ID = 0x3b7c;

    private List<DeviceData> mDeviceDataList = new ArrayList<>();

    public static HIDModule getInstance() {
        if(sHIDModule == null) {
            sHIDModule = new HIDModule();
        }
        return sHIDModule;
    }

    private HIDModule() {
        try {
            UsbServices services = UsbHostManager.getUsbServices();
            usbHub = services.getRootUsbHub();
        } catch (UsbException e) {
            e.printStackTrace();
        }
    }

    private List<UsbDevice> findDevice(UsbHub hub, List<UsbDevice> devices)
    {
        if(devices == null) {
            devices = new ArrayList<>();
        }

        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if((desc.idVendor() == VENDOR_ID1 && desc.idProduct() == PRODUCT_ID) || desc.idVendor() == VENDOR_ID2) {
                devices.add(device);
            }
            if (device.isUsbHub())
            {
                findDevice((UsbHub) device, devices);
                return devices;
            }
        }
        return devices;
    }

    public void loadDeviceList() {
        List<DeviceData> deviceDataList = new ArrayList<>();

        List<UsbDevice> devices = findDevice(usbHub, null);
        for(UsbDevice device : devices) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            deviceDataList.add(new DeviceData(desc.idProduct(), desc.idVendor(), device));
        }

        mDeviceDataList = deviceDataList;
    }

    public List<DeviceData> getDeviceList() {
        return mDeviceDataList;
    }

    private UsbDevice getDevice(int index) {
        if(index < 0 || index >= mDeviceDataList.size()) {
            return null;
        }
        else {
            return mDeviceDataList.get(index).getDevice();
        }
    }


    public void openDevice(int index) throws UsbException, IOException {
        UsbDevice device = getDevice(index);
        createHIDDevice(device);
    }


    public void exchange(String deviceId, String value) {
        try {
            HIDDevice hid = hidDevices.get(deviceId);
            if (hid == null) {
                throw new Exception(String.format("No device opened for the id '%s'", deviceId));
            }
            hid.exchange(hexToBin(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeDevice(String deviceId) {
        try {
            HIDDevice hid = hidDevices.get(deviceId);
            if (hid == null) {
                throw new Exception(String.format("No device opened for the id '%s'", deviceId));
            }
            hid.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] hexToBin(String src) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int i = 0;
        while (i < src.length()) {
            char x = src.charAt(i);
            if (!((x >= '0' && x <= '9') || (x >= 'A' && x <= 'F') || (x >= 'a' && x <= 'f'))) {
                i++;
                continue;
            }
            try {
                result.write(Integer.valueOf("" + src.charAt(i) + src.charAt(i + 1), 16));
                i += 2;
            } catch (Exception e) {
                return null;
            }
        }
        return result.toByteArray();
    }


    private void createHIDDevice(UsbDevice device) throws IOException, UsbException {
        HIDDevice hid = new HIDDevice(device);
        hid.setDebug(true);
        String id = generateId();
        hidDevices.put(id, hid);
    }

    private static final String ACTION_USB_PERMISSION = "com.ledgerwallet.hid.USB_PERMISSION";


    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
