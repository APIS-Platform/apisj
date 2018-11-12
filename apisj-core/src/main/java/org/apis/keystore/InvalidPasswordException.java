package org.apis.keystore;

public class InvalidPasswordException extends Exception {
    @Override
    public String getMessage() {
        return "Invalid password";
    }
}