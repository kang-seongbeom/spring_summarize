# 1.1 초난감 DAO

1.1.1 DAO(Data Access Object)

- **DB**를 사용해 데이터를 조회하거나 조작하는 기능을 전담하도록 만든 **오브젝트**

1.1.2 자바빈

- 원래는 비주얼 툴에서 조작 가능한 컴포넌트를 의미함
- 현재는 두 가지 관례에 따라 만들어진 오브젝트를 가리킴
    1. 디폴트 생성자
        - 자바빈은 **파라미터가 없는** **디폴트 생성자**를 갖고 있어야 한다. 프레임워크에서 리플렉션을 이용하여 오브젝트를 생성하기 때문
        
        <aside>
        💡 리플렉션 : 구체적인 클래스 **타입**을 알 수 없어도, 클래스의 변수 및 메소드 등을 접근할 수 있도록 하는 API. 라이브러리에서 사용자가 어떤 클래스를 만들지 모르기 때문에 리플렉션을 사용하여 접근할 수 있도록 함. ex) DI
        참고[[https://dublin-java.tistory.com/53](https://dublin-java.tistory.com/53)]
        
        </aside>
        
    2. 프로퍼티
        - 자바빈이 노출하는 이름을 가진 속성
        - 프로퍼티는 getter, setter로 조회 및 수정 가능

1.1.3 User 클래스 생성

- 사용자 정보 저장

| 필드명 | 타입 | 설정 |
| --- | --- | --- |
| id | VARCHAR(10) | Primary Key |
| name | VARCHAR(10) | Not Null |
| password | VARCHAR(10) | Not Null |

```sql
create table users(
	id varchar(10) primary key,
    name varchar(10) not null,
    password varchar(10) not null
)
```

```java
public class User {
    String id;
    String name;
    String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

1.1.4 UserDao

- jdbc 연결 순서
    1. DB 연결을 위한 Connection을 가져온다
    2. SQL을 담은 Statemet를 만든다
    3. 만들어진 Statement를 싱행항다
    4. 조회의 경우(Get) SQL 실행 결과를 ResultSet으로 받아서 정보를 저장할 오브젝트(User)에 옮긴다.
    5. 작업 줒에 생성된 Connection, Statement, ResultSet 같은 리소스는 작업을 마친후 반듯이 닫아준다.
    6. JDBC API가 만들어내는 예외를 잡아서 직접 처리하고나, 메소드에 throws를 선언해야 예외 발생시 메소드 밖으로 던진다.

```groovy
runtimeOnly 'mysql:mysql-connector-java:8.0.19'
```

```java
public class UserDao {
    public void add(User user) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection c = DriverManager.getConnection(
                "jdbc:mysql://localhost/toby?serverTimezone=UTC"
                , "root", "1234"
        );
        PreparedStatement ps = c.prepareStatement(
                "insert into users(id, name, password) value (?,?,?)"
        );
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());
        ps.executeUpdate();
        ps.close();
        c.close();
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection c = DriverManager.getConnection(
                "jdbc:mysql://localhost/toby?serverTimezone=UTC"
                , "root", "1234"
        );
        PreparedStatement ps = c.prepareStatement(
                "select * from users where id = ?"
        );
        ps.setString(1,id);

        ResultSet rs = ps.executeQuery();
        rs.next();
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));

        rs.close();
        ps.close();
        c.close();
        return user;
    }
}
```

1.1.5 연결 테스트

```java
public class Main {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        UserDao dao = new UserDao();

        User user = new User();
        user.setId("ksb 1");
        user.setName("ksb");
        user.setPassword("ksb-pwd");

        dao.add(user);

        System.out.println(user.getId() + "등록");

        User user2 = dao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());

        System.out.println(user2.getId() + "조회");

    }
}
```