# 2.3 개발자를 위한 테스팅 프레임워크 JUit

2.3.1 JUnit 테스트 실행 방법

- JUnitCore를 이용한 방법은 테스트의 수가 많아지면 관리하기 힘듦
- IDE에서 지원하는 방식을 사용하면 편함
- 이클립스 IDE는 여러 정보를 보여줌
    1. 총 수행시간
    2. 실행한 테스트의 수
    3. 테스트 에러의 수
    4. 테스트 실패의 수
    5. 어떤 테스트 클래스를 실행했는지
    6. @Test가 붙은 테스트 메소드의 이름
    7. 각 테스트 메소드와 메소드 수행 시간
- 빌드 툴에서 제공하는 JUnit 플러그인이나 태스트를 이용해 테스트 할 수 있음
- 테스트 결과를 HTML이나 텍스트 파일 형태로 추출 가능

2.3.2 deleteAll()의 getCount() 추가

- 현재 테스트는 수행되기 전에 수작업으로 DB의 데이터를 일일이 초기화 해야함
- 테스트가 외부 상태에 따라 결정되기도 함
- add()의 데이터와 동일한 데이터가 DB에 등록되어 있을 가능성이 있음
- deleteAll()을 통해 데이터를 초기화 하고, getCount를 통해 저장된 데이터의 개수를 가져옴
- deleteAll()
    
    ```java
    public void deleteAll() throws SQLException {
            Connection c = dataSource.getConnection();
    
            PreparedStatement ps = c.prepareStatement("delete from users");
            ps.executeUpdate();
            ps.close();
            c.close();
        }
    ```
    
- getCount()
    
    ```java
    public int getCount() throws SQLException {
            Connection c = dataSource.getConnection();
            PreparedStatement ps = c.prepareStatement("select count(*) from users");
    
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
    
            rs.close();
            ps.close();
            c.close();
    
            return count;
        }
    ```
    

2.3.3 deleteAll()과 getCount()

- deleteAll()은 테스트 시작될 때 시행
- 하지만, deleteAll()이 아직 검증이 되지 않았기 때문에 getCount()를 통해 데이터의 개수 확인
- 하지만, getCount() 역시 검증되지 않음. add() 후에 getCount()의 값이 바뀌는지 확인을 통해 검증할 수 있음

```java
@Test
    public void addAndGet() throws SQLException, ClassNotFoundException {
				...
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));
	
				User user = new	User();
				user.setId("1");
				user.setName("ksb");
				user.setPassword("ksb-p");

        dao.add(user);
        assertThat(dao.getCount(), is(1));

        User user2 = dao.get(user.getId());
        assertThat(user2.getName(), is(user.getName()));
        assertThat(user2.getPassword(), is(user.getPassword()));
    }
```

2.3.4 동일한 결과를 보장하는 테스트

- 위의 결과로 DB 삭제 수작업을 하지 않아도 됨
- 따라서 매번 동일한 결과를 얻을 수 있게 되었음
- 단위 테스트는 항상 일관성 있는 결과가 보장되야 함
- 또한, 외부 환경의 영향 및 실행 순서의 영향을 받지 말아야 함

2.3.5 JUnit 테스트 메소드 조건

1. @Test 애노테이션이 붙어야 함
2. public 접근자만 가능
3. 리턴 값이 void
4. 파라미터가 없어야 함

2.3.6 getCount() 테스트

- 테스트 메소드는 한 번에 한 가지 검증 목적에만 충실해야 함
- JUnit은 하나의 클래스 내부에 여러 테스트 메소드 허용
- 꼼꼼한 테스트를 하는것이 바람직함

```java
public class User {
		...
		public User(){}
		public User(String id, String name, String password){
        this.id = id;
        this.name = name;
        this.password = password;
    }
		...
}

public class UserDaoTest {
		@Test
    public void count() throws SQLException, ClassNotFoundException {
				User user1 = new User("k1", "k1", "k1");
				...

        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        assertThat(dao.getCount(), is(1));

        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        dao.add(user3);
        assertThat(dao.getCount(), is(3));
    }
}
```

<aside>
💡 JUnit은 테스트 실행 순서를 보장하지 않음. 각 테스트는 실행 순서와 무관해야 함

</aside>

2.3.7 addAndGet() 테스트 보완

- get()의 파라미터인 id에 대한 검증을 하지 못함
- 중복 확인으로 보안해야 함

```java
public class UserDaoTest {
    @Test
    public void addAndGet() throws SQLException, ClassNotFoundException {
				...
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        User userGet1 = dao.get(user1.getId());
        assertThat(userGet1.getName(), is(user1.getName()));
        assertThat(userGet1.getPassword(), is(user1.getPassword()));

        User userGet2 = dao.get(user2.getId());
        assertThat(userGet2.getName(), is(user2.getName()));
        assertThat(userGet2.getPassword(), is(user2.getPassword()));
    }
}
```

2.3.8 get() 예외조건에 대한 테스트

- get()의 파라미터인 id가 DB에 저장되지 않는 정보일 수 있음
- 정보가 없다는 예외 클래스를 던짐(throw)으로서 해결
- 미리 정의된 `EmptyResultDataAccessException` 사용
- @Test에 **expected**를 사용하면 정상적 테스트와 반대의 결과
- **예외 발생시** 테스트 **성공**, 예외 발생 안하면 테스트 실패
- 또한, 저장되지 않는 정보를 get하면 `ResultSet`에 정보가 담기지 않아 `SQLException` 발생
- UserDao의 get()에서 `ResultSet`에 정보가 있으면(rs.next()) User 오브젝트를 생성하고, 없으면 `EmptyResultDataAccessException`를 던짐

