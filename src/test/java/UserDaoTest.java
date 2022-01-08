import com.ksb.spring.*;
import org.junit.Before;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UserDaoTest {

    private final User user1= new User("gyumee", "k1n", "k1p");
    private final User user2 = new User("leegw700", "k2n", "k2p");
    private final User user3 = new User("bumjin", "k3n", "k3p");

    UserDaoJdbc dao;

    @Before
    public void setUp(){
        dao = new UserDaoJdbc();
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
    public void count() throws SQLException {
        dao.deleteAll();
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
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.get("unknown_id");
    }

    @Test
    public void delete(){
        dao.deleteAll();
    }

    @Test
    public void getAll() {
        dao.deleteAll();
        List<User> users0 = dao.getAll();
        assertThat(users0.size() ,is(0));

        dao.add(user1);
        List<User> listUsers1 = dao.getAll();
        assertThat(listUsers1.size(), is(1));
        checkSameUser(user1, listUsers1.get(0));

        dao.add(user2);
        List<User> listUsers2 = dao.getAll();
        assertThat(listUsers2.size(), is(2));
        checkSameUser(user1, listUsers2.get(0));
        checkSameUser(user2, listUsers2.get(1));

        dao.add(user3);
        List<User> listUsers3 = dao.getAll();
        assertThat(listUsers3.size(), is(3));
        checkSameUser(user3, listUsers3.get(0));
        checkSameUser(user1, listUsers3.get(1));
        checkSameUser(user2, listUsers3.get(2));
    }

    private void checkSameUser(User pUser1, User pUser2) {
        assertThat(pUser1.getId(), is(pUser2.getId()));
        assertThat(pUser1.getName(), is(pUser2.getName()));
        assertThat(pUser1.getPassword(), is(pUser2.getPassword()));
    }

    @Test(expected = DataAccessException.class)
    public void duplicateKey(){
        dao.deleteAll();

        dao.add(user1);
        dao.add(user1);
    }
}
