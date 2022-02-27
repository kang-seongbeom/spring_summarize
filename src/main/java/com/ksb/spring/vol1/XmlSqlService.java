package com.ksb.spring.vol1;

import com.ksb.spring.vol1.jaxb.SqlType;
import com.ksb.spring.vol1.jaxb.Sqlmap;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class XmlSqlService implements SqlService, SqlRegistry, SqlReader {
    //sqlMap은 SqlRegistry 구현의 일부가 되었으므로,
    // 책임이 다른 곳에서 직접 접근하면 안됨
    private Map<String, String> sqlMap = new HashMap<>();

    private String sqlmapFile;
    private SqlReader sqlReader;
    private SqlRegistry sqlRegistry;

    public void setSqlReader(SqlReader sqlReader) {
        this.sqlReader = sqlReader;
    }

    public void setSqlRegistry(SqlRegistry sqlRegistry) {
        this.sqlRegistry = sqlRegistry;
    }

    public void setSqlmapFile(String sqlmapFile) {
        this.sqlmapFile = sqlmapFile;
    }

    @Override
    public void registrySql(String key, String sql) {
        sqlMap.put(key, sql);
    }

    @Override
    public String findSql(String key) throws SqlRetrievalFailException {
        String sql = sqlMap.get(key);
        if (sql == null)
            throw new SqlRetrievalFailException(key + "에 대한 SQL을 찾을 수 없습니다.");
        else
            return sql;
    }

    @Override
    public void read(SqlRegistry sqlRegistry) {
        String contextPath = Sqlmap.class.getPackage().getName();

        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = UserDao.class.getResourceAsStream(sqlmapFile);
            Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(is);
            for (SqlType sql : sqlmap.getSql()) {
                //sqlMap 직접하용하지 않고, 간접적 사용
                sqlRegistry.registrySql(sql.getKey(), sql.getValue());
            }

        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
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
