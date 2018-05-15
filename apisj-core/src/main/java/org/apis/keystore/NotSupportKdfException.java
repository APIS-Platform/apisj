package org.apis.keystore;

public class NotSupportKdfException extends Exception {
    NotSupportKdfException(String msg) {
        super(msg);
    }
}