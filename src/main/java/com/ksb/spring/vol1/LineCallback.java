package com.ksb.spring.vol1;

import java.io.BufferedReader;
import java.io.IOException;

public interface LineCallback<T> {
    T doSomethingWithLine(String line, T value) throws IOException;
}