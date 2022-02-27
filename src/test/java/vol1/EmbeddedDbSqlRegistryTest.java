package vol1;

import com.ksb.spring.vol1.EmbeddedDbSqlRegistry;
import com.ksb.spring.vol1.SqlUpdateFailureException;
import com.ksb.spring.vol1.UpdatableSqlRegistry;
import org.junit.After;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;

public class EmbeddedDbSqlRegistryTest extends AbstractUpdateSqlRegistryTest{
    EmbeddedDatabase db;

    @Override
    protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
        db = new EmbeddedDatabaseBuilder()
                .setType(HSQL)
                .addScript("/vol1/schema.sql")
                .build();

        EmbeddedDbSqlRegistry embeddedDbSqlRegistry = new EmbeddedDbSqlRegistry();
        embeddedDbSqlRegistry.setDataSource(db);

        return embeddedDbSqlRegistry;
    }

    @Test
    public void transactionalUpdate(){
        checkFindResult("SQL1", "SQL2", "SQL3");

        Map<String,String> sqlmap = new HashMap<>();
        sqlmap.put("KEY1", "modify1");
        sqlmap.put("keyError", "modify2"); //키를 못찾기 때문에 실패할 것임

        try{
            sqlRegistry.updateSql(sqlmap);
        }catch (SqlUpdateFailureException e){
        }

        //트랜잭션 적용 되면 롤백될 것이기 때문에 원래 상태로 돌아와야 함
        //만약 트랜잭션 적용되지 않으면 첫 번째가 modify1으로 될 것임
        //때문에 checkFindResult() 실패
        checkFindResult("SQL1", "SQL2", "SQL3");
    }

    @After
    public void tearDown(){
        db.shutdown();
    }
}
