# 1.8 XML을 이용한 설정

1.8.1 XML 사용 이유

- 오브젝트 사이의 의존정보를 일일이 자바 코드로 만들기 번거로움
- 자바코드는 틀에 박힌 구조
- XML은 자바 코드와 달리 별도의 빌드 작업이 없고, 빠르게 변경사한 반영 가능
- 스키마나 DTD(Document Type Definition)을 사용해 정해진 포맷으로 작성 되었는지 확인 가능

1.8.2 XML 설정

- DI 정보가 담긴 XML 파일은 <beans>를 루트 엘리먼트로 사용
- <Bean> 태그로 빈 설정
- XML 설정은 자바의 @Configraion, @Bean 설정과 동일
    
    <aside>
    💡 @Configraion은 <Beans>,  @Bean은 <Bean>과 같다고 생각하면 됨
    
    </aside>
    
- @Bean 메소드를 통해서 얻을 수 있는 세 가지 정보
    1. 빈의 이름(id)
        - @Bean 메소드 이름이 빈의 이름. 이 이름은 getBea() 에서 사용됨
    2. 빈의 클래스(class)
        - 빈 오브젝트를 어떤 크래스를 이용해 만들지 정의
    3. 빈의 의존 오브젝트
        - 생성자나 수정자 메소드(setter)를 통해 **의존 오프젝트**를 넣어줌(ref). 의존 오브젝트로 하나의 빈이므로 이름이 있을 것이고, 그 이름에 해당하는 메소드를 호출해서 의존 오브젝트를 가져온다. 의존 오브젝트는 다수일 수 있음
            
            <aside>
            💡 DI를 원하는 오브젝트는 먼저 자기 자신이 컨테이너가 관리하는 빈이어야 함
            
            </aside>
            
- ConnectionMaker 과 같은 의존 오브젝트가 없는경우 ‘빈의 의존 오브젝트’는 생략 가능
- XML은 자바 코드처럼 유연하게 정의 될 수 없음. 따라서 **태그와 애트리뷰트**의 내용을 잘 알고있어야 함

1.8.2 ConnectionMaker의 전환

- ConnectionMaker가 의존하는 오브젝트가 없어 이름(id) 과 클래스(class) 정보만 있으면 됨
- 이름은 메소드 이름임
- 클래스는 자바 메소드에서 반환 오브젝트를 만들 때 사용하는 클래스 이름(패키지 경로까지 모두 포함되어 있어야함)

|  | 자바 코드 | XML 설정 정보 |
| --- | --- | --- |
| 빈 설정파일 | @Configraion | <beans> |
| 빈의 이름 | @Bean methodName | <bean id=”methodName”> |
| 빈의 클래스 | return new BeanClass(); | class=”a.b.c... BeanClass” |

```java
@Bean
public DConnectionMaker connectionMaker() {
    return new DConnectionMaker();
}

//id = connectionMaker()
//class = com.ksb.spring.DConnectionMaker
<bean id="connectionMaker" class="com.ksb.spring.DConnectionMaker"></bean>
```

1.8.3 userDao() 전환

- userDao()에는 이름, 클래스, 의존 오브젝트 세 가지가 모두 있어야 함
- 수정 메소드를 사용하는 이유는 XML로 의존관계 정보를 만들 때 편리함
- 자바빈의 관례를 따라 수정자 메소드는 **프로퍼티**가 됨
- 예를 들어, setConnecionMaker()라는 **메소드**가 있으면 connectionMaker가 **프로퍼티**임
- XML에서는 <property> 태그를 사용해 의존 오브젝트와의 관계 정의
- <property>는 **name**과 **ref**라는 두 개의 애트리뷰트를 가짐
- name은 프로퍼티 이름. 프로퍼티 이름으로 수정자 메소드를 알 수 있음(프로퍼티에 set을 붙이면 수정자 메소드가 됨)
- ref는 수정자 메소드를 통해 주입해줄 오브젝트 빈 이름

