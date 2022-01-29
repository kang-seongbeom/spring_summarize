package com.ksb.spring;

public class SqlRetrievalFailException extends RuntimeException{
    public SqlRetrievalFailException(String message){
        super(message);
    }

    //cause로 실패한 근본 원인을 담을 수 있게 함
    public SqlRetrievalFailException(String message, Throwable cause){
        super(message, cause);
    }
}
