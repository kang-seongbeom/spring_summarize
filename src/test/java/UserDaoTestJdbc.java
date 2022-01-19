import com.ksb.spring.User;
import com.ksb.spring.UserDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserDaoTestJdbc {

    private final User user1 = new User("k1", "k1", "k1");
    private final User user2 = new User("k2", "k2", "k2");
    private final User user3 = new User("k3", "k3", "k3");

    @Autowired
    UserDao dao;

    @Autowired
    DataSource dataSource;

    @Before
    public void setUp() {

    }

    @Test
    public void addAndGet() {
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
    public void getUserFailure() {
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.get("unknown_id");
    }

//    @Test
//    public void sqlExceptionTranslate() {
//        dao.deleteAll();
//
//        dao.add(user1);
//        dao.add(user1);
//        try {
//            dao.add(user1);
//            dao.add(user1);
//        } catch (DuplicateKeyException ex) {
//            SQLException sqlEx = (SQLException) ex.getRootCause();
//            SQLExceptionTranslator set =
//                    new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
//            assertThat(set.translate(null, null, sqlEx),
//                    is(DuplicateKeyException.class));
//        }
//    }
}
