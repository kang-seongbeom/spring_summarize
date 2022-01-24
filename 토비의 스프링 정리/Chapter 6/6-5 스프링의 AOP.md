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

6.5.11 포인트컷 표현식

- 지금까지 사용했던 포인트컷은 클래스 필터와 메소드 매처 오브젝트로 비교해서 선정하는 방식임
- 필터나 매처에서 클래스와 메소드의 메타정보를 제공받기 때문에 단순히 이름 비교를 넘어 복잡하고 세밀한 선정방식을 만들 수 있음
- 하지만, 조건이 달라질 때 마다 포인트컷 구현 코드를 직접 수정해야 하는 단점이 있음
- 또한, 클래스 필터와 메소드 매처 두 가지를 각각 제공해야함
- **포인트컷 표현식(Pointcut Expression)**은 간단하고 효과적인 포인트컷의 클래스와 메소드를 선정할 수있음
- 포인트컷 표현식은 클래스와 메소드 선정을 동시에 할 수 있음
- 스프링에서 포인트컷 표현식은 `AspectJExpressionPointcut`을 사용하면 됨
- 사실, `AspectJExpressionPointcut`은 `AspectJ`라는 유명 프레임워크에서 제공하는 것을 가져와 일부 문법을 확장해서 사용하는 것임
- 때문에 `AspectJ` **포인트컷 표현식**이라고도 함

6.5.12 포인트컷 표현식 문법

- AspectJ 포인트컷 표현식은 포인트컷 **지시자**를 이용해 작성함
- 대표적 지시자는 execution()이 있음
- []는 옵션 항목으로 생략 가능
- |는 OR 조건임
    
    ```java
    execution([접근제한자 패턴] 타입패턴 [타입패턴.]이름패턴 (타입패턴 | "..", ...) [throws 예외 패턴])
    ```
    
- 복잡해 보이지만 메소드 풀 시그니처를 문자열로 비교하는 개념과 비슷함
    
    ```java
    System.out.println(Target.class.getMethod("minus", int.class, int.class));
    
    //결과
    [public] int [springbook.learning.spring.pointcut.Target.]minus(int, int) 
    		[throws java.lang.RuntimeException]
    ```
    
- 출력 내용
    - public
        - 접근제한자
        - 생략시, 접근제한자의 조건을 부여하지 않는다는 의미임
    - int
        - 리턴값
        - 필수항목이며, 반드시 하나의 타입을 지정하거나 와일드카드(*)를 써 모든 타입을 선택하도록 해야함
    - springbook.learning.spring.pointcut.Target.
        - 패키지와 타입 이름을 포함한 **클래스의 타입 패턴**
        - 와일드 카드를 사용하거나, “..”를 사용하여 한 번에 여러 개의 패키지 선택 가능
    - minus
        - 메소드 이름 패턴
        - 모든 메소드 선택시 와일드카드 사용
    - (int, int)
        - 파라미터의 타입 패턴
        - “,”로 구분하면서 순서대로 적어야 함
        - “..”는 파라미터의 타입과 개수에 상관없이 모두 다 허용하는 패턴
        - “...”는 뒷부분의 파라미터 조건만 생략 가능
    - throws java.lang.RuntimeException
        - 예외 패턴으로 생략 가능 함
    

6.5.12 포인트컷 표현식 학습 테스트 - 1

- 포인트 컷 테스트용 클래스를 만들어 학습 테스트 진행
- 메소드 시그니처인 execution()안에 포인트컷 표현식을 작성
- 포인트컷 표현식 테스트이기 때문에 타깃 클래스는 null

