package com.ksb.spring.vol1;

public class SqlNotFoundException extends RuntimeException {
    public SqlNotFoundException() {
        super();
    }

    public SqlNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlNotFoundException(String message) {
        super(message);
    }

    public SqlNotFoundException(Throwable cause) {
        super(cause);
    }
}

