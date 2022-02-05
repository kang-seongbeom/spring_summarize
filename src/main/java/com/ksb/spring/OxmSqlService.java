package com.ksb.spring;

import com.ksb.spring.jaxb.SqlType;
import com.ksb.spring.jaxb.Sqlmap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Unmarshaller;

import javax.annotation.PostConstruct;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

public class OxmSqlService implements SqlService{
    private final OxmSqlReader oxmSqlReader = new OxmSqlReader();
    private final BaseSqlService baseSqlService = new BaseSqlService();

    private SqlRegistry sqlRegistry = new HashMapSqlRegistry();

    public void setSqlRegistry(SqlRegistry sqlRegistry){
        this.sqlRegistry = sqlRegistry;
    }

    //DI 받은 것을 oxmSqlReader로 전달
    public void setUnmarshaller(Unmarshaller unmarshaller){
        oxmSqlReader.setUnmarshaller(unmarshaller);
    }

    public void setSqlmap(Resource sqlmap){
        oxmSqlReader.setSqlmap(sqlmap);
    }

    @PostConstruct
    public void loadSql() {
        this.baseSqlService.setSqlReader(this.oxmSqlReader);
        this.baseSqlService.setSqlRegistry(this.sqlRegistry);

        this.baseSqlService.loadSql();
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailException {
        return this.baseSqlService.getSql(key);
    }

    private class OxmSqlReader implements SqlReader{
        private Unmarshaller unmarshaller;

        private Resource sqlmap = new ClassPathResource("/sqlmap.xml",
                UserDao.class);

        public void setUnmarshaller(Unmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
        }

        public void setSqlmap(Resource sqlmap) {
            this.sqlmap = sqlmap;
        }

        @Override
        public void read(SqlRegistry sqlRegistry) {
            try{
                Source source = new StreamSource(sqlmap.getInputStream());
                Sqlmap sqlmap = (Sqlmap)this.unmarshaller.unmarshal(source);

                for(SqlType sql : sqlmap.getSql())
                    sqlRegistry.registrySql(sql.getKey(), sql.getValue());
            }catch (IOException e){
                throw new IllegalArgumentException(this.sqlmap.getFilename() +
                        "을 가져올 수 없습니다."+e);
            }
        }
    }

}