```java
package com.ksb.spring.pointcutexpression;

public interface TargetInterface {
    void hello();
    void hello(String s);
    int minus(int a, int b) throws RuntimeException;
    int plus(int a, int b);
}

public class Target implements TargetInterface{
    @Override
    public void hello() {}

    @Override
    public void hello(String s) {}

    @Override
    public int minus(int a, int b) throws RuntimeException {
        return 0;
    }

    @Override
    public int plus(int a, int b) { return 0; }

    public void method() {}
}

public class Bean {
    public void method() throws RuntimeException{}
}

public class PointCutTest {
    @Test
    public void methodSignaturePointcut() throws NoSuchMethodException {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(public int " +
                "com.ksb.spring.pointcutexpression.Target.minus(int,int) " +
                "throws java.lang.RuntimeException)");

        //Target.minus
        //성공
        assertThat(pointcut.getClassFilter().matches(Target.class) &&
                pointcut.getMethodMatcher().matches(
                        Target.class.getMethod("minus", int.class, int.class),null
                ), is(true));

        //Target.plus
        //메소드 매처에서 실패
        assertThat(pointcut.getClassFilter().matches(Target.class) &&
                pointcut.getMethodMatcher().matches(
                        Target.class.getMethod("plus", int.class, int.class),null
                ), is(false));

        //Bean.method
        //클래스 필터에서 부터 실패
        assertThat(pointcut.getClassFilter().matches(Bean.class) &&
                pointcut.getMethodMatcher().matches(
                        Target.class.getMethod("method"),null
                ), is(false));
    }
}
```

6.5.12 포인트컷 표현식 학습 테스트 - 2

- 접근 제한자, 클래스 타입 패턴 등 옵션 부분을 생략하면 매우 간단해짐
    
    ```java
    execution(int minus(int,int))
    ```
    
- 단, 생략한 부분은 모든 경우를 다 허용하기 때문에 **느슨한 포인트컷**이 됐음
- 와일드 카드를 사용하면 더욱 간단한 포인트컷을 만들 수 있음
    
    ```java
    execution(* minus(int,int)) //리턴 타입 무시
    execution(* minus(..)) //파라미터 무시
    execution(* *(..)) //메소드 무시
    ```
    
- 다양한 활용법을 위해 테스트를 보충

```java
public class PointCutTest {
		...
    @Test
    public void pointcut() throws Exception{
        targetClassPointcutMatches("execution(* *(..))",
                true, true, true, true, true, true);
        //나머지 생략 표 6-1
    }

    private void targetClassPointcutMatches(String expression, boolean... expected)
            throws Exception{
        pointcutMatches(expression, expected[0], Target.class, "hello");
        pointcutMatches(expression, expected[1], Target.class, "hello", String.class);
        pointcutMatches(expression, expected[2], Target.class, "plus",int.class, int.class);
        pointcutMatches(expression, expected[3], Target.class, "minus",int.class, int.class);
        pointcutMatches(expression, expected[4], Target.class, "method");
        pointcutMatches(expression, expected[5], Bean.class, "method");
    }

    private void pointcutMatches(String expression, boolean expected,
                                 Class<?> clazz, String methodName, Class<?>... args)
            throws Exception {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(expression);

        assertThat(pointcut.getClassFilter().matches(clazz) &&
                pointcut.getMethodMatcher().matches(clazz.getMethod(methodName, args)
                        , null), is(expected));
    }
}
```

