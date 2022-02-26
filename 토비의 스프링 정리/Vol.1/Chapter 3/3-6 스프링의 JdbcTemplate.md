# 3.6 스프링의 JdbcTemplate

3.6.1 JdbcTemplate

- 스프링이 제공하는 JDBC 코드용 기본 템플릿
- `JdbcTemplate`은 생성자의 파라미터로 DataSource를 주입하면 됨
- `JdbcTemplate`는 DAO 안에서 만들어 수동 DI를 하는 것이 관례임
- 하지만, 낮은 결합도를 위해 `JdbcTemplate`를 독립적인 빈으로 등록하고 `JdbcTemplate`가 구현하고 있는 `JdbcOperations` 인터페이스를 통해 DI받아 사용하도록 해도 됨

```java
public class UserDao {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
```

<aside>
💡 JdbcTemplate을 직접 스프링 빈으로 등록하기 위해서는 setDataSource()을 setJdbcTemplate()로 바꾸면 됨

</aside>

3.6.2 update()

- deleteAll()에 처음 적용한 콜백은 `StatementStrategy` 인터페이스의 makePreparedStatement()임
- 이에 대응되는 JdbcTemplate 콜백은 `PrepareStatementCreator` 인터페이스의 createPreparedStatement()임
- 템플릿으로 부터 Connection을 제공받아 `PreparedStatement`를 반환하는 점에서 같음
- update()를 통해 콜백을 구현
- 앞서 만들었던 executeSql() 처럼 SQL 문장만으로 미리 준비된 콜백을 만들어서 템플릿을 호출 할 수 있음
- ps에 값을 바인딩 은 SQL 문장 다음 파라미터에 넣으면 됨

```java
	public class UserDao {
		...
		public void add(final User user) throws SQLException {
        String query = "insert into users(id, name, password) value (?,?,?)";
        this.jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getPassword());
                return ps;
            }
        });
    }
		public void deleteAll() throws SQLException{
        String query = "delete from users";
        this.jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) 
                    throws SQLException {
                return con.prepareStatement(query);
            }
        });
    }
}

//위와 같음
public class UserDao {
	...
	public void add(final User user) throws SQLException {
        String id = user.getId();
        String name = user.getName();
        String password = user.getPassword();
        String query = "insert into users(id, name, password) value (?,?,?)";
        this.jdbcTemplate.update(query, id, name, password);
    }
	public void deleteAll() throws SQLException{
        String query = "delete from users";
        this.jdbcTemplate.update(query);
    }
}
```

3.6.3 queryForInt() **[Deprecated]**

<aside>
💡 책에 나온 queryForInt는 deprecated 되었음. queryForObject()을 대신 사용

</aside>

- getCount()에 JdbcTemplate 적용
- getCount()는 ResultSet을 통해 값을 가져옴
- 템플릿은 **두개** 사용
    - PreparedStatementCreator : Connection을 받아 PreparedStatement 반환
    - ResultSetExtractor : ResultSet을 받고 추출한 결과 반환
- 위 두개 템플릿을 query()로 받음
- 콜백의 모든 반환 값은 결국 템플릿에 반환됨

```java
public class UserDao {
		...
		public int getCount() throws SQLException {
        String query = "select count(*) from users";
        return this.jdbcTemplate.query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con)
                    throws SQLException {
                return con.prepareStatement(query);
            }
        }, new ResultSetExtractor<Integer>() {
            @Override
            public Integer extractData(ResultSet rs)
                    throws SQLException, DataAccessException {
                rs.next();
                return rs.getInt(1);
            }
        });
    }
}
```

- ResultSet에서 추출할 수 있는 값이 다양하기 때문에 **제네릭스**임
- SQL의 실행 값이 하나의 정수 값이 되는 경우가 자주있고 콜백 작업을 위한 파라미터가 없기 때문에 `ResultSetExtractor` 콜백을 템플릿 안으로 재활용할 수 있음
- 이렇게 하나의 정수를 반환될 때 queryForObject()사용 가능

```java
public class UserDao {
		...
		public int getCount() {
        String query = "select count(*) from users";
        return this.jdbcTemplate.queryForObject(query, Integer.class);
    }
}
```

3.6.4 queryForObject()

- get() 메소드에 JdbcTemplate 적용
- get()의 경우 값을 바인딩 하고, User 오브젝트를 만들어 반환해 줘야함
- **ResultSetExtractor**는 ResultSet을 **한번** 전달받음
- **RowMapper**는 ResultSet의 로우 하나를 매핑하기 위해 사용되기 때문에 **여러번** 호출될 수 있음
- RowMapper 콜백을 사용하여 첫 번째 로우에 담긴 정보를 하나의 User 오브젝트에 매팽하면 됨
- RowMapper 두 번째 파라미터는 세 번째 파라미터가 존재하기 때문에 가변인자을 사용하지 못하고, 오브젝트 배열을 사용해서 SQL에 바인딩 해야함
- RowMapper가 호출되는 시점에서 ResultSet은 첫 번째 로우를 가리키므로, next()를 하지 않아도 됨
- queryForObject()는 SQL을 실행해서 받은 로우의 개수가 하나가 아니라면 `EmptyResultDataAccessException` 예외를 던지게 되어 있음

```java
public class UserDao {
		...
		public User get(String id) {
        String query = "select * from users where id = ?";
        return this.jdbcTemplate.queryForObject(query,
                new Object[]{id},
                new RowMapper<User>() {
                    @Override
                    public User mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        User user = new User();
                        user.setId(rs.getString("id"));
                        user.setName(rs.getString("name"));
                        user.setPassword(rs.getString("password"));
                        return user;
                    }
                }
        );
    }
}
```

