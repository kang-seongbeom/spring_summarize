# 1.3 DAO의 확장

1.3.1 클래스의 분리

- DB연결 메소드를 클래스로 분리한다
- 이 방법은 DB연결 방식이 달라질 때 마다 새로운 클래스를 만들어 UserDao에 추가해야함(UserDao에 디자인 패턴을 적용시킨 것보다 못함)

![https://user-images.githubusercontent.com/40616436/75679116-5e1bf700-5cd2-11ea-8700-f176678ca17b.png](https://user-images.githubusercontent.com/40616436/75679116-5e1bf700-5cd2-11ea-8700-f176678ca17b.png)

```java
public class SimpleConnectionMaker {
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost/toby?serverTimezone=UTC"
                , "root", "1234"
        );
    }
}

public class UserDao {
    private SimpleConnectionMaker simpleConnectionMaker;

    public UserDao(){
        simpleConnectionMaker = new SimpleConnectionMaker();
    }

    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = simpleConnectionMaker.getConnection();
				...
		}
}
```

1.3.2 인터페이스의 도입

- 클래스 사이간 추상적 느슨한 결합을 위해 인터페이스 사용
- 인터페이스는 어떤 일을 하겠다는 기능만 정의해놓은 것
- 인터페이스를 도입 함으로써 UserDao는 기능에만 관심을 가지지, 기능을 어떻게 구현했는지에 관심을 가지지 않게 됨
- 하지만, UserDao는 특정(DConnectionMaker) 오브젝트를 생성자에서 구현하게 됨
- new DConnectionMaker()는 매우 간단하지만, 그 자체로 충분히 독립적인 관심사임
- **UserDao**는 DB 연결이 아닌, **SQL문 생성 및 실행만 관심**을 가져야함
- 이러한 결과는 **UserDao가 필요 없는 관심과 의존을 가지게 됨**

![https://i.imgur.com/iCN1k2t.png](https://i.imgur.com/iCN1k2t.png)

![https://leejaedoo.github.io/assets/img/%EB%B6%88%ED%95%84%EC%9A%94%ED%95%9C%EC%9D%98%EC%A1%B4%EA%B4%80%EA%B3%84.jpeg](https://leejaedoo.github.io/assets/img/%EB%B6%88%ED%95%84%EC%9A%94%ED%95%9C%EC%9D%98%EC%A1%B4%EA%B4%80%EA%B3%84.jpeg)

```java
public interface ConnectionMaker {
    Connection getConnection() throws ClassNotFoundException, SQLException;
}

public class NConnectionMaker implements ConnectionMaker {
    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        //연결구현
    }
}

public class DConnectionMaker implements ConnectionMaker {
    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        //연결구현
    }
}

public class UserDao {
    private ConnectionMaker connectionMaker;

    public UserDao(){
        connectionMaker = new DConnectionMaker(); //여기서 문제 발생
    }

    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = connectionMaker.getConnection();
				...
		}
}
```

1.3.3 관계설정 책임의 분리

- UserDao의 오브젝트(클래스가 아닌 객체)와 ConnectionMaker가 구현된 오브젝트(NConnectionMaker 또는 DConnectionMaker)의 관계를 설정해 주어야함
- **클래스와 클래스**가 아닌 **오브젝트 와 오브젝트** 사이의 관계를 설립해 줘야함
- 오브젝트 사이의 관계는 **런타임** 시에 한쪽이 다른 오브젝트의 레퍼런스를 갖는 방식임
- UserDao 역시 오브젝트로 생성이 되고나서 실행되는 것임
- UserDao가 실행되는 위치는 **UserDao 클라이언트**임
- **클라이언트**에서 **ConnectionMaker를 구현**하고 이를 **UserDao 생성자**에 전달
- 클라이언트 책임은 **런타임 오브젝트 관계**를 갖는 구조를 만들어 주는것임
    
    ![https://user-images.githubusercontent.com/40616436/75683933-5280fe00-5cdb-11ea-9c4c-a5a8ad5f3885.png](https://user-images.githubusercontent.com/40616436/75683933-5280fe00-5cdb-11ea-9c4c-a5a8ad5f3885.png)
    
- **DB 연결 관심**을 UserDao 클라이언트에게 떠넘길 것임
- UserDaoTest는 UserDao와 ConnectionMaker 구현 클래스와의 런타임 오브젝트 의존 관계를 설정하는 책임 담당
- DAO가 아무리 많아져도 DB 연결에 대한 관심은 오직 한 곳에 집중할 수 있음
- DB 연결은 ConnectionMaker를 상속받아 구현하고, 클라이언트가 생성하면 됨

![https://leejaedoo.github.io/assets/img/%EA%B4%80%EA%B3%84%EC%84%A4%EC%A0%95%EC%B1%85%EC%9E%84.jpeg](https://leejaedoo.github.io/assets/img/%EA%B4%80%EA%B3%84%EC%84%A4%EC%A0%95%EC%B1%85%EC%9E%84.jpeg)

```java
//클라리언트
public class UserDaoTest {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
				//생성자 파라미터로 관심 전달
        UserDao dao = new UserDao(new DConnectionMaker()); 
				...
}

public class UserDao {
    private ConnectionMaker connectionMaker;

    public UserDao(ConnectionMaker connectionMaker){
        this.connectionMaker = connectionMaker;
    }

    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = connectionMaker.getConnection();
				...
		}
}
```

1.3.5 개방 폐쇄 원칙

- 클래스나 모듈은 **확장**에는 열려있어야 하고, **변경**에는 닫혀있어야 한다.
- UserDao에 전혀 영향을 주지 않고 얼마든지 기능 확장(DB 연결)이 가능해졌음
- 기능 확장을 할 때 UserDao의 핵심 코드(get, add)는 영향을 받지 않고 있음

<aside>
💡 객체지향 설계 원칙(SOLID)
1. SRP(The Single Responsibility Principle) : 단일 책임 원칙
2. OCP(The Open Closed Principle) : 개방 폐쇄 원칙
3. LSP(The Liskov Substitution Principle) : 리스코프 치환 원칙
4. ISP(The Interface Segregation Principle) : 인터페이스 분리 원칙
5. DIP(The Dependency Inversion Principle) : 의존관계 역전 원칙

</aside>

1.3.6 높은 응집도, 낮은 결합도

1. 높은 응집도
    - 변화가 일어날 때 해당 모듈에서 변화하는 부분이 크다는 것
    - 모듈 일부만 변환시 변경 위치를 찾아야 하고, 변경시 다른 코드에 영향을 미치지 않는지 확인해야 하므로 변경 부분이 커야함
2. 낮은 결합도
    - 하나의 오브젝트가 변경이 일어날 때에 관게를 맺고 있는 다른 오브젝트에게 변화를 요구하는 정도
    - 모듈과 객체로 변경에 대한 요구가 전파되지 않는 상태가 낮은 결합도

1.3.7 전략 패턴(Strategy Pattern)

- UserDaoTest-UserDao-ConnectionMaker 구조를 디자인 패턴 시각으로 보면 **전략 패턴**임
- 개방 폐쇄 원칙과 가장 잘들어 맞는 패턴
- 자신의 맥락(Context)에서 인터페이스를 통해 통째로 외부로 분리하고, 이를 구현한 구체적인 알고리즘 클래스를 필요에 맞게 바꿔서 사용할 수 있게하는 디자인 패턴
- UserDao가 전략 패턴의 맥락(Context)임