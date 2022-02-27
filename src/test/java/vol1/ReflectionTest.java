package vol1;

import com.ksb.spring.vol1.proxy.Hello;
import com.ksb.spring.vol1.proxy.HelloTarget;
import com.ksb.spring.vol1.proxy.UppercaseHandler;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class ReflectionTest {
    @Test
    public void invokeMethod() throws Exception {
        String name = "Spring";

        //length()
        assertThat(name.length(), is(6));

        Method lengthMethod = String.class.getMethod("length");
        assertThat((Integer)lengthMethod.invoke(name), is(6));

        //charAt()
        assertThat(name.charAt(0), is('S'));

        Method charAtMethod = String.class.getMethod("charAt", int.class);
        assertThat((Character)charAtMethod.invoke(name, 0), is('S'));
    }

    @Test
    public void simpleProxy(){
        Hello hello = new HelloTarget();
        assertThat(hello.sayHello("Toby"), is("Hello Toby"));
        assertThat(hello.sayHi("Toby"), is("Hi Toby"));
        assertThat(hello.sayThankYou("Toby"), is("Thank You Toby"));

        //프록시를 통한 타깃 접근
        Hello proxiedHello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] {Hello.class},
                new UppercaseHandler(new HelloTarget())
        );
        //
        assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));
        assertThat(proxiedHello.sayHi("Toby"), is("HI TOBY"));
        assertThat(proxiedHello.sayThankYou("Toby"), is("THANK YOU TOBY"));

    }
}