![https://mblogthumb-phinf.pstatic.net/20161013_148/kbh3983_1476364891621POdwT_PNG/aa.PNG?type=w800](https://mblogthumb-phinf.pstatic.net/20161013_148/kbh3983_1476364891621POdwT_PNG/aa.PNG?type=w800)

6.5.13 포인트컷 표현식을 이용하는 포인트컷 적용

- AspectJ 포인트컷 표현식은 메소드를 선정하는데 편리하게 쓸 수 있는 강력한 표현식 언어임
- execution() 외에도 몇 가지 표현식 스타일을 가지고 있음
- 대표적으로 bean()을 사용하면 괄호 내부 조건에 맞는 아이디를 만족하는 모든 빈을 선택함
    
    <aside>
    💡 bean(*Service)는 아이디가 Service로 끝나는 모든 빈을 선택함
    
    </aside>
    
- 또한 특정 애노테이션이 타입, 메소드, 파라미터에 적용되어 있는 것을 보고 메소드를 선정하는 포인트컷을 만들 수 있음
    
    <aside>
    💡 @annotation(org.springframework.transaction.annotation.Transactional)을 사용하면 애노테이션 중 @Transaction 애노테이션이 적용된 메소드를 선정
    
    </aside>
    
- 기존 xml에 존재하는 포인트컷은 클래스 이름, 메소드 이름에 대한 패턴임
    
    ```xml
    <bean id="transactionPointcut"
          class="com.ksb.spring.NameMatchClassMethodPointcut">
        <property name="mappedClassName" value="*ServiceImpl"/>
        <property name="mappedName" value="upgrade*"/>
    </bean>
    ```
    
- 포인트컷 표현식을 사용한 빈 설정은 value에 표현식 하나만 적으면 됨
    
    ```xml
    <bean id="transactionPointcut"
          class="org.springframework.aop.aspectj.AspectJExpressionPointcut">
        <property name="expression" value="execution(* *..*ServiceImpl.upgrade*(..))"/>
    </bean>
    ```
    

6.5.14 타입 패턴과 클래스 이름 패턴

- 이전에 네이밍을 통한 포인트컷 선정 때문에 `TestUserService`을 `TestUserServiceImpl`로 변경했음
- 현재 포인트컷 표현식 역시 *ServiceImpl임(단, `NameMatchMethodPointcut`을 사용하지 않고, `AspectJExpressionPointcut`으로 변경했음)
    
    ```xml
    <property name="expression" value="execution(* *..*ServiceImpl.upgrade*(..))"/>
    ```
    
- 여기서 `TestUserServiceImpl`를 `TestUserService`로 클래스 이름을 변경하면 표현식에 위반이 되니 테스트 실패할 것이라 생각 하겠지만, 사실 테스트가 성공해야 정상임
- 그 이유는 포인트컷 표현식의 **클래스 이름**에 적용되는 패턴은 클래스 이름 패턴이 아니라 **타입 패턴**이기 때문임
- `TestUserService`는 `UserServiceImpl`를 상속하고, `UserServiceImpl`는 `UserService`를 상속함
- 즉, `TestUserService`는 `UserServiceImpl`타입이라 할 수 있음
- 표 6-1의 16번에서 `TargetInterface` 인터페이스 표현식을 사용했을 때 `Target`오브젝트가 포인트컷에 선정된 이유가 바로 이 이유임
- `Target`은 `TargetInterface`를 구현했기 때문에 `Target` 오브젝트는 `TargetInterface` 타입임

6.5.15 UserService에 트랜잭션을 적용해온 과정

1. 트랜잭션 서비스 추상화
    - 비즈니스 로직에 트랜잭션 기술이 등장하면서 트랜잭션 기술 종속적인 문제가 발생
    - 트랜잭션 적용이라는 **추상적**인 작업 내용을 유지한 채로 구체적인 구현 방법을 자유롭게 바꾸기 위해 서비스 추상화 도입
    - 런타임 시에 다이내믹하게 연결하는 DI 방법을 활용한 접근 방법
2. 프록시와 데코레이터 패턴
    - 서비스 추상화를 하더라도 여전히 비즈니스 로직에 트랜잭션을 적용하고 있다는 사실이 드러나 있음
    - 단순한 추상화와 메소드 추출 방법으로 제거할 수 없음
    - DI를 사용해 데코레이터 패턴 적용
    - 클라이언트가 인터페이스와 DI를 통해 접근하도록 설계하고, 데코레이터 패턴을 적용해 비즈니스 로직을 영향주지 않으면서 **부가기능**을 자유롭게 부여하는 구조를 만듦
    - 데코레이터는 부가기능만 추가하고 비즈니스 로직은 타깃 또는 다른 데코레이터에 **위임**
    - 클라이언트가 프록시 역할을 하는 트랜잭션 데코레이터를 거쳐 타깃에 접근
3. 다이내믹 프록시와 프록시 팩토리 빈
    - 데코레이터 패턴으로 비즈니스 로직에서 트랜잭션 코드는 모두 제거할 수 있었음
    - 하지만, 트랜잭션 기능을 부여하는 코드를 일일이 만들고 트랜잭션 기능이 필요하지 않은 메소드조차 프록시로서 위임을 해야하기 때문에 전부 구현해야 함
    - **JDK 다이내믹 프록시** 기술을 적용해 부가기능 코드가 중복되는 문제 해결
    - 하지만, 동일한 기능의 프록시를 여러 오브젝트에 적용할 경우 오브젝트 단위로 중복이 발생함
    - JDK 다이내믹 프록시와 같은 프록시 기술을 추상화한 프록시 **팩토리 빈**을 이용해서 다이내믹 프록시 생성 방법에 DI 적용
    - 내부적으로 **템플릿/콜백** 패턴을 활요하는 스프링의 프록시 **팩토리 빈** 덕분에 부가기능을 담은 **어드바이스**와 부가기능 선정 알고리즘을 담은 **포인트컷**을 프록시에서 **분리**함
4. 자동 프록시생성 방법과 포인트컷
    - 여전히 트랜잭션 적용 대상이 되는 빈마다 일일이 프록시 팩토리 빈을 설정해야 하는 문제가 남았음
    - 스프링 컨테이너의 빈 생성 **후처리 기법**을 활용해 자동으로 프록시를 만드는 방법 도입
    - 어드바이스오하 프록시를 완전히 분리
    - 포인트컷 표현식을 사용해 포인트컷 클래스를 만들지 않아도 됐음
5. 부가기능의 모듈화

6.5.16 부가기능의 모듈화

- 지금까지 관심사가 같은 코드를 객체지향 설계 원칙에 따라 분리했음
- 덕분에 낮은 결합도를 가지며, 유연하게 확장을 할 수 있었음
- 하지만, 트랜잭션 적용 코드는 기존에 써왔던 방법으로는 간단히 분리해서 모듈화할 수 없었음
- 이유는 트랜잭션 경계설정 기능은 다른 모듈의 코드에서 **부가적**으로 부여되는 특징이 있기 때문
- 때문에 트랜잭션 코드를 한데 모을 수 없고, 애플리케이션 전반에 걸쳐 흩어져 있음
- 트랜잭션 같은 **부가기능**은 핵심기능과 같은 방식으로 모듈화하기 매우 힘듦
- **핵심기능**은 그 자체로 독립적으로 존재할 수 있으며, 독립적으로 테스트가 가능하고, 최소한의 인터페이스를 통해 다른 모듈과 결합해 사용할 수 있음
- 이와 대조적으로 **부가기능**은 핵심기능과 같은 레벨에서 독립적으로 존재할 수 없음
- 부가기능을 모듈화하기 위해 DI, 데코레이터 패턴, 다이내믹 프록시, 오브젝트 생성 후처리, 자동 프록시 생성, 포인트컷과 같은 기술을 사용했음

6.4.17 AOP : 애스펙트 지향 프로그래밍

- 트랜잭션과 같은 부가기능을 어떻게 모듈화할 것인가 연구한 사람들은, 부가기능 모듈화 작업이 기존의 객체지향 설계 패러다임과 구분과는 **새로운 특징**이 있다고 생각했음
- 이런 부가기능 모듈을 객체지향 기술에서 주로 사용되는 오브젝트와 다른 **애스펙트(Aspect)**라 부름
- 애스팩트란, 그 자체로 애플리케이션의 핵심기능을 담지 않지만, 애플리케이션을 구성하는 중요한 한 가지 요소이고, 핵심기능에 **부가**되어 의미를 갖는 **특별한 모듈**임
- 애스팩트는 부가될 기능인 **어드바이스**와, 어드바이스를 어디에 적용할지 결정하는 **포인트컷**을 함께 가지고 있음

[https://t1.daumcdn.net/cfile/tistory/24672E3754CB92800D](https://t1.daumcdn.net/cfile/tistory/24672E3754CB92800D)

- 그림 6-21 왼쪽은 부가기능을 분리하기 전의 상태임
- 그림 오른쪽은 부가기능을 독립적인 모듈인 **애스펙트**로 분리한 것임
- 2차원 평면적 구조를 3차우너 다면체 구조로 가져가면서 각각 성격이 다른 부가기능은 다른 면에 존재하도록 했음
- 이렇게 독립된 **측면**에 존재하는 애스펙트로 핵심기능은 순수하게 그 기능을 담은 코드만으로 존재함
- 여러 다른 **측면**에 존재하는 부가기능은 결국 핵심기능과 함께 어우러져서 동작함
- 이렇게 애플리케이션의 핵심적인 기능에서 부가적인 기능을 분리해서 애스펙트라는 독특한 모듈로 만들어서 설계하고 개발하는 방법을 **애스펙트 지향 프로그래밍(AOP, Aspect Oriented Programming)**라 함
- AOP는 OOP를 돕는 **보조적 기술**이지, OOP를 완전히 대체하는 새로운 개념은 아님
- AOP는 애스펙트를 분리함으로써 핵심기능을 설계하고 구현할 때 객체지향적인 가치를 지킬 수 잇도록 도와주는 것임
- AOP는 결국 애플리케이션을 **다양한 측면**에서 독립적으로 모델링하고, 설계하고, 개발할 수 있도록 만들어주는 것임
- 그로인해 애플리케이션을 **다양한 관점**에서 개발할 수 있게 도와줌
- AOP를 특정 관점을 기준으로 바라볼 수 있게 해준다는 의미로 **관점 지향 프로그래밍**이라고도 함

6.4.18 프록시를 이용한 AOP

- 스프링은 IoC/DI 컨테이너와 다이내믹 프록시, 데코레이터 패턴, 프록시 패턴, 자동 프록시 생성 기법, 빈 오브젝트의 후처리 조작 기법 등의 다양한 기술을 조합해 **AOP**를 지원하고 있음
- 프록시를 만들어서 DI로 연결된 빈 사이에 적용해 타킷의 메소드 호출 과정에 참여해서 **부가기능**을 제공하도록 했음
- 어드바이스가 구현하는 `MethodInterceptor` 인터페이스는 다이내믹 프록시의 `InvocationHandler`와 마찬가지로 프록시로부터 메소드 요청정보를 전달받아 타깃 오브젝트를 호출하는 전후에 다양한 부가기능을 제공함
- 다이내믹하게 적용해주기 위해 가장 중요한 역할을 맡고 있는 게 바로 **프록시**임
- 그래서 스프링 AOP는 프록시 방식의 AOP라고 할 수 있음

6.4.19 바이트코드 생성과 조작을 통한 AOP

- 프록시 방식 AOP가 대부분이나, 프록시 방식이 아닌 AOP 역시 존재함
- **AspectJ** 프레임워크가 대표적인 프록시 방식이 아닌 AOP 기술임
- AspectJ는 프록시처럼 간접적인 방법이 아닌, 타깃 오브젝트를 뜯어고쳐서 부가기능을 직접 넣어주는 **직접적인 방법**을 사용함
- 컴파일된 타깃 클래스의 파일 저체를 수정하거나 클래스가 JVM에 로딩되는 시점을 가로채서 **바이트 코드**를 조작함
- 물론, 소스코드를 수정하지 않으므로 개발자는 계속해서 비즈니스 로직에 충실할 수 있음
- 장점
    1. 스프링과 같은 DI 컨테이너의 도움을 박지 않아도 AOP 적용이 가능함
    2. 바이트 코드를 직접 조작해 오브젝트 생성, 필드값 조작 등 프록시보다 훨씬 강력하고 유연한 AOP 가능
    

6.4.20 AOP의 용어

1. 타깃
    - 부가기능을 부여할 대상
    - 핵심기능을 담은 클래스일 수 있지만, 경우에 따라 다른 부가기능을 제공하는 프록시 오브젝트 일 수 있음
2. 어드바이스
    - 타깃에 제공할 부가기능을 담은 모듈
    - 오브젝트로 정의하기도 하지만 메소드 레벨에서 정의 가능
    - `MethodInterceptor`처럼 메소드 호출 과정에 전반적으로 참여하는 것도 있지만, 예외가 발생했을 때만 동작하는 어드바이스처럼 메소드 호출 과정의 일부에서만 동작하는 어드바이스도 존재
3. 조인 포인트(Join Point)
    - 어드바이스가 적용될 수 있는 위치
    - 스프링의 프록시 AOP에서 조인 포인트는 메소드의 실행 단계뿐임
    - 타깃 오브젝트가 구현한 인터페이스의 모든 메소드는 조인 포인트가 됨
4. 포인트컷
    - 어드바이스를 적용할 조인 포인트를 선별하는 작업 또는 그 기능을 정의한 모듈
    - 스프링의 AOP의 조인 포인트는 메소드의 실행이므로 스프링의 포인트컷은 메소드를 선정하는 기능을 가짐
    - 때문에 메소드의 실행이라는 의미인 execution으로 시작하고, 메소드의 시그니처를 비교하는 방법으로 주로 이용함
    - 메소드는 클래스 안에 존재하는 것이기 때문에 메소드 선정이란 결국 클래스를 선정하고 그 안의 메소드를 선정하는 과정을 거치게 됨
5. 프록시
    - 클라이언트와 타깃 사이에 투명하게 존재하면서 부가기능을 제공하는 오브젝트
    - DI를 통해 타깃 대신 클라이언트에게 주입됨
    - 클라이어트의 메소드 호출을 대신 받아서 타깃에 위임 및 과정에서 부가기능 부여
    - 스프링은 프록시를 이용해 AOP 지원
6. 어드바이저
    - 포인트컷과 어드바이스를 하나씩 갖고 있는 오브젝트
    - 스프링은 **자동 프록시 생성기**가 어드바이저를 AOP 작업의 정보로 활용함
    - 어드바이저는 스프링 AOP에서만 사용되는 특별한 용어임
    - 일반적인 AOP에서는 사용되지 않음
7. 애스펙트
    - OOP의 클래스와 마찬가지로, 애스펙트는 AOP의 기본 모듈임
    - 한 개 이상의 포인트 컷과 어드바이스의 조합으로 만들어지며 보통 **싱글톤** 형태의 오브젝트로 존재함
    - 스프링의 어드바이저는 아주 단순한 애스펙트라 볼 수 있음
    

6.4.21 스프링 프록시 방식 AOP를 위한 네 가지 빈

- 어드바이스를 제외한 **세 가지 빈** 모두 스프링이 직접 제공하는 크랠스를 빈으로 등록하고 프로퍼티만 설정
- 네 가지 빈
    1. 자동 프록시 생성기
        - 스프링의 `DefaultAdvisorAutoProxyCreator` 클래스 빈으로 등록
        - 다른 빈을 DI 하지도 않고 자신도 DI 되지 않아 **독립적**으로 존재
        - 따라서 id도 굳이 필요 없음
        - 애플리케이션 컨텍스트가 빈 오브젝트를 생성하는 과정에서 **빈 후처리기**로 참여
        - 빈으로 등록된 어드바이저를 이용해서 프록시를 자동으로 생성
    2. 어드바이스
        - 부가기능을 구현한 클래스를 빈으로 등록
        - `TransactionAdvice`는 AOP 관련 빈 중에서 **유일**하게 **직접 구현한 클래스를 사용**
    3. 포인트컷
        - `AspectJExpressionPointcut`을 빈으로 등록하고 expression 프로퍼티에 포인트컷 표현식을 넣음
        - 때문에, 코드 작성 불필요
    4. 어드바이저
        - `DefaultPointcutAdvisor` 클래스를 빈으로 등록해서 사용
        - 어드바이스와 포인트컷 프로퍼티로 참조하는 것 외에는 기능이 없음
        - 자동 프록시 생성기에 의해 **자동 검색**되어 사용됨

6.4.22 AOP 네임스페이스

- 스프링은 AOP와 관련된 태그를 정의해둔 **aop 스키마**를 제공함
- aop 스프키마에 정의된 태그는 **별도의 네임스페이스**를 지정해서 디폴트 네임스페이스의 <bean> 태그와 구분해서 사용할 수 있음
- aop 스키마에 정의된 캐그를 사용하려면 성정파일에 aop 네임 스페이스 선언을 추가해야 함
- <aop:config>, <aop:pointcut>, <aop:advisor> 세 가지 태그를 정의해두면 그에따른 **세 개의 빈**이 자동으로 등록됨
    
    <aside>
    💡 어드바이스는 직접 등록해야 함!
    
    </aside>
    

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                            http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
                            http://www.springframework.org/schema/aop
                            http://www.springframework.org/schema/aop/spring-aop-3.0.xsd">

    <bean id="transactionAdvice" class="com.ksb.spring.TransactionAdvice">
        <property name="transactionManager" ref="transactionManager"/>
    </bean>

    <aop:config>
        <aop:pointcut id="transactionPointcut" expression="execution(* *..*ServiceImpl.upgrade*(..))"/>
        <aop:advisor advice-ref="transactionAdvice" pointcut-ref="transactionPointcut"/>
    </aop:config>
		...
</beans>
```

- 포인트컷을 내장한 어드바이저 태그로 만들 수 있음
- 포인트컷을 내장하는 경우 <aop:advisor> 태그 하나로 **두 개의 빈**이 등록 됨
- 즉, 애트리뷰트 설정에 따라 등록되는 빈의 개수와 종류가 달라질 수 있음

```xml
<aop:config>
    <aop:advisor advice-ref="transactionAdvice" pointcut="execution(* *..*ServiceImpl.upgrade*(..))"/>
</aop:config>
```