```java
public class UserDaoTest {
		...
		@Test(expected = EmptyResultDataAccessException.class)
    public void getUserFailure() throws SQLException, ClassNotFoundException {
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.get("unknown_id");
    }
}

public class UserDao {
		...
		public User get(String id) throws ClassNotFoundException, SQLException {
        ...
        ResultSet rs = ps.executeQuery();
        User user = null;
        if (rs.next()) {
            user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setPassword(rs.getString("password"));
        }

        rs.close();
        ps.close();
        c.close();

        if (user == null) throw new EmptyResultDataAccessException(1);

        return user;
    }
}
```

2.3.9 테스트 주도 개발(TDD, Test Driven Development)

- 테스트 코드를 먼저 만들고, 테스트를 성공하게 해주는 코드를 작성하는 방식의 개발 방법
- 테스트 우선 개발(Test First Development)라고도 함
- TDD의 기본 원칙
    
    <aside>
    💡 실패한 테스트를 성공시키기 위한 목적이 아닌 코드는 만들지 않는다
    
    </aside>
    
- getUserFailure() 테스트 코드에 나타난 기능
    
    
    |  | 단계 | 내용 | 코드 |
    | --- | --- | --- | --- |
    | 조건 | 어떤 조건을 가지고 | 가져올 사용자 정보가 존재하지 않는 경우에 | dao.deleteAll();
    assertThat(dao.getCount(), is(0)); |
    | 행위 | 무엇을 할 때 | 존재하지 않는 id로 get()을 실행하면 | dao.get("unknown_id"); |
    | 결과 | 어떤 결과가 나온다 | 특별한 예외가 던져진다 | @Test(expected = EmptyResultDataAccessException.class) |
- TDD는 기능설계, 구현, 테스트라는 일반적 개발 흐름에서 **기능설계**의 일부분을 담당
- TDD는 테스트를 성공하는 코드만 만들기 때문에 꼼꼼하게 개발할 수 있음
- 테스트 실행시간은 매우 짧으므로 TDD는 코드에 대한 **피드백을 빠르게** 받을 수 있음
- 개발자의 흔한 실수는 성공하는 테스트 코드만 작성
- 테스트 코드를 작성할 때 부정적인 케이스를 먼저 만드는 습관이 필요

2.3.10 JUnit이 실행하는 클래스 테스트 순서

1. 테스트 클래스에서 **@Test**가 붙은 **public**이고 **void**형이며 **파라미터가 없는** 테스트 메소드를 모두 찾음
2. 테스트 클래스의 오브젝트를 하나 생성
3. @Before가 붙은 메소드가 있으면 실행
4. @Test가 붙은 메소드를 하나 호출하고 테스트 결과를 저장
5. @After가 붙은 메소드가 있으면 실행
6. 나머지 테스트 메소드에 대해 2~5번 **반복**
7. 모든 테스트의 결과를 종합 및 출력

2.3.11 테스트 코드 개선

- @Before 애노테이션이 붙은 메소드는 @Test 애노테이션이 붙은 테스트 메소드 실행 전 **먼저** 실행됨
- 반복적인 코드를 미리 실행하여 인스턴스에 저장 함으로서 제거할 수 있음

```java
public class UserDaoTest {
		...
    @Before
    public void setUp(){
        ApplicationContext applicationContext =
                new GenericXmlApplicationContext("applicationContext.xml");
        this.dao = applicationContext.getBean("userDao", UserDao.class);

        this.user1 = new User("k1", "k1", "k1");
        this.user2 = new User("k2", "k2", "k2");
        this.user3 = new User("k3", "k3", "k3");
    }
}
```

2.3.12 테스트 코드의 주의점

- 각 테스트 **메소드를 실행할 때마다** 테스트 클래스의 **오브젝트 새로 생성**
- 각 테스트 메소드마다 독립적인 오브젝트로 동작하기 때문에 테스트 메소드끼리 확실하게 독립적 동작 가능
- 다음 테스트 메소드가 실행되면 새로운 오브젝트가 생성되기 때문에 인스턴스 변수를 부담없이 사용할 수 있음
- 일부 테스트 메소드에서만 중복되는 코드가 있을 경우 @Before을 사용하지 않고 메소드 추출 방식을 이용하는 것이 좋음

![https://gunju-ko.github.io//assets/img/posts/toby-spring/test.png](https://gunju-ko.github.io//assets/img/posts/toby-spring/test.png)

2.3.13 픽스처(Fixture)

- 테스트를 수행하는데 필요한 정보나 오브젝트를 의미함
- 일반적으로 여러 테스트에서 **반복적**으로 사용
- @Before 애노테이션을 통해 생성하면 편리함
- UserDaoTest에서 dao가 대표적인 픽스처임

```java
public class UserDaoTest {
    private UserDao dao;
    private User user1;
    private User user2;
    private User user3;

    @Before
    public void setUp(){
        ApplicationContext applicationContext =
                new GenericXmlApplicationContext("applicationContext.xml");
        this.dao = applicationContext.getBean("userDao", UserDao.class);

        this.user1 = new User("k1", "k1", "k1");
        this.user2 = new User("k2", "k2", "k2");
        this.user3 = new User("k3", "k3", "k3");
    }
}
```