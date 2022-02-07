import com.ksb.spring.SqlRegistry;

import java.util.Map;

public interface UpdatableSqlRegistry extends SqlRegistry {
    void updateSql(String key, String sql);

    void updateSql(Map<String, String> sqlmap);
//    void updateSql(Map<String, String> sqlmap) throws SqlUpdateFailException;
//
//    void updateSql(String key, String sql) throws SqlUpdateFailException;
}
