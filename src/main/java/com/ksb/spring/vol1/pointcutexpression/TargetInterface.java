package com.ksb.spring.vol1.pointcutexpression;

public interface TargetInterface {
    void hello();
    void hello(String s);
    int minus(int a, int b) throws RuntimeException;
    int plus(int a, int b);
}
