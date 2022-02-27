package com.ksb.spring.vol1;

import javax.annotation.PostConstruct;

public class BaseSqlService implements SqlService{
    //상속을 통한 확장을 위해 protected로 선언
    protected SqlReader sqlReader;
    protected SqlRegistry sqlRegistry;

    public void setSqlReader(SqlReader sqlReader) {
        this.sqlReader = sqlReader;
    }

    public void setSqlRegistry(SqlRegistry sqlRegistry) {
        this.sqlRegistry = sqlRegistry;
    }

    @PostConstruct
    public void loadSql() {
        this.sqlReader.read(this.sqlRegistry);
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailException {
        try{
            return this.sqlRegistry.findSql(key);
        }catch (SqlRetrievalFailException e){
            throw new RuntimeException(e);
        }
    }
}
