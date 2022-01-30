import com.ksb.spring.jaxb.SqlType;
import com.ksb.spring.jaxb.Sqlmap;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JaxbTest {
    @Test
    public void readSqlmap() throws JAXBException, IOException {
        //바인딩용 클래스들 위치를 가지고 JAXB 컨텍스트를 만듦
        String contextPath = Sqlmap.class.getPackage().getName();
        JAXBContext context = JAXBContext.newInstance(contextPath);

        //언마샬러 생성. xml -> 자바 오브젝트
        Unmarshaller unmarshaller = context.createUnmarshaller();

        //언마샬을 하면 매핑된 오브젝트 트리의 루트인 Sqlmap을 반환
        Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(
                getClass().getResourceAsStream("sqlmap.xml")
        );

        //List에 담겨 있는 Sql 오브젝트를 가져와 XML 문서와 같은 정보를 갖는지 확인
        List<SqlType> sqlList = sqlmap.getSql();

        assertThat(sqlList.size() ,is(3));
        assertThat(sqlList.get(0).getKey(), is("add"));
        assertThat(sqlList.get(0).getValue(), is("insert"));
        assertThat(sqlList.get(1).getKey(), is("get"));
        assertThat(sqlList.get(1).getValue(), is("select"));
        assertThat(sqlList.get(2).getKey(), is("delete"));
        assertThat(sqlList.get(2).getValue(), is("delete"));
    }
}
