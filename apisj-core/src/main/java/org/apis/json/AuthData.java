package org.apis.json;

public class AuthData {
    private String type, id;
    private char[] pw;

    public AuthData(String type, String id, char[] pw) {
        this.type = type;
        this.id = id;
        this.pw = pw;
    }
}
