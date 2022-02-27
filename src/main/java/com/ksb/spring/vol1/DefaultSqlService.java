package com.ksb.spring.vol1;

public class DefaultSqlService extends BaseSqlService{
    public DefaultSqlService(){
        //생성자에서 디폴트 의존 관계를 수동 DI
        setSqlReader(new JaxbXmlSqlReader());
        setSqlRegistry(new HashMapSqlRegistry());
    }
}
