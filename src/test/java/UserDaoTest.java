import com.ksb.spring.DeleteAllStatement;
import com.ksb.spring.StatementStrategy;
import com.ksb.spring.User;
import com.ksb.spring.UserDao;
import org.junit.Before;

import java.sql.SQLException;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UserDaoTest {

    private User user1;
    private User user2;
    private User user3;

    UserDao dao;

    StatementStrategy st;

    @Before
    public void setUp(){
        dao = new UserDao();
        DataSource dataSource = new SingleConnectionDataSource(
                "jdbc:mysql://localhost/toby?serverTimezone=UTC",
                "root",
                "1234",
                true
        );
        dao.setDataSource(dataSource);

        this.user1 = new User("k1", "k1", "k1");
        this.user2 = new User("k2", "k2", "k2");
        this.user3 = new User("k3", "k3", "k3");

        st = new DeleteAllStatement();
    }

    @Test
    public void addAndGet() throws SQLException, ClassNotFoundException {
        dao.jdbcContextWithStatementStrategy(st);
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        User userGet1 = dao.get(user1.getId());
        assertThat(userGet1.getName(), is(user1.getName()));
        assertThat(userGet1.getPassword(), is(user1.getPassword()));

        User userGet2 = dao.get(user2.getId());
        assertThat(userGet2.getName(), is(user2.getName()));
        assertThat(userGet2.getPassword(), is(user2.getPassword()));
    }

    @Test
    public void count() throws SQLException, ClassNotFoundException {
        dao.jdbcContextWithStatementStrategy(st);
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        assertThat(dao.getCount(), is(1));

        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        dao.add(user3);
        assertThat(dao.getCount(), is(3));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void getUserFailure() throws SQLException {
        dao.jdbcContextWithStatementStrategy(st);
        assertThat(dao.getCount(), is(0));

        dao.get("unknown_id");
    }
}
