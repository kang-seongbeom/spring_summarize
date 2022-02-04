package com.ksb.spring;

public interface SqlRegistry {
    void registrySql(String key, String sql); //sql을 검색할 수 있도록 key와 함께 등록

    String findSql(String key) throws SqlRetrievalFailException;
}
