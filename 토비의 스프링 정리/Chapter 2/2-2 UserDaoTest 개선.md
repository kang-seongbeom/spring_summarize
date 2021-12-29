# 2.2 UserDaoTest 개선

2.2.1 테스트 검증의 자동화

- add()와 get()을 통해 다시 DB에서 가져온 User 오브젝트의 정보가 정확히 일치하는가를 테스트
- add()를 통한 등록 자체는 별다르게 검증할 것이 없음. add()이후 별다른 에러가 발생하지 않으면 성공으로 간주
- add()를 통해 등록이 되지 않았다면, get()을 통해 가져오지 못할 것 이므로 get()을 통해 add()의 작업도 함께 확인
- 테스트 실패 종류
    1. 테스트 에러
        - 테스트가 진행되는 동안에 에러가 발생해서 실패
    2. 테스트 실패
        - 에러가 발생하진 않았지만, 결과가 기대한 것과 다르게 나오는 경우
- main()은 제어권을 **직접** 가지므로, 프레임워크의 기본 동작원리인 IoC가 아니다. 따라서 프레임워크(스프링)에 main()을 사용하는 테스트는 적합하지 않음
- Java에서는 **JUnit 프레임워크**를 통해 테스트 검증의 자동화 가능

2.2.2 JUit 프레임워크 요구조건 두 가지

1. 메소드가 public으로 선언되어야 함
2. @Test 애노테이션을 붙여야 함

```java
public class UserDaoTest {
    @Test
    public void addAndGet() throws SQLException, ClassNotFoundException {
		    ...
		}
}
```

2.2.3 JUnit을 사용한 검증 코드

- JUnit에서 제공하는 static 메서드인 **asserThat()**을 사용하여 테스트 상에서 오브젝트 **비교**
- assertThat 메소드의 첫 번째 파라미터와 두 번째 파라미터를 matcher 조건으로 비교
- 비교시 일치하지 않으면 테스트 실패
- is()는 matcher의 일종으로 equals() 처럼 비교 기능

```java
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UserDaoTest {
    @Test
    public void addAndGet() throws SQLException, ClassNotFoundException {
        ApplicationContext applicationContext =
                new GenericXmlApplicationContext("applicationContext.xml");
        UserDao dao = applicationContext.getBean("userDao", UserDao.class);

        User user = new User();
        user.setId("ksb 1");
        user.setName("ksb");
        user.setPassword("ksb-pwd");

        dao.add(user);

        System.out.println(user.getId() + "등록");

        User user2 = dao.get(user.getId());

        assertThat(user2.getName(), is(user.getName()));
        assertThat(user2.getPassword(), is(user.getPassword()));
    }
}
```

2.2.4 JUnit 테스트 실행

- JUnit 프레임워크 역시 스프링 컨테이너처럼 자바 코드로 만들어진 프로그램이므로 어디선가 한 번은 JUnit 프레임워크를 시작해줘야 함
- main()에 JUnitCore 클래스를 호출하여 테스트 할 수 있도록 함
- 실행 종료시 몇 개의 테스트가 실패 했는지 알 수 있음
- 또한, 테스트 코드에서 검증에 실패한 위치를 알 수 있음
- assertThat() 통한 검증을 통해 기대한 결과가 아닐 시, AssertionError 발생

```java
import org.junit.runner.JUnitCore;

public class Main {
    public static void main(String[] args){
        JUnitCore.main("com.ksb.spring.UserDaoTest");
    }
}
```