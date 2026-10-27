package com.galilstay.server.exception;

public class BadLoginException extends RuntimeException {

    public BadLoginException() {
        super("שם המשתמש או הסיסמה אינם נכונים");
    }
}
