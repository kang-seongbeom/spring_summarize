import com.ksb.spring.ConcurrentHashMapSqlRegistry;
import com.ksb.spring.UpdatableSqlRegistry;

public class ConcurrentHashMapSqlRegistryTest extends  AbstractUpdateSqlRegistryTest{
    @Override
    protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
        return new ConcurrentHashMapSqlRegistry();
    }
}