```java
@Bean
public UserDao userDao() {
    UserDao userDao = new UserDao();
    userDao.setConnectionMaker(connectionMaker());
		return userDao
}

//id = userDao()
//class = com.ksb.spring.UserDaoJdbc
//property = set
//name = ConnectionMaker
/*
ref = connectionMaker()의 bean id
<bean id="connectionMaker" class="com.ksb.spring.DConnectionMaker"></bean>
*/
<bean id="userDao" class="com.ksb.spring.UserDaoJdbc">
	<property name="connectionMaker" ref="connectionMaker">
</bean>
```

1.8.4 프로퍼티 애트리뷰트

- name 애트리뷰트는 DI에 사용할 수정자 메소드의 프로퍼티 이름
- ref 애트리뷰트는 주입할 오브젝트를 정의한 빈 id
- 보톤 프로퍼티 이름(name)과 DI되는 빈의 이름(ref)이 같은 경우가 많음(둘 다 인터페이스의 이름을 따르는 경우가 많기 때문)
- 빈의 이름을 바꾸는 경우 그 이름을 참조한 다른 빈의 <property> ref 애트리뷰트의 값도 함께 변경해 줘야 함

```java
//예시
<beans>
	<bean id="myConnectionMaker" class="com.ksb.spring.DConnectionMaker"></bean>
	<bean id="userDao" class="com.ksb.spring.UserDaoJdbc">
		<property name="connectionMaker" ref="myConnectionMaker">
	</bean>
<beans/>
```

- 같은 인터페이스를 구현한 여러 의존 오브젝트 중 하나만 DI 받을 빈을 지정 하는 경우

```java
//예시
<beans>
	<bean id="my1ConnectionMaker" class="com.ksb.spring.DConnectionMaker"></bean>
	<bean id="my2ConnectionMaker" class="com.ksb.spring.NConnectionMaker"></bean>
	<bean id="my3ConnectionMaker" class="com.ksb.spring.GConnectionMaker"></bean>

	<bean id="userDao" class="com.ksb.spring.UserDaoJdbc">
		<property name="connectionMaker" ref="my1ConnectionMaker">
	</bean>
<beans/>
```

1.8.5 DTD와 스키마(schema)

- XML 문서를 미리 정해진 구조를 따라서 작업 되었는지 검사
- 스프링은 DTD와 스키마 둘 다 지원
- 특별한 이유가 없다면 스키마를 사용하는 것이 바람직함
- DTD를 사용하는 경우
    
    ```xml
    <!DOCTYPE beans PUBLIC"-//SPRING//DTD DBAN 2.0//EN"
           "http://www.springframework.org/dtd/spring-beans-2.0.dtd">
    ```
    
- 스키마를 사용하는 경우
    
    ```xml
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
                                http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">
    ```
    

1.8.7 XML을 이용하는 애플리케이션 컨텍스트

- 애플리케이션 컨텍스트가 DaoFactory 대신 **XML 설정정보**를 활용하게 함
- `GenericXmlApplicationContext`를 이용하여 XML 설정정보를 가져옴
    
    ```java
    ApplicationContext applicationContext =
                    new GenericXmlApplicationContext("applicationContext.xml");
    ```
    
- `ClassPathXmlApplicationContext`는 두 번째 파라미터의 위치로 하여금 **상대 위치**로 XML 설정 정보를 가져옴
    
    ```java
    ApplicationContext applicationContext =
                    new ClassPathXmlApplicationContext("applicationContext.xml", 
    																										UserDao.class);
    ```
    
