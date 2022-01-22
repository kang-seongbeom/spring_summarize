# 6.5 스프링의 AOP

6.5.1 프록시 생성의 문제

- 부가기능이 타깃 오브젝트마다 새로 만들어지는 문제는 `ProxyFactoryBean`의 어드바이스를 통해 해결 되었음
- 남은 문제는 부가기능의 적용이 필요한 타깃 오브젝트마다 비슷한 내용의 `ProxyFactoryBean` 빈 설정정보(XML)를 추가해야하는 부분임
- **빈 후처리기**를 사요하면 설정정보를 자동으로 추가할 수 있음

6.5.2 빈 후처리기를 이용한 자동 프록시 생성기

- 스프링은 컨테이너로서 제공하는 기능 중에서 변하지 않는  핵심적인 부분**외**에 대부분 확장할 수 있도록 **확장 포인트**를 제공하고 있음
- 관심을 가질만한 확장 포인트는 `BeanPostProcessor` 인터페이스를 **구현**해서 만드는 **빈 후처리기**임
- 스프링은 **빈 후 처리기가 빈으로 등록**되어 있으면 빈 오브젝트가 생성될 때 마다 빈 후처리기에 보내서 **후처리 작업을 요청**함
- 빈 후처리기는 후처리 작업을 함으로써 빈 오브젝트의 프로퍼티를 강제로 수정하거나 별도의 초기화 작업을 할 수 있음
- 또한, 설정정보를 통해 만들어진 빈 오브젝트가 아닌 다른 오브젝트를 빈으로 등록할 수 있음
- 이를 활용하여 스프링이 생성하는 빈 오브젝트의 일부를 프록시로 포장하고, **프록시를 빈으로 대신 등록**할 수 있음
- 이것이 **자동 프록시 생성 빈 후처리기**임
- 스프링이 제공하는 빈 후처리기중 하나인 `DefaultAdvisorAutoProxyCreator`를 사용할 것임
- `DefaultAdvisorAutoProxyCreator`가 빈으로 등록되어 있으면 스프링은 빈 오브젝트를 만들 때마다 후처리기에 빈을 보냄
- `DefaultAdvisorAutoProxyCreator`는 빈으로 등록된 모든 어드바이저 내의 포인트 컷을 이용해 빈이 프록시 적용 대상인지 확인함
- 프록시 적용 대상이면 내장된 프록시 생성기에 의해 프록시를 만들고, 어드바이저를 연결함
- 프록시가 만들어지면 컨테이너가 전달해준 빈 오브젝트 대신 프록시 오브젝트를 컨테이너에 반환함

