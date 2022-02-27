package vol1;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import java.util.*;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class EmbeddedDbTest {
    EmbeddedDatabase db;
    NamedParameterJdbcTemplate template;

    @Before
    public void setUp(){
        db= new EmbeddedDatabaseBuilder()
                .setType(HSQL)
                .addScript("/vol1/schema.sql")
                .addScript("/vol1/data.sql")
                .build();

        template = new NamedParameterJdbcTemplate(db);
    }

    @After
    public void tearDown(){
        db.shutdown();
    }

    @Test
    public void initData(){
        String sql = "select count(*) from sqlmap";
        Map<String, String> params = Collections.singletonMap(":null", "null");
        // 두 번째 값은 쓰레기 값임
        assertThat(template.queryForObject(sql, params,Integer.class), is(2));

        sql = "select * from sqlmap order by key_";
        params = Collections.singletonMap(":key_", "key_");
        List<Map<String, Object>> list = template.queryForList(sql, params);
        assertThat((String)list.get(0).get("key_"), is("KEY1"));
        assertThat((String)list.get(0).get("sql_"), is("SQL1"));
        assertThat((String)list.get(1).get("key_"), is("KEY2"));
        assertThat((String)list.get(1).get("sql_"), is("SQL2"));
    }

    @Test
    public void insert(){
        String sql = "insert into sqlmap(key_, sql_) values(:key_, :sql_)";
        Map<String,String> params = new HashMap<>();
        params.put("key_", "KEY3");
        params.put("sql_", "SQL3");
        template.update(sql, params);

        sql = "select count(*) from sqlmap";
        params = Collections.singletonMap(":null", "null");
        assertThat(template.queryForObject(sql, params,Integer.class), is(3));
    }

}
