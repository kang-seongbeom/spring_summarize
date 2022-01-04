package com.ksb.spring;

import java.io.BufferedReader;
import java.io.IOException;

public interface LineCallback<T> {
    T doSomethingWithLine(String line, T value) throws IOException;
}