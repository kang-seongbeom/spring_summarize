package vol2;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ScopeTest {
    @Test
    public void prototypeScope(){
        ApplicationContext ac = new AnnotationConfigApplicationContext(
                PrototypeBean.class, PrototypeClientBean.class
        );
        Set<PrototypeBean> bean = new HashSet<>();

        //DL 방식으로 빈을 요청할 때 마다 새로운 빈이 추가되는지 확인
        bean.add(ac.getBean(PrototypeBean.class));
        assertThat(bean.size(), is(1));
        bean.add(ac.getBean(PrototypeBean.class));
        assertThat(bean.size(), is(2));

        //DI 방식으로 빈을 요청할 때 마다 새로운 빈이 추가되는지 확인
        bean.add(ac.getBean(PrototypeClientBean.class).bean1);
        assertThat(bean.size(), is(3));
        bean.add(ac.getBean(PrototypeClientBean.class).bean2);
        assertThat(bean.size(), is(4));

    }

    @Scope("prototype")
    static class PrototypeBean{}

    static class PrototypeClientBean{
        @Autowired PrototypeBean bean1;
        @Autowired PrototypeBean bean2;
    }
}