- XML 설정파일의 이름은 관례로 **applicationContext.xml**로 만듦
    
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
                                http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">
    
        <bean id="connectionMaker" class="com.ksb.spring.DConnectionMaker"></bean>
        <bean id="userDao" class="com.ksb.spring.UserDaoJdbc">
            <property name="connectionMaker" ref="connectionMaker"></property>
        </bean>
    </beans>
    ```
    

1.8.8 DataSource 인터페이스로 변환

- ConnectionMaker는 DB 커넥셔을 생성해주는 단순 인터페이스임
- 자바에서 이와 비슷한 DataSource 인터페이스가 이미 존재함
- DataSource에는 getConnecion()이외에 구현 해야 하는 메소드들이 여러 존재하여 직접 구현하기에는 번거로움
- **DataSource 구현 클래스**는 이미 여러개 존재. 대부분의 DataSource 구현 클래스는 DB의 종류나 아이디, 비밀번호 정도는 DataSource 구현 클래스를 다시 만들지 않아도 지정할 수 있는 방법 제공하고 있음
- **SimpleDriverDataSource**를 사용할 것임
- 자바 코드
    
    ```java
    public class UserDao {
    
    	private DataSource dataSource;
    
    	public void setDataSource(DataSource dataSource){
    	  this.dataSource = dataSource;
    	}
    
    	public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = dataSource.getConnection();
    		...
    	}
    }
    
    @Configuration
    public class DaoFactory {
        @Bean
        public UserDao userDao() {
            UserDao userDao = new UserDao();
            userDao.setDataSource(dataSource());
            return userDao;
        }
    
        @Bean
        public DataSource dataSource(){
            SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
    
            dataSource.setDriverClass(com.mysql.cj.jdbc.Driver.class);
            dataSource.setUrl("jdbc:mysql://localhost/toby?serverTimezone=UTC");
            dataSource.setUsername("root");
            dataSource.setPassword("1234");
    
            return dataSource;
        }
    }
    ```
    

1.8.9 프로퍼티 값 주입

- DaoFactory의 dataSource는 수정자 메소드에 다른 빈, 오브젝트 뿐 만이 아니라 **String** 같은 단순한 값을 넣을 수 있음(**dataSource.setUrl**)
- **dataSource.setDriverClass()**는 **Class** 타입의 오브젝트이나, 다른 빈 오브젝트를 DI 방식으로 가져와서 넣는 것은 아님
- 이렇게 다른 빈 오브젝트의 레퍼런스가 아닌 단순 정보도 오브젝트를 초기화하는 과정에서 수정자 메소드에 넣을 수 있음
- 이때는 DI에서처럼 오브젝트의 구현 크래스를 동적으로 바꿀수 있게 하는 **목적이 아님**
- 단순히 변경 가능한 정보(DB 접속 아이디, 비밀번호와 같은)를 설정하도록 위한 것
- 텍스트나 단순 오브젝트 등을 수정자 메서드에 넣어주는 것 ‘값을 주입’하는 것 이므로 DI의 일종임
- <property ref=””>의 일종이나 단순 값을 주입하는 것이기 때문에 value 애트리뷰트 사용

```java
dataSource.setDriverClass(com.mysql.cj.jdbc.Driver.class);
dataSource.setUrl("jdbc:mysql://localhost/toby?serverTimezone=UTC");
dataSource.setUsername("root");
dataSource.setPassword("1234");

<property name="driverClass" value="com.mysql.cj.jdbc.Driver"/>
<property name="url" value="jdbc:mysql://localhost/toby?serverTimezone=UTC"/>
<property name="username" value="root"/>
<property name="password" value="1234"/>
```

1.8.10 value 값의 자동 변환

- DriverClass는 String이 아닌 클래스임
- String이 아닌 클래스임에도 value를 통해 넣을 수 있는 까닭은, value로 값 주입 시 수정자 메소드의 파라미터를 참고해서 적절한 형태로 **형 변환**하기 때문
- 값이 여러개라면 List, Map, 배열 등을 사용해 값 주입 가능
- XML
    
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
                                http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">
        <bean id="dataSource" class="org.springframework.jdbc.datasource.SimpleDriverDataSource">
            <property name="driverClass" value="com.mysql.cj.jdbc.Driver"/>
            <property name="url" value="jdbc:mysql://localhost/toby?serverTimezone=UTC"/>
            <property name="username" value="root"/>
            <property name="password" value="1234"/>
        </bean>
        <bean id="userDao" class="com.ksb.spring.UserDaoJdbc">
            <property name="dataSource" ref="dataSource"/>
        </bean>
    </beans>
    ```