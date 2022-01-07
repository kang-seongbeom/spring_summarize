# 4.1 사라진 SQLException

4.1.1 JdbcContext에서 JdbcTemplate로 적용

- JdbcContext를 JdbcTemplate로 바꾸니 `throws SQLException`이 사라짐

```java
//JdbcContext
public deleteAll() throws SQLException {
	this.jdbcContext.update("delete from users");
}

//JdbcTemplate
public deleteAll() {
	this.jdbcTemplate.update("delete from users");
}
```

4.1.2 초난감 예외처리

- try/catch로 예외를 잡고 아무것도 안하는 것은 예외 발생시 무시하고 **계속 진행**하기 때문에, 그냥 예외가 발생 했을 때 보다도 더 나쁜 코드임
    
    ```java
    try{
    	...
    }catch(SQLException){} //아무런 처리를 안함
    ```
    
- 예외 메시지만 출력 하는 것 역시 예외 처리를 한 것이 아님
    
    ```java
    try{
    	...
    }catch(SQLException){
    	e.printStackTrace();
    }
    ```
    
- 예외 발생시 적절히 복구 되던지, 작업을 중단하고 운영진에게 통보해야 함

4.1.3 무의미하고 무책임한 throws

- throws를 통해 호출한 메소드에 예외를 떠넘길 수 있음
- 상위 메소드에 throws 선언이 되어 있으면 해당 메소드는 어떤 실행 중에 예외가 발생 했는지, 습관적으로 예외를 던진것인지 알 수 없음(런타임 예외도 throws로 던질 수 있기 때문)

