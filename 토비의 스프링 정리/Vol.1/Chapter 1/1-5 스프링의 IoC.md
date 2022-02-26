# 1.5 스프링의 IoC

1.5.1 애플리케이션 컨텍스트와 설정정보

- 스프링이 제어권을 가지고 직접 만들고 관계를 부여하는 오브젝트를 **빈(Bean)**이라 함
- 빈은 오브젝트 단위의 애플리케이션 컴포넌트 및 IoC가 적용된 오브젝트
- 스프링에서 빈의 생성과 괄계설정 같은 제어 정보를 담당하는 IoC오브젝트를 **빈 팩토리(Bean Factory)**라 한다
- **애플리케이션 컨텍스트(Application Context)**는 빈 팩토리를 상속받아 구현 하였는데, 실제 빈 팩토리보다 애플리케이션 컨텍스트를 더 많이 사용한다.
- 애플리케이션 컨텍스트는 IoC 방식을 따라 만들어진 일종의 빈 팩토리라 할 수 있음
- 애플리케이션 컨텍스트는 별도의 **설정정보(Configration)**를 참고해서 빈의 생성, 광계설정 등의 **제어작업**을 총괄함
- 애플리케이션 컨텍스트는 설정정보를 담고 있는 무엇인가를 가져와 이를 활용하는 범용적인 IoC 엔진
- 설정정보는 애플리케이션 로직을 담당하지 않지만, IoC 방식을 이용해 애플리케이션 컴포너트를 생성하고 사용할 관계를 맺는 **책임**을 가짐

1.5.2 DaoFactory를 사용하는 애플리케이션 컨텍스트

- @Configration 애노테이션을 사용하여 설정정보 추가
- @Bean 애노테이션을 사용하여 오브젝트를 만들 수 있게 함
- 두 가지 애노테이션만으로 스프링 프레임워크의 **빈 팩토리** 또는 **애플리케이션 컨텍스트**가 **IoC 방식**의 기능을 제공할 **설정정보**가 됨
- getBean()은 ApplicationContext가 관리하는 오브젝트 요청 메서드
- getBean()의 **첫 번째 파라미터**를 바탕으로 설정정보(Configraion)에 등록된 빈(bean) 중에서 첫 번째 파라미터와 동일한 이름의 빈을 찾아 반환
- 기본적으로 반환되는 타입은 오브젝트(Object)이며, **두 번째 파라미터**를 통해 형변환할 수 있음
- AnnotationConfigApplicationContext은 자바 코드에서 애노테이션으로 등록된 빈을 찾음

```java
@Configuration
public class DaoFactory {
    @Bean
    public UserDao userDao() {
        return new UserDao(connectionMaker());
    }

    @Bean
    public DConnectionMaker connectionMaker() {
        return new DConnectionMaker();
    }
}

public class UserDaoTest {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        //UserDao dao = new DaoFactory().userDao();
        ApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(DaoFactory.class);
        UserDao dao = applicationContext.getBean("userDao", UserDao.class);
				...
		}
}
```

1.5.3 팩토리 방식과 비교하여 애플리케이션 컨텍스트를 사용했을 때의 장점

1. 클라이언트는 구쳊거인 팩토리 클래스를 알 필요가 없다.
    - 기존 팩토리 방식은 어떤 팩토리 클래스를 사용하는지 **알아야** 하고, 필요할 때마다 팩토리 오브젝트를 **생성**해야 함
    - 애플리케이션 컨텍스트를 이용하면 **일관된 방식**으로 원하는 오브젝트를 가져올 수 있음
    - XML처럼 단순한 방법을 사용하여 IoC 설정정보를 만들 수 있음
2. 애플리케이션 컨텍스트는 종합 IoC 서비스를 제공해준다.
    - 오브젝트 생성 방식, 시점, 전략 등을 다르게 가져갈 수 있음
    - 자동생성, 후처리, 정보의 조합, 설정 방식의 다변화, 인터셉팅 등 오브젝트를 효과적으로 활용할 수 있는 다양한 기능 제공
    - 기반기술 서비스나 외부 시스템과의 연동을 **컨테이너 차원**에서 제공
3. 애플리케이션 컨텍스트는 빈을 검색하는 다양한 방법을 제공한다.
    - getBean() 메소드를 사용하여 빈을 찾을 수 있음
    - 타입만으로 빈을 검색하거나 특별한 애노테이션 설정이 되어잇는 빈을 찾을 수 있음
    

1.5.4 스프링 IoC 용어 정리

1. 빈(Bean)
    - 스프링이 IoC 방식으로 관리하는 오브젝트
    - 스프링을 사용하는 애플리케이션에서 만들어 지는 모든 오브젝트가 전부 빈은 아님
    - 스프링이 직접 생성과 제어를 담당하는 오브젝트만이 빈임
2. 빈 팩토리(Bean Factory)
    - 스프링의 IoC 를 담당하는 핵심 컨테이너
    - 빈을 **등록, 생성, 조회, 관리** 등의 역할을 함
    - 보통 빈 팩토리를 사용하지 않고 빈 팩토리를 확장항 애플리케이션 컨텍스트를 이용함
    - BeanFactory는 빈 팩토리가 구현하고 있는 가장 일반적인 인터페이스
3. 애플리케이션 컨텍스트(Application Context)
    - 빈 팩토리를 확장한 IoC 컨테이너
    - 기본적 기능은 빈 팩토리와 동일
    - 추가로 스프링이 제공하는 각종 부가 서비스 제공
    - 애플리케이션 컨텍스트라고 할 때는 스프링이 제공하는 애플리켕션 지원 기능을 모두 포함한 것
    - ApplicationContext는 애플리케이션 컨텍스트가 구현해야 하는 기본적인 인터페이스
    - ApplicationContext는 BeanFactory를 상속받고 있음
    - 애플리케이션 컨텍스트 오브젝트는 보통 하나의 애플리케이션에 여래개가 존재하여 이를 통틀어서 스프링 컨테이나라고도 함
4. 설정정보/설정 메타정보(Configration metadata)
    - 애플리케이션 컨텍스트 또는 빈 팩토리가 IoC를 적용하기 위해 사용하는 메타정보
    - 형상정보, 청사진(blue print) 라고도 함
    - 주로 IoC 컨테이너에 으해 관리도는 애플리케이션 오브젝트를 생성하고 구성할 때 사용 됨
5. 컨테이너(container) 또는 IoC 컨테이너
    - 애플리케이션 컨텍스트나 빈 팩토리를 컨테이너 또는 IoC 컨테이너 라고도 함
    - 컨테이너 말 자체가 IoC 개념을 담고 있음
6. 스프링 프레임워크
    - IoC 컨테이너, 애플리케이션 컨텍스트를 포함해서 스프링이 제공하는 모든 기능을 통틀어서 말할 때 스프링 프레임워크라 함
    - 줄여서 스프링이라 함