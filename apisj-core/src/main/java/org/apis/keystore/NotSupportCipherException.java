package org.apis.keystore;

public class NotSupportCipherException extends Exception {
    NotSupportCipherException(String msg) {
        super(msg);
    }
}