# 1.4 제어의 역전(IoC)

1.4.1 오브젝트 팩토리

- UserDaoTest는 테스트를 하는 코드지만, ConnectionMaker을 구현하는 코드의 역할까지 있음
- 관심을 분리하여 UserDao 오브젝트 생성 및 ConnectionMaker 구현 클래스 오브젝트를 생성할 **DaoFactory 클래스** 생성
- DaoFactory는 **팩토리 클래스**임
    
    <aside>
    💡 팩토리 클래스 : 객체 **생성 방법 결정** 및 생성된 오브젝트 **반환**
    
    </aside>
    
- 팩토리를 사용하여 오브젝트를 생성(DaoFactory) 하는 쪽과 사용(UserDaoTest)하는 쪽을 분리

```java
public class DaoFactory {
    public UserDao userDao() throws SQLException, ClassNotFoundException {
        return new UserDao(connectionMaker());
    }
}

public class UserDaoTest {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        UserDao dao = new DaoFactory().userDao();
				...
		}
}
```

1.4.2 설계도로서의 팩토리

- UserDao와 ConnectionMaker은 애플리케이션의 핵심적 데이터, 기술 로직을담당
- DaoFactory는 애플리케이션의 오브젝트를 **구성**하고 그 **관계**를 정의하는 책임을 지님
- DaoFactory와 같은 로직이 애플리케이션 전체에 걸쳐 일어나면 컴포넌트의 의존관계에 대한 설계도와 같은 역할을 하게 됨
- **컴포넌트** 역할 오브젝트와 **애플리케이션 구조(설계도)**를 결정하는 오브젝트를 분리했다는 것에 가장 큰 의미가 있음

![https://media.vlpt.us/images/devsigner9920/post/18994736-9052-409f-88e1-df1a4e883355/IMG_B20387F2F8B2-1.jpeg](https://media.vlpt.us/images/devsigner9920/post/18994736-9052-409f-88e1-df1a4e883355/IMG_B20387F2F8B2-1.jpeg)

1.4.3 오브젝트 팩토리의 활용

- DaoFactory에 다른 DAO 생성 기능을 넣기 위해 userDao()를 복사해서 만든다면, 코드 중복의 문제가 발생
- 중복된 코드를 추출하여 변경사항 발생 시 하나의 메소드 내에서 처리할 수 있도록 함

```java
public class DaoFactory {
    public UserDao userDao() throws SQLException, ClassNotFoundException {
        return new UserDao(connectionMaker());
    }

    public UserDao accountDao() throws SQLException, ClassNotFoundException {
        return new UserDao(connectionMaker());
    }

    public UserDao messageDao() throws SQLException, ClassNotFoundException {
        return new UserDao(connectionMaker());
    }

    private DConnectionMaker connectionMaker() {
        return new DConnectionMaker();
    }
}
```

1.4.4 제어 역전(Ioc)

- 제어 흐름 구조가 뒤바뀌는 것
- 제어 역전에서의 오브젝트는 오브젝트 자신이 사용할 오브젝트를 스스로 선택 및 생성하지 않음
- 언제 어디서 사용되는지 알 수 없음
- main을 제외한 엔트리 포인트를 제외하면 모든 오브젝트는 **특별한 오브젝트(DaoFacotory)**에 의해 생성됨
- 웹에서 **서블릿**은 제어 권한을 가진 **컨테이너**가 적절한 시점에 서블릿 클래스의 오브젝트를 만들고, 그 안의 메소드를 호출
- 1.1 초난감 DAO에서 추상 UserDao를 상속한 서브클래스는 getConnection()을 **구현**함. 하지만 언제 사용될 지 모름
- 단지 구현만 하면 슈퍼클래스에서 **필요할 때 호출**해서 사용함
- 즉, **제어권을 상위 템플릿 메소드에 넘기고** 자신은 필요할 때 호출 되어서 사용되는 **템플릿 메소드 패턴**은 제어 역전 개념을 활용한 디자인 패턴임
- Ioc를 적용하면 설계가 **깔끔** 해지고, **유연성**이 증가하며, **확장성**이 좋아지게 됨
- Ioc에서는 프레임워크 또는 컨테이너와 같이 애플리케이션 컴포넌트의 생성, 관계 설정, 사용, 생명주기 관리 등을 관장하는 존재가 필요
- DaoFactory는 가장 단순한 Ioc 컨테이너 또는 Ioc 프레임워크라 할 수 있음\
- 스프링은 Ioc를 극한까지 적용한 프레임워크임

1.4.5 라이브러리와 프레임워크

1. 라이브러리
    - 라이브러리를 사용하는 애플리케이션 코드는 애플리케이션 흐름을 직접 제어
    - 필요한 기능이 있을 때 **능동적**으로 라이브러리를 사용
2. 프레임워크
    - 애플리케이션 코드가 프레임워크에 의해 사용됨
    - 프레임워크가 흐름을 주도하는 중에 개발자가 만든 애플리케이션 코드를 사용하는 방식
    - 애플리케이션 코드는 프레임워크가 짜놓은 틀에서 **수동적**으로 동작
    - UserDao와 ConnectionMaker의 구현체를 생성하는 책임을 DaoFactory가 가지고 있음
    - UserDao는 DaoFactory에게 어떤 ConnectionMaker을 만들고 사용할지 제어권을 넘김. 따라서, UserDao는 수동적인 존재가 되었음