3.6.5 query()

- 모든 사용자 정보를 가져오는 getAll() 사용
- 사용자 정보를 List<User> 컬렉션 사용
- id 순으로 정렬(order by)
- 동등성 비교
- 바인딩할 파라미터가 있으면 두 번째 파라미터에 추가. 없으면 생략 가능

```java
public class UserDaoTest {
		...
		@Test
    public void getAll() {
        dao.deleteAll();

        dao.add(user1);
        List<User> listUsers1 = dao.getAll();
        assertThat(listUsers1.size(), is(1));
        checkSameUser(user1, listUsers1.get(0));

        dao.add(user2);
        List<User> listUsers2 = dao.getAll();
        assertThat(listUsers2.size(), is(2));
        checkSameUser(user1, listUsers2.get(0));
        checkSameUser(user2, listUsers2.get(1));

        dao.add(user3);
        List<User> listUsers3 = dao.getAll();
        assertThat(listUsers3.size(), is(3));
        checkSameUser(user3, listUsers3.get(0));
        checkSameUser(user1, listUsers3.get(1));
        checkSameUser(user2, listUsers3.get(2));
    }

		private void checkSameUser(User pUser1, User pUser2) {
        assertThat(pUser1.getId(), is(pUser2.getId()));
        assertThat(pUser1.getName(), is(pUser2.getName()));
        assertThat(pUser1.getPassword(), is(pUser2.getPassword()));
    }
}

public class UserDao {
		public List<User> getAll() {
        String query = "select * from users order by id";
        return this.jdbcTemplate.query(query,
                new RowMapper<User>() {
                    @Override
                    public User mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        User user = new User();
                        user.setId(rs.getString("id"));
                        user.setName(rs.getString("name"));
                        user.setPassword(rs.getString("password"));
                        return user;
                    }
                }
        );
    }
}
```

3.6.6 테스트 보완

- 개발자는 예외적인 상황에 대한 검증을 잘 안하려는 습관이 있음
- 네거티브 테스트를 해야함
- 예외상황에 대한 일관성 있는 기준을 정하고 이를 테스트로 만들어서 검증해야 함
    
    <aside>
    💡 메소드마다 null 반환, 런타임 예외 발생, 빈 리스트 반환 등의 일관적이지 않은 상황이 발생 하므로
    
    </aside>
    
- query() 메소드가 예외 발생하면 빈 리스트 반환하도록 되어있음

```java
public class UserDaoTest {
		...
		@Test
    public void getAll() {
        dao.deleteAll();
        List<User> users0 = dao.getAll();
        assertThat(users0.size() ,is(0));
}
```

3.6.7 중복 제거

- get()과 getAll()의 RowMapper의 내용이 같음
- 나중에 검색과 같은 내용이 추가될 수 있으므로 추출
- RowMapper 콜백 오브젝트는 상태정보가 없기 때문에 필드로 만들어서 공유하게 함

```java
public class UserDao {
		...
		private RowMapper<User> userMapper =
            new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));
                    return user;
                }
            };
		public User get(String id) {
        String query = "select * from users where id = ?";
        return this.jdbcTemplate.queryForObject(query,
                new Object[]{id}, this.userMapper);
    }

    public List<User> getAll() {
        String query = "select * from users order by id";
        return this.jdbcTemplate.query(query, this.userMapper);
    }
}
```

3.6.8 템플릿/콜백 패턴과 UserDao

- 테이블과 필드정보가 변경되면 UserDao의 정보가 거의 모두 변경되므로 높은 응집도를 지님
- JDBC API 사용 방식, 예외처리, 리소스 반납, DB 연결 등의 책임과 관심은 JdbcTemplate가 가져 책임이 다른 코드와 낮은 결합도를 지님
- 기존 관례처럼 JdbcTemplate을 직접 구현하여 수동 DI를 하고 있음
- 두 가지 개선점
    1. userMapper가 인스턴스 변수이면서 한번 만들어지면 변경되지 않는 프로퍼티와 같은 성격을 지니므로 UserMapper을 독립된 빈으로 만들고 XML 설정에 의한 변경을 할 수 있음
    2. SQL 문장을 외부 리소스에 담고 이를 읽어와 사용하게 함(일부 개발팀은 정책적으로 SQL 쿼리를 DBA가 만들어서 제공하고 관리하는 경우가 있음)
    
    <aside>
    💡 위 개선점은 나중에 할 것임
    
    </aside>
    

```java
public class UserDao {

    private RowMapper<User> userMapper =
            new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));
                    return user;
                }
            };

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void add(final User user) {
        String id = user.getId();
        String name = user.getName();
        String password = user.getPassword();
        String query = "insert into users(id, name, password) value (?,?,?)";
        this.jdbcTemplate.update(query, id, name, password);
    }

    public void deleteAll() {
        String query = "delete from users";
        this.jdbcTemplate.update(query);
    }

    public User get(String id) {
        String query = "select * from users where id = ?";
        return this.jdbcTemplate.queryForObject(query,
                new Object[]{id}, this.userMapper);
    }
    
    public List<User> getAll() {
        String query = "select * from users order by id";
        return this.jdbcTemplate.query(query, this.userMapper);
    }

    public int getCount() {
        String query = "select count(*) from users";
        return this.jdbcTemplate.queryForObject(query, Integer.class);
    }
}
```