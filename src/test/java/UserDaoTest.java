import com.ksb.spring.*;
import org.junit.Before;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserDaoTest {

    private final User user1 = new User("gyumee", "k1n", "k1p",
            Level.BASIC, 1, 0);
    private final User user2 = new User("leegw700", "k2n", "k2p",
            Level.SILVER, 55, 10);
    private final User user3 = new User("bumjin", "k3n", "k3p",
            Level.GOLD, 100, 40);

    @Autowired
    UserDao dao;

    @Test
    public void addAndGet() {
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        assertThat(dao.getCount(), is(1));

        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        User userGet1 = dao.get(user1.getId());
        checkSameUser(userGet1, user1);

        User userGet2 = dao.get(user2.getId());
        checkSameUser(userGet2, user2);
    }

    @Test
    public void count() {
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
    public void delete() {
        dao.deleteAll();
    }

    @Test
    public void getAll() {
        dao.deleteAll();
        List<User> users0 = dao.getAll();
        assertThat(users0.size(), is(0));

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
        assertThat(pUser1.getLevel(), is(pUser2.getLevel()));
        assertThat(pUser1.getLogin(), is(pUser2.getLogin()));
        assertThat(pUser1.getRecommend(), is(pUser2.getRecommend()));
    }

    @Test(expected = DataAccessException.class)
    public void duplicateKey() {
        dao.deleteAll();

        dao.add(user1);
        dao.add(user1);
    }

    @Test
    public void update(){
        dao.deleteAll();
        dao.add(user1);
        dao.add(user2);

        user1.setName("ksb");
        user1.setPassword("ksb-p");
        user1.setLevel(Level.GOLD);
        user1.setLogin(1000);
        user1.setRecommend(999);
        dao.update(user1);

        User user1Update = dao.get(user1.getId());
        checkSameUser(user1, user1Update);

        User user2same = dao.get(user2.getId());
        checkSameUser(user2, user2same);
    }
}
