package com.ksb.spring.vol1;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class UserSqlMapConfig implements SqlMapConfig{
    @Override
    public Resource getSqlMapResource() {
        return new ClassPathResource("/vol1/sqlmap.xml", UserDao.class);
    }
}
