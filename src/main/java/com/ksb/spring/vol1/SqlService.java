package com.ksb.spring.vol1;

public interface SqlService {
    String getSql(String key) throws SqlRetrievalFailException;
}
