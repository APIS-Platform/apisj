package org.apis.hid;

import org.apis.config.SystemProperties;
import org.apis.core.Transaction;
import org.apis.crypto.ECKey;
import org.apis.util.ByteUtil;
import org.apis.util.ConsoleUtil;
import org.apis.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.usb.*;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HIDDevice {
    private Logger logger = LoggerFactory.getLogger("hid");

    private UsbInterface dongleInterface;
    private UsbEndpoint in;
    private UsbEndpoint out;
    private byte transferBuffer[];
    private boolean debug = false;




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

        byte[] response = exchange(request.toByteArray());

        int sw = ByteUtil.byteArrayToInt(Arrays.copyOfRange(response, response.length - 2, response.length));

        // sw를 이용한 에러 검출
        if(StateCode.isError(sw)) {
            throw new Exception(StateCode.stateMessage(sw));
        }
        ConsoleUtil.printlnRed(ByteUtil.toHexString(response));

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


        if(debug) {
            String log = "Received PublicKey : " + ByteUtil.toHexString0x(publicKey) + "\n";
            log += "Received Address : " + ByteUtil.toHexString(address);
            ConsoleUtil.printlnGreen(log);
        }
        return new String(address);
    }


    public byte[] signTransaction(String path, Transaction tx, byte[] address) throws Exception {
        List<Integer> paths = splitPath(path);

        byte[] encodedRaw = tx.getEncodedRaw();

        int offset = 0;
        List<byte[]> toSend = new ArrayList<>();
        while(offset != encodedRaw.length) {
            int maxChunkSize =
                    offset == 0
                            ? 150 - 1 - paths.size()*4
                            : 150;
            int chunkSize =
                    offset + maxChunkSize > encodedRaw.length
                            ? encodedRaw.length - offset
                            : maxChunkSize;

            byte[] buffer = new byte[
                    offset == 0
                            ? 1 + paths.size()*4 + chunkSize
                            : chunkSize];

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
                mergedBuffer.write(Arrays.copyOfRange(encodedRaw, offset, offset + chunkSize));
                toSend.add(mergedBuffer.toByteArray());
            } else {
                toSend.add(Arrays.copyOfRange(encodedRaw, offset, offset + chunkSize));
            }
            offset += chunkSize;
        }

        byte[] response = null;
        for(int i = 0; i < toSend.size(); i++) {
            byte[] data = toSend.get(i);
            response = send(0xe0, 0x04, i == 0 ? 0x00 : 0x80, 0x00, data);
        }
        if(response == null || response.length == 0) {
            return null;
        }

        byte v = response[0];
        byte[] r = Arrays.copyOfRange(response, 1, 1 + 32);
        byte[] s = Arrays.copyOfRange(response, 1 + 32, 1 + 32 + 32);

        ECKey.ECDSASignature sig = new ECKey.ECDSASignature(ByteUtil.bytesToBigInteger(r), ByteUtil.bytesToBigInteger(s));
        for(int i = 0; i < 2; i++) {
            byte[] computeAddress = ECKey.recoverAddressFromSignature(i, sig, tx.getRawHash());
            if(computeAddress != null && FastByteComparisons.equal(computeAddress, address)) {
                sig.v = (byte) (27 + i);
                break;
            }
        }

        Transaction signedTx = new Transaction(tx.getNonce(), tx.getGasPrice(), tx.getGasLimit(), tx.getReceiveAddress(), new String(tx.getReceiveMask(), Charset.forName("UTF-8")), tx.getValue(), tx.getData(), sig.r.toByteArray(), sig.s.toByteArray(), sig.v, SystemProperties.getDefault().networkId());

        return signedTx.getEncoded();
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
