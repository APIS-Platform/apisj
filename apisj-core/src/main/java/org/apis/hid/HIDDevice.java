
package org.apis.hid;

import org.apis.core.Transaction;
import org.apis.crypto.ECKey;
import org.apis.crypto.HashUtil;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.usb.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HIDDevice {
    private Logger logger = LoggerFactory.getLogger("hid");

    private UsbInterface dongleInterface;
    private UsbEndpoint in;
    private UsbEndpoint out;
    private byte transferBuffer[];
    private boolean debug = true;
    private boolean ledger;




    public HIDDevice(UsbDevice device) throws UsbException {
        UsbConfiguration configuration = device.getActiveUsbConfiguration();
        //UsbInterface dongleInterface = device.getInterface(0);
        UsbInterface dongleInterface = configuration.getUsbInterface((byte)0);
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
            ConsoleUtil.printlnGreen("HIDDevice => %s", ByteUtil.toHexString0x(command));
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
                pipeRead.syncSubmit(readData);
                ConsoleUtil.printlnPurple(ByteUtil.toHexString0x(readData));
                response.write(readData, 0, HID_BUFFER_SIZE);
            }

            if (debug) {
                ConsoleUtil.printlnGreen("HIDDevice <= %s", ByteUtil.toHexString0x(responseData));
                logger.debug("HIDDevice", "<= " + ByteUtil.toHexString0x(responseData));
            }
        } finally {
            pipeRead.close();
        }

        return responseData;
    }

    // https://github.com/LedgerHQ/ledgerjs/blob/master/packages/hw-transport/src/Transport.js
    public byte[] send(long cla, long ins, long p1, long p2, byte[] data) throws Exception {
        if(data.length >= 256) {
            throw new Exception("data.length exceed 256 bytes limit. Got: " + data.length);
        }

        ByteArrayOutputStream request = new ByteArrayOutputStream();
        request.write((byte)cla);
        request.write((byte)ins);
        request.write((byte)p1);
        request.write((byte)p2);
        request.write((byte)data.length);
        request.write(data);

        ConsoleUtil.printlnRed(ByteUtil.toHexString0x(request.toByteArray()));

        byte[] response = exchange(request.toByteArray());
        ConsoleUtil.printlnRed(ByteUtil.toHexString0x(response));

        int sw = ByteUtil.byteArrayToInt(Arrays.copyOfRange(response, response.length - 2, response.length));

        // sw를 이용한 에러 검출
        if(StateCode.isError(sw)) {
            throw new Exception(StateCode.stateMessage(sw));
        }

        return response;
    }

    // https://github.com/LedgerHQ/ledgerjs/blob/master/packages/hw-app-eth/src/Eth.js
    public byte[] getAddress(String path, boolean isDisplay, boolean isRequestChainCode) {
        List<Integer> paths = splitPath(path);

        byte[] buffer = new byte[1 + paths.size()*4];
        buffer[0] = (byte) paths.size();

        int startIdx = 1;
        for(int element : paths) {
            byte[] elementBytes = ByteUtil.intToBytes(element);
            for(byte elementByte : elementBytes) {
                buffer[startIdx] = elementByte;
                startIdx++;
            }
        }

        byte[] result = null;
        try {
            result = send(0xe0, 0x02, isDisplay?0x01:0x00, isRequestChainCode?0x01:0x00, buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ConsoleUtil.printlnRed(ByteUtil.toHexString0x(result));

        if(result == null) {
            return null;
        }
        return ByteUtil.hexStringToBytes(parseResponseAddress(result));
    }

    private String parseResponseAddress(byte[] data) {
        int publicKeyLength = data[0]&0xFF;
        int addressLength = data[1 + publicKeyLength]&0xFF;
        byte[] publicKey = Arrays.copyOfRange(data, 1, 1 + publicKeyLength);
        byte[] address = Arrays.copyOfRange(data, 1 + publicKeyLength + 1, 1 + publicKeyLength + 1 + addressLength);

        ConsoleUtil.printlnGreen("PUBL : " + publicKeyLength);
        ConsoleUtil.printlnGreen("ADDL : " + addressLength);
        ConsoleUtil.printlnGreen("PUB  : " + ByteUtil.toHexString0x(publicKey));
        ConsoleUtil.printlnGreen("ADDR : " + ByteUtil.toHexString0x(address));
        ConsoleUtil.printlnGreen("ADDR : " + new String(address));
        return new String(address);
    }


    public ECKey.ECDSASignature signTransaction(String path, byte[] rawTx) throws Exception {
        List<Integer> paths = splitPath(path);

        int offset = 0;
        List<byte[]> toSend = new ArrayList<>();
        while(offset != rawTx.length) {
            int maxChunkSize = offset == 0 ? 150 - 1 - paths.size()*4 : 150;
            int chunkSize = offset + maxChunkSize > rawTx.length ? rawTx.length - offset : maxChunkSize;

            byte[] buffer = new byte[offset == 0 ? 1 + paths.size()*4 + chunkSize : chunkSize];

            if(offset == 0) {
                buffer[0] = (byte) paths.size();
                int startIdx = 1;

                for(int element : paths) {
                    byte[] elementBytes = ByteUtil.intToBytes(element);
                    for(byte elementByte : elementBytes) {
                        buffer[startIdx] = elementByte;
                        startIdx++;
                    }
                }

                ByteArrayOutputStream mergedBuffer = new ByteArrayOutputStream();
                mergedBuffer.write(Arrays.copyOfRange(buffer, 0, 1 + paths.size()*4));
                mergedBuffer.write(Arrays.copyOfRange(rawTx, offset, offset + chunkSize));
                toSend.add(mergedBuffer.toByteArray());
            } else {
                toSend.add(Arrays.copyOfRange(rawTx, offset, offset + chunkSize));
            }
            offset += chunkSize;
        }


        for(int i = 0; i < toSend.size(); i++) {
            byte[] data = toSend.get(i);
            ConsoleUtil.printlnCyan(ByteUtil.toHexString0x(data));
            byte[] response = send(0xe0, 0x04, i == 0 ? 0x00 : 0x80, 0x00, data);

            byte v = response[0];
            byte[] r = Arrays.copyOfRange(response, 1, 1 + 32);
            byte[] s = Arrays.copyOfRange(response, 1 + 32, 1 + 32 + 32);

            ConsoleUtil.printlnBlue("렛저에서 받은 응답 : %s", ByteUtil.toHexString0x(response));
            ConsoleUtil.printlnBlue("V : " + ByteUtil.oneByteToHexString(v));
            ConsoleUtil.printlnBlue("R : " + ByteUtil.toHexString0x(r));
            ConsoleUtil.printlnBlue("S : " + ByteUtil.toHexString0x(s));

            ECKey.ECDSASignature aa = ECKey.ECDSASignature.fromComponents(r, s, v);

            return aa;
        }
        return null;
    }







    // https://github.com/LedgerHQ/ledgerjs/blob/master/packages/hw-app-eth/src/utils.js
    private List<Integer> splitPath(String path) {
        List<Integer> result = new ArrayList<>();
        String[] components = path.split("/");
        for(String element : components) {
            if(element.contains("'")) {
                element = element.replace("'", "");
                int number = Integer.parseInt(element) + 0x80000000;
                result.add(number);
            } else {
                result.add(Integer.parseInt(element));
            }
        }

        return result;
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
