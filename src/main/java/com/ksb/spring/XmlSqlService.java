package com.ksb.spring;

import com.ksb.spring.jaxb.SqlType;
import com.ksb.spring.jaxb.Sqlmap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class XmlSqlService implements SqlService{
    private Map<String, String> sqlMap = new HashMap<>();

    public XmlSqlService(){
        String contextPath = Sqlmap.class.getPackage().getName();

        try{
            //JAXB API를 이용해 XML 문서를 오브젝트 트리로 읽어옴
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            //UserDao와 같은 클래스 패스의 sqlmap.xml 파일 변환
            InputStream is = UserDao.class.getResourceAsStream("sqlmap.xml");
            Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(is);

            //읽어온 SQL을 맵으로 저장
            for(SqlType sql : sqlmap.getSql())
                sqlMap.put(sql.getKey(), sql.getValue());

        } catch (JAXBException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailException {
        String sql = sqlMap.get(key);
        if(sql == null)
            throw new SqlRetrievalFailException(key+"에 대한 SQL을 찾을 수 없습니다.");
        else
            return sql;
    }
}
