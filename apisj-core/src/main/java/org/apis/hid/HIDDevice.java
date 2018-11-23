
package org.apis.hid;

import org.apis.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.usb.*;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class HIDDevice {
    private Logger logger = LoggerFactory.getLogger("hid");

    private UsbInterface dongleInterface;
    private UsbEndpoint in;
    private UsbEndpoint out;
    private byte transferBuffer[];
    private boolean debug;
    private boolean ledger;




    public HIDDevice(UsbDevice device) throws UsbException {
        UsbConfiguration configuration = device.getActiveUsbConfiguration();
        //UsbInterface dongleInterface = device.getInterface(0);
        UsbInterface dongleInterface = configuration.getUsbInterface((byte)1);
        dongleInterface.claim();
        UsbEndpoint in = null;
        UsbEndpoint out = null;
        List<UsbEndpoint> usbEndpointList = (List<UsbEndpoint>)dongleInterface.getUsbEndpoints();
        for (UsbEndpoint tmpEndpoint : usbEndpointList) {
            if (tmpEndpoint.getDirection() == UsbConst.ENDPOINT_DIRECTION_IN) {
                in = tmpEndpoint;
            } else {
                out = tmpEndpoint;
            }
        }

        this.dongleInterface = dongleInterface;
        this.in = in;
        this.out = out;

        transferBuffer = new byte[HID_BUFFER_SIZE];
    }

    public byte[] exchange(byte[] command) throws Exception {
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        byte[] responseData;
        int offset = 0;

        command = LedgerHelper.wrapCommandAPDU(LEDGER_DEFAULT_CHANNEL, command, HID_BUFFER_SIZE);
        if (debug) {
            logger.debug("HIDDevice", "=> " + ByteUtil.toHexString0x(command));
        }

        UsbPipe pipeWrite = out.getUsbPipe();
        try {
            pipeWrite.open();

            while (offset != command.length) {
                int blockSize = (command.length - offset > HID_BUFFER_SIZE ? HID_BUFFER_SIZE : command.length - offset);
                System.arraycopy(command, offset, transferBuffer, 0, blockSize);
                pipeWrite.syncSubmit(ByteBuffer.wrap(transferBuffer).array());

                offset += blockSize;
            }
        } finally {
            pipeWrite.close();
        }

        UsbPipe pipeRead = in.getUsbPipe();
        try {
            pipeRead.open();

            while ((responseData = LedgerHelper.unwrapResponseAPDU(LEDGER_DEFAULT_CHANNEL, response.toByteArray(), HID_BUFFER_SIZE)) == null) {
                byte[] readData = new byte[HID_BUFFER_SIZE];
                response.write(readData, 0, HID_BUFFER_SIZE);
            }

            if (debug) {
                logger.debug("HIDDevice", "<= " + ByteUtil.toHexString0x(responseData));
            }
        } finally {
            pipeRead.close();
        }

        return responseData;
    }

    public void close() throws Exception {
        dongleInterface.release();
    }

    public void setDebug(boolean debugFlag) {
        this.debug = debugFlag;
    }

    private static final int HID_BUFFER_SIZE = 64;
    private static final int LEDGER_DEFAULT_CHANNEL = 1;
    //private static final int SW1_DATA_AVAILABLE = 0x61;

}
