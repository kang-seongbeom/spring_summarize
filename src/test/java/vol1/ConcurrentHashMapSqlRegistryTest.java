package vol1;

import com.ksb.spring.vol1.ConcurrentHashMapSqlRegistry;
import com.ksb.spring.vol1.UpdatableSqlRegistry;

public class ConcurrentHashMapSqlRegistryTest extends  AbstractUpdateSqlRegistryTest{
    @Override
    protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
        return new ConcurrentHashMapSqlRegistry();
    }
}
