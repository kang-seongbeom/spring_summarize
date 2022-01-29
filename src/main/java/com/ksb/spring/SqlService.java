package com.ksb.spring;

public interface SqlService {
    String getSql(String key) throws SqlRetrievalFailException;
}