![https://mblogthumb-phinf.pstatic.net/20161013_67/kbh3983_1476335214473XrMkg_PNG/aa.PNG?type=w800](https://mblogthumb-phinf.pstatic.net/20161013_67/kbh3983_1476335214473XrMkg_PNG/aa.PNG?type=w800)

6.5.3 확장된 포인트컷

- 앞에서 포인트컷은 **메소드**에 부가기능을 적용할지 선정하는 역할이라 했음
- 위에서 포인트컷으로 **오브젝트**인 빈이 프록시 적용 대상인지 확인한다 했으니 말의 앞뒤가 맞지 않음
- 사실, 포인트컷은 **클래스 필터** 및 **메소드 매처** 두 가지 기능을 가지고 있음
    
    [https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbmluaq%2FbtqPAucOeoQ%2FX9KBo0B6sUMe64961sK45k%2Fimg.png](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fbmluaq%2FbtqPAucOeoQ%2FX9KBo0B6sUMe64961sK45k%2Fimg.png)
    
- 클래스 필터로 적용 대상 오브젝트를 확인하고 조건에 맞으면, 매소드 매처로 적용 대상 메소드를 확인함
- 앞서 사용한 `NameMatchMethodPointCut` 는 메소드만 선정하기 위해 모든 클래스를 받아들이도록 만들어진 특별한 포인트컷임
- 앞서 사용한 `ProxyFactoryBean`에서 `NameMatchMethodPointCut`를 사용한 이유는 이미 타킷 오브젝트가 정해졌기 때문임
- 빈 후처리기인 `DefaultAdvisorAutoProxyCreatorDefaultAdvisorAutoProxyCreator`는 클래스 및 메소드 선정 알고리즘을 모두 갖고있는 포인트컷이 필요함
- 정확히는 그런 포인트컷과 어드바이스가 결합된 **어드바이서**가 필요함

6.5.4 포인트컷 테스트

- 포인트컷에 대한 학습 테스트임
- `NameMatchMethodPointCut`는 클래스 필터 기능이 없음
    
    <aside>
    💡 정확히는 모든 클래스를 수용하는 클래스 필터를 가지고 있는것임
    
    </aside>
    
- 때문에 `NameMatchMethodPointCut`를 **확장**하여 클래스 필터를 추가한 포인트컷을 만들어 테스트 하는 것임

```java
public class DynamicProxyTest {
		...
		@Test
    public void classNamePointcutAdvisor(){
        NameMatchMethodPointcut classMethodPointcut = new NameMatchMethodPointcut(){
            @Override
            public ClassFilter getClassFilter() {
                return new ClassFilter() {
                    @Override
                    public boolean matches(Class<?> clazz) {
                        //class 이름이 HelloT로 시작하는 것만 선정
                        return clazz.getSimpleName().startsWith("HelloT");
                    }
                };
            }
        };
        classMethodPointcut.setMappedName("sayH*"); //메소드 매처

        //테스트
        checkAdviced(new HelloTarget(), classMethodPointcut, true);

        class HelloWorld extends HelloTarget{};
        checkAdviced(new HelloWorld(), classMethodPointcut, false);

        class HelloToby extends HelloTarget{};
        checkAdviced(new HelloToby(), classMethodPointcut, true);
    }

		private void checkAdviced(Object target, Pointcut pointcut,
                              boolean adviced) {
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(target);
        pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));
        Hello proxiedHello = (Hello) pfBean.getObject();

        if(adviced){ //적용 대상
            assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));
            assertThat(proxiedHello.sayHi("Toby"), is("HI TOBY"));
            assertThat(proxiedHello.sayThankYou("Toby"), is("Thank You Toby"));
        }else{ //적용 대상 아님
            assertThat(proxiedHello.sayHello("Toby"), is("Hello Toby"));
            assertThat(proxiedHello.sayHi("Toby"), is("Hi Toby"));
            assertThat(proxiedHello.sayThankYou("Toby"), is("Thank You Toby"));
        }
    }
}
```

6.5.5 클래스 필터를 적용한 포인트컷 작성

- `NameMatchMethodPointCut`를 상속하고, ClassFilter 추가
- 내부 클래스로 `SimpleClassFilter` 생성
- `PatternMatchUtils`의 simpleMatch()는 와일드카드(*)가 들어간 문자열 비교를 지원하는 스프링 유틸리티 메소드임

```java
public class NameMatchClassMethodPointcut extends NameMatchMethodPointcut {
    public void setMappedClassName(String mappedClassName){
        this.setClassFilter(new SimpleClassFilter(mappedClassName));
    }

    private class SimpleClassFilter implements ClassFilter {
        String mappedName;

        public SimpleClassFilter(String mappedClassName) {
            this.mappedName = mappedClassName;
        }

        @Override
        public boolean matches(Class<?> clazz) {
            return PatternMatchUtils.simpleMatch(mappedName,
                    clazz.getSimpleName());
        }
    }
}
```

6.5.6 어드바이저를 이용하는 자동 프록시 생성기 등록

- `DefaultAdvisorAutoProxyCreator`를 빈에 등록하면 **자동으로** 등록된 빈 중에서 Advisor 인터페이스를 구현한 빈을 모두 찾음
- 찾은 빈에 포인트컷을 적용하여 프록시 적용 대상 선정을 함
- 프록시 생성이 끝나면 기존의 빈 오브젝트와 바꿔치기 함
- `DefaultAdvisorAutoProxyCreator` 등록은 한줄이면 충분함
- `DefaultAdvisorAutoProxyCreator`를 참조하는 빈은 존재하지 않기 때문에 id가 없음

```xml
<beans
		...>
		<bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"/>
<beans/>
```

6.5.7 포인트컷 등록

- 기존의 포인트컷 설정을 삭제하고 새로만든 클래스 필터 지원 포인트컷 등록

```xml
<beans
		...>
		<bean id="transactionPointcut"
          class="com.ksb.spring.NameMatchClassMethodPointcut">
        <property name="mappedClassName" value="*ServiceImpl"/>
        <property name="mappedName" value="upgrade*"/>
    </bean>
<beans/>
```

6.5.8 어드바이스와 어드바이저

- 기존의 어드바이스 및 어드바이저를 수정할 필요가 없음
- 기존에 등록된 transactionAdvisor는 `DefaultAdvisorAutoProxyCreator`가 빈으로 등록됐으므로 자동으로 후처리가 될 것임
- 즉, transactionAdvisor를 명시적으로 DI하는 빈은 존재하지 않음

6.5.9 ProxyFactoryBean 제거와 서비스 빈의 원상복구

- 더이상 명시적으로 프록시 팩토리 빈을 등록하지 않음
- 프록시를 도입했던 때부터 아이드를 바꾸고 프록시에 DI돼서 간접적으로 사용됐던 UserServiceImpl 빈의 아디를 다시 UserService로 되돌림

```xml
<beans
		...>
		<bean id="userService" class="com.ksb.spring.UserServiceImpl">
        <property name="userDao" ref="userDao"/>
        <property name="mailSender" ref="mailSender"/>
    </bean>
<beans/>
```

6.5.10 자동 프록시 생성기를 사용하는 테스트

- 기존의 upgradeAllOrNothing()의 테스트에 문제가 생김
- 현재까지 예외상황을 위한 테스트를 하기 위해 수동 DI로 구성을 바꿨음
- 자동 프록시 생성기를 적용한 후 더 이상 가져올 `ProxyFactoryBean` 같은 팩토리 빈이 존재하지 않음
- 때문에 강제 예외 발생용 `TestUserService`를 직접 빈으로 등록
- `TestUserService`에는 두 가지 문제가 존재함
    1. `UserServiceImpl`의 스태틱 내부 클래스
        
        ```java
        public static class TestUserService extends UserServiceImpl {
        ```
        
    2. 클래스 필터 조건이 “*ServiceImpl”임
        
        ```xml
        <property name="mappedClassName" value="*ServiceImpl"/>
        ```
        
- 첫 번째는 내부 클래스를 빈에 등록하기 위해 “$”사용
- 두 번째는 클래스 필터 조건에 맞게 클래스 이름 수정
- tesetUserService의 parent를 통해 userService 빈의 설정 내용을 상속 받을 수 있음
- 상속을 받아 userDao나 mailSender 프로퍼티를 지정할 필요가 없음

```java
public class UserServiceImpl implements UserService {
		...
		public static class TestUserServiceImpl extends UserServiceImpl {
				//예외를 위해 user의 세 번째 값 id로 고정
        private String id = "k2";

        @Override
        protected void upgradeLevel(User user) {
            if (user.getId().equals(this.id)) throw new TestUserServiceException();
            super.upgradeLevel(user);
        }
    }
}

public class UserServiceTest {
    @Autowired
    UserService userService;

    @Autowired
    UserService testUserService;
		...
		@Test
    public void upgradeAllOrNothing() {
        userDao.deleteAll();
        for (User user : users) userDao.add(user);

        try {
            this.testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        } catch (UserServiceImpl.TestUserServiceException e) {
        }

        checkLevelUpgraded(users.get(0), false);
    }
}
```

```xml
<beans
		...>
		<bean id="testUserService"
          class="com.ksb.spring.UserServiceImpl$TestUserServiceImpl"
          parent="userService">
    </bean>
<beans/>
```