package com.ksb.spring;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EmbeddedDbSqlRegistry implements UpdatableSqlRegistry {
    NamedParameterJdbcTemplate jdbc;
    //JdbcTemplate와 트랜잭션을 동기화하는 트랜잭션 템플릿
    //멀티스레드 환경에서 공유 가능
    TransactionTemplate transactionTemplate;

    /*
        내장 DB 빌더가 Datasource의 서브 인스턴스인 EmbeddedDatebase를 반환 해도
        Datasource로 인자를 받는 이유는, 인터페이스 분리 원칙을 지키기 위함임.
        클라이언트는 자신이 필요한 기능을 가진 인터페이스를 DI 받아야함.
        SQL 레지스트리는 JDBC를 이용해 DB에 접근만 하면 되므로 Datasource가 가장 적합함
     */
    public void setDataSource(DataSource dataSource) {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
        transactionTemplate = new TransactionTemplate(
                new DataSourceTransactionManager(dataSource)
        );
    }

    @Override
    public void registrySql(String key, String sql) {
        System.out.println("qweqwe" + key);
        Map<String, String> params = new HashMap<>();
        params.put("key_", key);
        params.put("sql_", sql);

        jdbc.update("insert into sqlmap(key_, sql_) values(:key_, :sql_)", params);
    }

    @Override
    public String findSql(String key) throws SqlRetrievalFailException {
        try {
            Map<String, String> params = Collections.singletonMap("key_", key);

            return jdbc.queryForObject("select sql_ from sqlmap where key_ = :key_",
                    params, String.class);
        } catch (EmptyResultDataAccessException e) {
            throw new SqlNotFoundException(key + "에 해당하는 SQL을 찾을 수 없습니다.");
        }
    }

    @Override
    public void updateSql(String key, String sql) {
        Map<String, String> params = new HashMap<>();
        params.put("sql_", sql);
        params.put("key_", key);
        int affected = jdbc.update("update sqlmap set sql_ = :sql_ where key_ = :key_",
                params);
        if (affected == 0) {
            throw new SqlUpdateFailureException(key + "에 해당하는 SQL을 찾을 수 없습니다.");
        }
    }

    //익명 내부 클래스에서 인자를 사용하기 때문에 final을 붙여야 함
    @Override
    public void updateSql(final Map<String, String> sqlmap) {
        //트랜잭션 템플릿이 만드는 트랜잭션 경계 안에서 동작할 코드를 콜백 형태로 만들고
        //TransactionTemplate의 execute() 메소드에 전달해야 함
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                for (Map.Entry<String, String> entry : sqlmap.entrySet()) {
                    updateSql(entry.getKey(), entry.getValue());
                }
            }
        });
    }
}
