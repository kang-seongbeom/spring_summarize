import com.ksb.spring.Calculator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class CalcSumTest {
    Calculator calculator;
    String numFilePath;

    @Before
    public void setUp(){
        calculator = new Calculator();
        numFilePath = getClass().getResource("numbers.txt").getPath();
    }

    @Test
    public void multiplyOfNumbers() throws IOException {
        assertThat(this.calculator.calcMultiply(this.numFilePath),is(24));
    }

    @Test
    public void sumOfNumber() throws IOException {
        assertThat(this.calculator.calcSum(this.numFilePath), is(10));
    }

    @Test
    public void concatenateStrings() throws IOException {
        assertThat(calculator.concatenate(this.numFilePath), is("1234"));
    }
}
