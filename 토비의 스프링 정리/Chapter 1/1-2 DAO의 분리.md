# 1.2 DAO의 분리

1.2.1 관심사의 분리(Separation of Concerns)

- 관심사를 분리하면 코드 변화에 효과적으로 대체할 수 있음
- 객체제향 기술이 만들어내는 가상의 추상세계 자체를 효과적으로 구성 가능
- 자유롭고 편리하게 **변경, 발전, 확장**할 수 있음
- 관심사의 분리가 가능한 이유는, 모든 변경과 발전은 **한 번에 한 가지** 관심사항에 집중해서 발생하기 때문

1.2.2 리팩토링

- 기존의 코드를 외부의 동작방식에느 변화 없이 내부 구조를 변경해서 재구성하는 기술
- 용이, 견고, 유연한 제품을 개발 할 수 있게 됨
- **관심사** 하나하나를 리팩토링 하여 **분리**시킴

1.2.3 UserDao의 관심사항

1. DB와 연결된 관심
2. 사용자 정보를 Statement에 바인딩(DB연결) 하고, Statement에 담긴 SQL을 DB를 통해 실행
3. Objects 닫기

1.2.4 메소드 추출(Extract Method)

- DB연결하는 코드가 중복되므로 DB 연결 **관심**을 분리하여 메소드 추출(Extract Method)

```java
public class UserDao {
    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = getConnection();
        ...
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        Connection c = getConnection();
				...
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost/toby?serverTimezone=UTC"
                , "root", "1234"
        );
    }
}
```

1.2.5 메소드 확장

- **템플릿 메소드** 디자인 패턴을 사용하여 getConnection()을 추상 메소드로 구현
- UserDao는 Connection 인터페이스만을 바라볼 수 있게 됨
- **팩토리 메소드** 디자인 패턴을 사용하여 서브클래스에서 구체적 구현

```java
public abstract class UserDao {
		/*템플릿 메소드 시작*/
    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = getConnection();
        ...
    }
		/*템플릿 메소드 끝*/

		/*템플릿 메소드 시작*/
    public User get(String id) throws ClassNotFoundException, SQLException {
        Connection c = getConnection();
				...
    }
		/*템플릿 메소드 끝*/
		
		/*팩토리 메소드 시작*/
    private abstract Connection getConnection() 
					throws ClassNotFoundException, SQLException;
		/*팩토리 메소드 끝*/
}

public class NUserDao extends UserDao{
	private abstract Connection getConnection() 
					throws ClassNotFoundException, SQLException{
		//연결 구현
	}
}

public class DUserDao extends UserDao{
	private abstract Connection getConnection() 
					throws ClassNotFoundException, SQLException{
		//연결 구현
	}
}
```

![https://media.vlpt.us/images/andy230/post/fbd8b479-2741-4047-b5aa-672e9203f7d4/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202021-03-06%20%EC%98%A4%ED%9B%84%202.25.06.png](https://media.vlpt.us/images/andy230/post/fbd8b479-2741-4047-b5aa-672e9203f7d4/%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202021-03-06%20%EC%98%A4%ED%9B%84%202.25.06.png)

1.2.6 템플릿 메소드(Template Method)

- 기본 알고리즘 골격을 담은 메소드
    
    ```java
    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = getConnection();
        ...
    }
    ```
    
- 기능의 일부를 추상 메소드나 오버라이딩이 가능한 protected 메소드 등으로 만든 뒤 서브클래스에서 이런 메소드를 필요에 맞게 구현해서 사용하도록 하는 디자인 패턴
- 상속을 통해 슈퍼클래스를 확장할 때 사용되는 대표적인 방법
- 디폴트 기능을 정의해두거나 비워뒀다가 서브클래스에서 선택적으로 오버라이드할 수 있도록 만들어둔 메소드를 훅(hook) 메소드라 한다.

```java
public abstract class Super{
	public void templateMethod(){
		hookMethod();
		abstractMethod();
	}
	protected void hookMethod(){} //선택적 구현
	public abstract void abstractMethod(); //필수 구현
}

public class Sub1 extends Super{
	protected void hookMethod(){
		...
	}
	public void abstractMethod(){
		...
	}
}
```

1.2.7 팩토리 메소드(Factory Method)

- 템플릿 메소드 패턴과 마찬가지로 상속을 통해 기능 함
- 서브클래스에서 오브젝트 생성 방법과 클래스를 결정할 수 있도록 미리 정의해둔 메소드.
    
    ```java
    private abstract Connection getConnection() 
    					throws ClassNotFoundException, SQLException;
    ```
    
- 슈퍼클래스는 서브클래스의 **리턴값을 사용**하지만, 서브클래스가 어떤 오브젝트를 리턴할지 **관심 없음.** 보통 **인터페이스**를 리턴함
    
    ```java
    Connection c = getConnection();
    ```
    
- 슈퍼클래스에서 기본 코드를 독립시키는 방법이 팩토리 메소드 패턴임

![https://media.vlpt.us/images/jakeseo_me/post/e1237c31-b890-4359-8ce3-3e5e3456a6e4/image.png](https://media.vlpt.us/images/jakeseo_me/post/e1237c31-b890-4359-8ce3-3e5e3456a6e4/image.png)

1.2.8 디자인 패턴

- 소프트웨어 설계 시 특정 상황에서 자주 만나는 묹를 해결하기 위해 사용할 수 있는 **재사용 가능한 솔루션**
- 디자인 패턴은 주로 객체지향 설계에 관한 것
- 각 패턴의 핵심이 담긴 목적 도는 의도가 가장 중요
- 패턴을 적용할 상황, 문제, 솔루션 구조와 각 요소의 역할 과 함께 핵심 의도가 무엇인기 기억해야함
- 두 가지 구조로 정의 할 수 있음
    1. 클래스 상속
    2. 오브제트 합성