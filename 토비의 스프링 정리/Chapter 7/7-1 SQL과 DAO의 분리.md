# 7.1 SQL과 DAO의 분리

7.1.1 XML 설정을 이용한 SQL과 DAO의 분리

- 비즈니스 로직을 Service에서 책임을 갖게 하고, 데이터 액세스 로직을 DAO에서 책임을 갖도록 했음
- 데이터를 가져오고 추가하는 작업의 **인터페이스 역할**을 하는 것이 DAO임
- 데이터 액세스 기술 및 오브젝트 등이 변경 되더라도 DAO는 변경되지 않음
- 하지만, DAO에서 DB의 테이블, 필드 이름과 같은 SQL 문장이 바뀔수 있음
- 필드 추가와 같은 SQL 변경이 필요한 상황에는 DAO 코드는 수정될 수 밖에 없음
- 때문에, SQL과 DAO를 분리해야 함
- SQL을 스프링의 XML 설정 파일로 빼고, String으로 빈의 값을 주입
- SQL은 String으로 빈의 값이 주입되어 있으므로, 프토퍼티 값으로 정의해 DAO에 주입 가능

7.1.2 개별 SQL 프로퍼티 방식

- UserDaoJdbc에는 6개의 SQL 문장이 있음
- 조회할 때 사용되는 userMapper도 SQL 문장은 아니지만 필드 이름을 가지고 있음
- 지금은 순수 6개의 SQL 문장만 프로퍼티로 만들고 XML에서 지정
- DI를 위한 변수와 수정자 메소드를 일일이 만들어야 함

```xml
<beans
		.../>
    <bean id="userDao" class="com.ksb.spring.UserDaoJdbc">
        <property name="dataSource" ref="dataSource"/>
        <property name="sqlAdd" value="insert into users(id, name, password, level, login, recommend) value (?,?,?,?,?,?)"/>
				...<!--나머지 5개의 프로퍼티 및 sql-->
    </bean>
</beans>
```

```java
public class UserDaoJdbc implements UserDao {
    private String sqlAdd;
		//나머지 5개의 sql 문장을 저장할 변수

		...

    public void setSqlAdd(String sqlAdd){
        this.sqlAdd = sqlAdd;
    }
		//나머지 5개의 수정자 메소드
		...
		public void add(final User user) {
        ...
				//this.sqlMap.get("add")은 제공받은 맵에서 필요한 sql을 가져옴
        this.jdbcTemplate.update(this.sqlMap.get("add"), id, name, password,
                level, login, recommend);
    }
}
```

7.1.3 SQL 맵 프로퍼티 방식

- 개별 SQL 프로퍼티 방식은 sql 하나 당 수정자 메소드가 필요
- SQL을 컬렉션인 맵에 저장시키면 프로퍼티는 하나만 만들면 됨
- 프로퍼티가 하나로 줄었지만, SQL을 가져올 때 문자열로 된 **키 값**을 사용하기 때문에 메소드가 실행되기 전에는 오류 확인이 힘듦

```xml
<beans
		.../>
		...
    <bean id="userDao" class="com.ksb.spring.UserDaoJdbc">
        <property name="dataSource" ref="dataSource"/>
        <property name="sqlMap">
            <map>
                <entry key="add" value="insert into
                users(id, name, password, level, login, recommend) value (?,?,?,?,?,?)"/>
                <entry key="get" value="select * from users where id = ?"/>
                <entry key="getAll" value="select * from users order by id"/>
                <entry key="deleteAll" value="delete from users"/>
                <entry key="getCount" value="select count(*) from users"/>
                <entry key="update" value="update
                users set name=?, password=?, level=?, login=?, recommend=? where id=?"/>
            </map>
        </property>
    </bean>
</beans>
```

```java
public class UserDaoJdbc implements UserDao {
		...
		public void add(final User user) {
        ...
        this.jdbcTemplate.update(this.sqlMap.get("add"), id, name, password,
                level, login, recommend);
    }
    public void deleteAll() {
        this.jdbcTemplate.update(this.sqlMap.get("deleteAll"));
    }

    public User get(String id) {
        return this.jdbcTemplate.queryForObject(this.sqlMap.get("get"),
                new Object[]{id}, this.userMapper);
    }

    public List<User> getAll() {
        return this.jdbcTemplate.query(this.sqlMap.get("getAll"), this.userMapper);
    }

    public int getCount() {
        return this.jdbcTemplate.queryForObject(this.sqlMap.get("getCount"), Integer.class);
    }

    public void update(User user1) {
        ...
        this.jdbcTemplate.update(this.sqlMap.get("update"), name, password,
                level, login, recommend, id);
    }
}
```

