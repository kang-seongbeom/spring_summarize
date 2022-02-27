package com.ksb.spring.vol1;

public class DuplicateUserIdException extends RuntimeException{
    //중첩 예외를 위한 생성자
    public DuplicateUserIdException(Throwable cause){
        super(cause);
    }
}
