package vol1;

import com.ksb.spring.vol1.pointcutexpression.Bean;
import com.ksb.spring.vol1.pointcutexpression.Target;
import org.junit.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PointCutTest {
    @Test
    public void methodSignaturePointcut() throws NoSuchMethodException {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(public int " +
                "com.ksb.spring.vol1.pointcutexpression.Target.minus(int,int) " +
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

    @Test
    public void pointcut() throws Exception{
        targetClassPointcutMatches("execution(* *(..))",
                true, true, true, true, true, true);
        //나머지 생략
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
