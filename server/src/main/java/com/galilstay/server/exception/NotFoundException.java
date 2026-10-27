package com.galilstay.server.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String what, Long id) {
        super(what + " מספר " + id + " לא נמצא/ה");
    }
}
