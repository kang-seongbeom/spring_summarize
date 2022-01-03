import com.ksb.spring.*;
import org.junit.Before;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UserDaoTest {

    private final User user1= new User("k1", "k1", "k1");
    private final User user2 = new User("k2", "k2", "k2");
    private final User user3 = new User("k3", "k3", "k3");

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
    }

    @Test
    public void addAndGet() throws SQLException {
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        assertThat(dao.getCount(), is(1));

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
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        assertThat(dao.getCount(), is(1));

        st = new AddStatement(user2);
        dao.jdbcContextWithStatementStrategy(st);
        assertThat(dao.getCount(), is(2));

        st = new AddStatement(user3);
        dao.jdbcContextWithStatementStrategy(st);
        assertThat(dao.getCount(), is(3));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void getUserFailure() throws SQLException {
        st = new DeleteAllStatement();
        dao.jdbcContextWithStatementStrategy(st);
        assertThat(dao.getCount(), is(0));

        dao.get("unknown_id");
    }
}