7.1.4 SQL 제공 서비스

- SQL 문장과 XML 설정 정보가 같은 파일에 생겨 관리 및 의존 관계 확인이 힘들어졌음
- SQL 문장과 XML 설정 정보를 같은 파일에 두는 건 바람직하지 못함
- 독립적인 SQL 제공 서비스가 필요함

7.1.5 SQL 서비스 인터페이스

- 키 값을 전달하면 해당 SQL을 돌려주는 SQL 인터페이스를 만듦
- DAO는 적절한 키를 제공해주고 드에대한 SQL문만 돌려받으면 됨
- 인터페이스의 이름은 SqlService
- SqlService 구현 클래스 이름은 SimpleSqlService
- SQL문을 가져오는데 실패하면 SqlRetrievalFailException 예외를 던지도록 정의
- 해당 예외는 런타임 예외로 정의
    
    <aside>
    💡 어쩌피, SQL문을 가져오는데 실패하면 복구가 불가능 함
    
    </aside>
    
- SqlService 인터페이스를 사용함으로써, UserDao를 비롯한 모든 DAO는 SQL에 대한 관심을 없앨 수 있음
- 구체적인 구현 방법과 기술 상관없이 SqlService 인터페이스 타입의 빈을 DI 받아 필요한 SQL을 사용하기만 하면 됨
- 모든 DAO가 SqlService를 사용하기 때문에 **키 값** 구분을 잘 해야 함

```java
public interface SqlService {
    String getSql(String key) throws SqlRetrievalFailException;
}

public class SqlRetrievalFailException extends RuntimeException{
    public SqlRetrievalFailException(String message){
        super(message);
    }

    //cause로 실패한 근본 원인을 담을 수 있게 함
    public SqlRetrievalFailException(String message, Throwable cause){
        super(message, cause);
    }
}

public class SimpleSqlService implements SqlService{
    private Map<String, String> sqlMap;

    public void setSqlMap(Map<String, String> sqlMap){
        this.sqlMap = sqlMap;
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

public class UserDaoJdbc implements UserDao {
    private SqlService sqlService;

    public void setSqlService(SqlService sqlService){
        this.sqlService = sqlService;
    }
		...

    public void add(final User user) {
        ...
        this.jdbcTemplate.update(sqlService.getSql("userAdd"), id, name, password,
                level, login, recommend);
    }

    public void deleteAll() {
        this.jdbcTemplate.update(sqlService.getSql("userDeleteAll"));
    }

    public User get(String id) {
        return this.jdbcTemplate.queryForObject(sqlService.getSql("userGet"),
                new Object[]{id}, this.userMapper);
    }

    public List<User> getAll() {
        return this.jdbcTemplate.query(sqlService.getSql("userGetAll"), this.userMapper);
    }

    public int getCount() {
        return this.jdbcTemplate.queryForObject(sqlService.getSql("userGetCount"), Integer.class);
    }

    public void update(User user1) {
        ...
        this.jdbcTemplate.update(sqlService.getSql("userUpdate"), name, password,
                level, login, recommend, id);
    }
}
```

```xml
<beans
		.../>
    <bean id="userDao" class="com.ksb.spring.UserDaoJdbc">
        <property name="dataSource" ref="dataSource"/>
        <property name="sqlService" ref="sqlService"/>
    </bean>

    <bean id="sqlService" class="com.ksb.spring.SimpleSqlService">
        <property name="sqlMap">
            <map>
                <entry key="userAdd" value="insert into
                users(id, name, password, level, login, recommend) value (?,?,?,?,?,?)"/>
                <entry key="userGet" value="select * from users where id = ?"/>
                <entry key="userGetAll" value="select * from users order by id"/>
                <entry key="userDeleteAll" value="delete from users"/>
                <entry key="userGetCount" value="select count(*) from users"/>
                <entry key="userUpdate" value="update
                users set name=?, password=?, level=?, login=?, recommend=? where id=?"/>
            </map>
        </property>
    </bean>
</beans>
```