![https://blog.kakaocdn.net/dn/bfMWCK/btqSdtquh2C/kxlc0i8F9fdEsiuCl0oIGk/img.jpg](https://blog.kakaocdn.net/dn/bfMWCK/btqSdtquh2C/kxlc0i8F9fdEsiuCl0oIGk/img.jpg)

<aside>
💡 예외를 출력만 하거나, 단순히 상위로 던지는 것은 용납되어선 안됨

</aside>

4.1.4 예외의 종류와 특징

- 에러(Error)
- 예외(Exception)
    - 체크 예외(Checked Exception)
    - 언체크 예외(Unchecked Exception), **런타임 예외**(Runtime Exception)

[https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcEQd31%2FbtqSmBusuFr%2Fac873B3IY8bXDNJJ0gD9Y0%2Fimg.jpg](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcEQd31%2FbtqSmBusuFr%2Fac873B3IY8bXDNJJ0gD9Y0%2Fimg.jpg)

4.1.5 Error

- java.lang.Error의 서브 클래스들
- 시스템에 비정상적인 상황이 발생했을 때 발생
- 주로 JVM에서 발생되고, 애플리케이션 코드에서 잡으려고 하면 안됨
    
    <aside>
    💡 애플리케이션 코드에서 대응 방법이 없기 때문
    
    </aside>
    
- 애플리케이션에서 에러에 대한 처리는 신경쓰지 않아도 됨

4.1.6 체크 예외

- Exception 클래스의 서브 클래스 이면서, RuntimeException의 상속을 받지 않은 서브 클래스
- 반드시 예외 처리 코드를 강제함
- 예외 처리 코드가 없으면 컴파일 에러 발생

<aside>
💡 체크 예외는 예외 처리를 강제하기 때문에, 예외 블랙홀이나 무책임한 throws가 남발되어 체크 예외의 불필요성을 제기하고 있음

</aside>

4.1.7 런타임 예외

- 명시적으로 예외처리를 강제하지 않음
- 물론, 명시적으로 잡거나 throws로 선언해도 상관 없음
- 피할 수 있지만, 개발자의 부주의로 발생하는 예외

<aside>
💡 최근 등장하는 API 들의 예외는 대부분 런타임 예외를 던지도록 하고 있음

</aside>

4.1.8 예외 처리 방법

1. 예외 복구
2. 예외 회피
3. 예외 전환
    - 중첩 예외(Nested Exception)
    - 예외 포장(Exception Wrap)

4.1.9 예외 복구

- 예외 상황을 파악하고 문제를 해결해서 정상 상태로 돌려놓는 것
- 단순히 메시지를 보내는 것은 예외 복구라 할 수 없음
- 예외 발생시 사용자에게 특정 **행동을 유도**하는 것 역시 예외 복구임
- 예외가 처리됐으면 비록 기능적으로는 사용자에게 예외상황으로 비쳐도 애플리케이션은 정상적으로 설계된 흐름에 따라 진행돼야 함
- DB 서버에 접속하다 실패해서 SQLException 이 발생한 경우, 정해진 횟수만큼 재시도하여 예외 복구를 시도할 수 있음
- 체크 예외의 경우 예외를 어떤식으로든 복구할 가능성이 있을 때 사용되야 함

```java
int maxretry = MAX_RETRY;
while(maxretry --> 0){
	try{
		... //예외가 발생할 가능성이 있는 시도
		return; //작업 성공
	} catch(SomeException e){
		//로그 출력. 일정 시간 대기
	} finnaly{
		//리소스 반납. 정리 작업
	}
}
throw new RetryFailedException(); //최대 재시도 횟수를 넘기면 예외 발생
```

4.1.10 예외처리 회피

- `thows`로 예외 처리를 자신을 호출한 메소드로 던짐
- ResultSet이나 PreparedStatement 등을 이용해 작업하다 SQLException 발생 시, 자신이 처리하지 않고 **템플릿**에 던짐
- 하지만, 콜백과 템플릿과 같이 긴밀한 관계를 가지지 않는다면 무책임한 책임회피가 될 수 있음
- 회피 방법
    1. 바로 예외를 던지는 경우
        
        ```java
        public void add() SQLException{}
        ```
        
    2. try/catch로 잡은 후 에러 메시지를 출력하고 던지는 경우
        
        ```java
        public void add() throws SQLException{
        	try{
        		...
        	}catch(SQLException e){
        		e.printStackTrace();
        		throw e;
        	}
        }
        ```
        
    

4.1.11 예외 전환

- 예외 회피와 같이 예외를 밖으로 던지지만, 적절한 예외로 **전환**해서 던짐
    
    <aside>
    💡 예외 회피와 달리, 발생한 예외를 그대로 넘기지 않음
    
    </aside>
    
- 보통 **두 가지** 목적으로 사용
    - 발생한 예외를 그대로 던지는 것이 예외상황에 대한 적절한 의미를 부여하지 못할 경우
    - 의미를 분명하게 해줄 수 있는 예외로 변경하기 위해
- 서비스 계층에서 어떤 이유 때문에 발생한 예외인지 확인하는 절차를 넣는것은 어색하므로,  의미가 분명한 예외로 전환해서 전달하는것이 좋음
- SQLException은 DB 연결 실패, 쿼리의 실수, 중복된 아이디 존재 등 다양한 이유로 발생
- 추가하려는 아이디가 이미 존재할 때 발생하는 예외는 충분히 예상할 수 있으므로 의미가 분명하게 바꿔 전달할 수 있음

```java
public void add(User user) throws DuplicatedUserIdException, SQLException{
	try{
		...
	}catch(SQLException){
		if(e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY)
			throw DuplicatedUserIdException();
		else 
			throw SQLException();
	}
}
```

4.1.12 중첩 예외

- 예외 전환을 통해 예외를 던지면 처음에 발생한 예외가 어떤 예외인지 확인 못함
- 생성자 또는 initCause() 메소드를 통해 처음 발생한 예외를 담아서 던짐

```java
catch(SQLException e){
	...
	throw DuplicatedUserIdException(e);
	//throw DuplicatedUserIdException().initCause(e);
}
```

4.1.13 예외 포장

- 중첩 예외와 같이 새로운 예외를 만들고 원인이 되는 예외를 내부에 담아서 던짐
- 하지만, 의미를 명확하게 하기 위해서 하려는 의도가 아님
- 주로 **체크 예외**를 **런타임 예외**로 변환하는 경우에 사용
- 체크 예외는 애플리케이션 코드 상에서 **복구가 불가능** 하므로 무분별한 throws를 막기 위해 예외처리를 강제하지 않는 런타임 예외로 변경하기 위해 사용
- 대부분 서버환경에서는 애플리케이션 코드에서 처리하지 않고 전달된 예외들을 일괄적으로 다룰수 있는 기능 제공
- 어쩌피 복구못할 예외라면 애플리케이션 코드에서 포장해서 던지고, 예외 처리 서비스 등을 이용하여 로그 기록 및 운영진에게 알리는 것이 바람직 함

```java
try{
	OrderHome orderHome = EJBHomeFactory.getInstance().getOrderHome();
	Order order = orderhome.findByPrimaryKey(Integer id);
} catch (NamingException ne){
	throw new EJBException(ne); //체크 예외 -> 런타임 예외
}
```

4.1.14 예외처리 전략

1. 런타임 예외의 보편화
2. 애플리케이션 예외

<aside>
💡 예외가 발생한 코드를 깔끔히 정리하기 위해 일관된 예외처리 전략이 필요

</aside>

4.1.15 런타임 예외의 보편화

- 예외처리를 강제하는 체크 예외 대신 **예외 포장**을 사용한 런타임 예외를 사용하도록 함
- 자바 엔터프라이즈 서버는 독립형 애플리케이션과 달리 예외 발생시 작업을 일시 중지하고 사용자와 커뮤니케이션을 통해 복구할 수 있는 방법이 없음
- 차라리, 예외상황을 **미리 파악**하고 예외가 발생하지 않도록 하는 것이 좋음
    
    <aside>
    💡 런타임 예외는 예외 처리를 하지 않아도 되지만, 예외 발생 경우를 대비해 예외처리를 함
    
    </aside>
    
- 즉, 서버 환경에서 체크 예외의 활용도와 가치가 떨어지기 때문에 런타임 예외으로 변경하여 던지는 것이 좋음
- 아이디 중복으로 발생하는 SQLException을 제외한 예외발생 원인들은 대부분 복구 불가능 예외이기 때문에, 어처피 처리하지 못할 코드를 런타임 예외로 변경하는 것이 바람직함

```java
public class DuplicateUserIdException extends RuntimeException{
		//중첩 예외를 위한 생성자
    public DuplicateUserIdException(Throwable cause){
        super(cause);
    }
}

public class UserDao {
		...
		public void add() throws DuplicateUserIdException{
        try{
					...
        }catch (SQLException e){
            if(e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY)
                throw new DuplicateUserIdException(e); //예외 전환
            else
                throw new RuntimeException(e); //예외 포장
        }
    }
}
```

<aside>
💡 런타임 예외는 복구할 수 있는 예외는 없다고 가정하고 예외가 생겨도 시스템 레벨에서 알아서 처리해 줄 것이고, 필요한 경우 런타임 예외에서라도 처리할 수 있기 때문에 낙관적인 에외처리 기법임

</aside>

4.1.16 애플리케이션 예외

- **의도적**으로 예외를 발생하여 반드시 처리를 하도록 강제
- 두 가지의 설계 방법
    1. 리턴 값을 통한 예외상황 체크
        1. 특정 행동에 대한 결과의 반환 값을 확인하여 예외상황인지 확인
        2. 계좌 출금시 예치된 금액보다 많은 금액을 출금하여 할 때, -1 값을 반환하여 예외상황 확인
        3. 리턴 값을 개발자 마다 다르게 할 수 있기 때문에 혼동이 올 수 있음
        4. 조건문이 남발될 수 있음
    2. 비즈니스적인 의미를 띤 예외를 던져 체크
        1. 특정 의미를 지닌 이름의 예외를 만들어 예외상황 확인
        2. 반드시 처리할 수 있도록 **체크 예외**로 생성
        3. 예외상황에 대한 상세한 정보를 담고 있도록 설계할 필요가 있음
    
    ```java
    try{
    	...
    }catch(InsufficientBalanceException e){ //잔고 부족 의미를 지닌 체크 예외
    	//예외 처리
    }
    ```
    

4.1.17 SQLException은 어떻게 됐나?

- JdbcContext를 JdbcTemplate로 변경하니 SQLException이 사라졌음
- 코드상에서 SQLException은 복구 가능한 예외가 아님
- 때문에 JdbcTemplate는 **체크 예외**인 SQLException을 **런타임 예외**인 `DataAccessException`으로 변경하여 예외를 던짐
- 때문에 JdbcTemplate의 update(), query() 등의 메소드를 사용할 때 SQLException 처리를 하지 않아도 됐던것임