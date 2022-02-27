package vol1;

import com.ksb.spring.vol1.SqlNotFoundException;
import com.ksb.spring.vol1.SqlUpdateFailureException;
import com.ksb.spring.vol1.UpdatableSqlRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public abstract class AbstractUpdateSqlRegistryTest {
    UpdatableSqlRegistry sqlRegistry;

    @Before
    public void setUp(){
        sqlRegistry = createUpdatableSqlRegistry();
        //사용할 정보 미리 등록
        sqlRegistry.registrySql("KEY1", "SQL1");
        sqlRegistry.registrySql("KEY2", "SQL2");
        sqlRegistry.registrySql("KEY3", "SQL3");
    }

    abstract protected UpdatableSqlRegistry createUpdatableSqlRegistry();

    @Test
    public void find(){
        checkFindResult("SQL1", "SQL2", "SQL3");
    }

    protected void checkFindResult(String expected1, String expected2, String expected3) {
        assertThat(sqlRegistry.findSql("KEY1"), is(expected1));
        assertThat(sqlRegistry.findSql("KEY2"), is(expected2));
        assertThat(sqlRegistry.findSql("KEY3"), is(expected3));
    }

    @Test(expected = SqlNotFoundException.class)
    public void unknownKey(){
        sqlRegistry.findSql("SQLERROR");
    }

    @Test
    public void updateSingle(){
        sqlRegistry.updateSql("KEY2", "modify2");
        checkFindResult("SQL1", "modify2", "SQL3");
    }

    @Test
    public void updateMulti(){
        Map<String, String> sqlmap = new HashMap<>();
        sqlmap.put("KEY1", "modify1");
        sqlmap.put("KEY3", "modify3");

        sqlRegistry.updateSql(sqlmap);
        checkFindResult("modify1", "SQL2", "modify3");
    }

    @Test(expected = SqlUpdateFailureException.class)
    public void updateWithNotExistingKey(){
        sqlRegistry.updateSql("SQLERROR", "modify2");
    }
}
