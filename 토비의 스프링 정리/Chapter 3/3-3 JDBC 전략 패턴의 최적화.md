# 3.3 JDBC 전략 패턴의 최적화

3.3.1 전략 클래스의 추가정보

- add()에는 User 정보를 받아와서 PrepareStatement를 실행해야 함
- 해당 User 정보는 클라이언트로 부터 생성자의 파라미터로 받으면 됨
- try/catch/finnaly 컨텍스트를 공유할 수 있게 됨
- 코드의 양이 매우 줄어듦
- 하지만, **두 가지 문제점** 있음
    1. 템플릿 메소드 패턴과 비슷하게 DAO 메소드 마다 **새로운 StatementStrategy 구현 클래스**를 만들어야 함. 클래스의 양이 너무 많아짐
    2. StatementStrategy에 전달할 User와 같은 부가 정보가 있을 경우, 이 정보를 받기 위해 생성자 및 인스턴스 변수 필요

```java
public class AddStatement implements StatementStrategy{
    private User user;

    public AddStatement(User user){
        this.user = user;
    }
    @Override
    public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
        PreparedStatement ps = c.prepareStatement(
                "insert into users(id, name, password) value (?,?,?)"
        );
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());
        return ps;
    }
}

public class UserDaoTest {
		...
		@Test
    public void addAndGet() throws SQLException {
        ...
        st = new AddStatement(user1);
        dao.jdbcContextWithStatementStrategy(st);
				...
		}
}
```

3.3.2 중첩 클래스(Nested Class)

- 다른 클래스 내부에 정의되는 클래스
- 종류
    1. 스태틱 클래스(Static Class)
        - **독립적**으로 오브젝트로 만들어 질 수 있는 클래스
    2. 내부 클래스(Inner Class)
        - 자신이 정의된 클래스 오브젝트 내에서만 만들어질 수 있는 클래스
        - **범위**(scope)에 따라 세 가지로 구분
            1. 멤버 내부 클래스(Member Inner Class)
                1. 멤버 필드처럼 **오브젝트 레벨**에 정의되는 클래스
            2. 로컬 클래스(Local Class)
                1. **메소드 레벨**에 정의되는 클래스
            3. 익명 내부 클래스(Anonymous Inner Class)
                1. 이름을 갖지 않는 클래스
            

3.3.3 로컬 클래스

- 클래스 파일이 많아지는 문제는 매번 독립된 파일로 만들지 않고, 내부 클래스로 정의
- DeleteAllStatement나 AddStatement는 UserDao 밖에서는 사용되지 않음
- 즉, UserDao의 메소드 로직에 강하게 결합되어 있음
- 로컬 클래스는 자신이 선언된 곳의 정보에 접근할 수 있어, User와 같은 부가 정보를 따로 넘겨주지 않아도 됨
- 단, 내부 클래스에서 외부의 변수를 사용할 때는 외부 변수는 반드시 final 이어야 함
    - 클래스 파일을 줄일 수 있고, 값을 넘겨주지 않아도 되므로 3.3.1의 두 가지 문제를 해결함

```java
public class UserDao {
		...
		public void add(final User user) throws SQLException {
        class AddStatement implements StatementStrategy{
            @Override
            public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
                PreparedStatement ps = c.prepareStatement(
                        "insert into users(id, name, password) value (?,?,?)"
                );
                ps.setString(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getPassword());
                return ps;
            }
        }
        StatementStrategy st = new AddStatement();
        jdbcContextWithStatementStrategy(st);
    }
}
```

3.3.4 익명 내부 클래스

- AddStatement 클래스는 add()에서만 사용할 용도이므로, 익명 내부 클래스를 사용하여 클래스 이름도 제거 가능
- 익명 내부 클래스는 선언과 동시에 오브젝트를 생성함
- 이름이 없기 때문에 클래스 자신의 타입을 가질 수 없고(구현 클래스의 생성자), 구현한 인터페이스 타입의 변수에만 저장할 수 있음

```java
public class UserDao {
		...
		public void add(final User user) throws SQLException {
        StatementStrategy st = new StatementStrategy() {
            public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
                PreparedStatement ps = c.prepareStatement(
                        "insert into users(id, name, password) value (?,?,?)"
                );
                ps.setString(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getPassword());
                return ps;
            }
        };
        jdbcContextWithStatementStrategy(st);
    }
}

//위와 같음
public class UserDao {
		...
		public void add(final User user) throws SQLException {
        jdbcContextWithStatementStrategy(new StatementStrategy() {
            public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
                PreparedStatement ps = c.prepareStatement(
                        "insert into users(id, name, password) value (?,?,?)"
                );
                ps.setString(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getPassword());
                return ps;
            }
        });
    }
}
```

```java
public class UserDao {
		...
		public void deleteAll() throws SQLException{
        jdbcContextWithStatementStrategy(new StatementStrategy() {
            public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
                return c.prepareStatement("delete from users");
                
            }
        });
    }
}
```