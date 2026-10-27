package com.galilstay.server.exception;

/** הפרה של כלל עסקי - למשל יחידה תפוסה או ביטול מאוחר מדי */
public class RuleViolationException extends RuntimeException {

    public RuleViolationException(String message) {
        super(message);
    }
}
