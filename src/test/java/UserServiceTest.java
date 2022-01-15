import com.ksb.spring.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import static com.ksb.spring.UserServiceImpl.MIN_LOG_COUNT_FOR_SILVER;
import static com.ksb.spring.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserServiceTest {
    @Autowired
    UserService userService;

    @Autowired
    UserDao userDao;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    MailSender mailSender;

    @Autowired
    UserServiceImpl userServiceImpl;

    List<User> users;

    @Before
    public void setUp() {
        users = Arrays.asList(
                new User("k1", "k1", "k1", Level.BASIC,
                        MIN_LOG_COUNT_FOR_SILVER - 1, 0),
                new User("k2", "k2", "k2", Level.BASIC,
                        MIN_LOG_COUNT_FOR_SILVER, 0),
                new User("k3", "k3", "k3", Level.SILVER,
                        60, MIN_RECOMMEND_FOR_GOLD - 1),
                new User("k4", "k4", "k4", Level.SILVER,
                        60, MIN_RECOMMEND_FOR_GOLD),
                new User("k5", "k5", "k5", Level.GOLD,
                        100, Integer.MAX_VALUE)
        );
    }

    @Test
    public void bean() {
        assertThat(this.userService, is(notNullValue()));
    }

    @Test
    //@DirtiesContext //DI 설정을 변경한다고 알림
    public void upgradeLevels() {
        userDao.deleteAll();
        for (User user : users) userDao.add(user);

//        MockMailSender mockMailSender = new MockMailSender();
//        userServiceImpl.setMailSender(mockMailSender);

        userService.upgradeLevels();

        checkLevelUpgraded(users.get(0), false);
        checkLevelUpgraded(users.get(1), true);
        checkLevelUpgraded(users.get(2), false);
        checkLevelUpgraded(users.get(3), true);
        checkLevelUpgraded(users.get(4), false);

        //List<String> request = mockMailSender.getRequests();
        //assertThat(request.size(), is(2)); //2,4이 업그레이드 이므로 총 2번
        //assertThat(request.get(0), is(users.get(1).getEmail()));
        //assertThat(request.get(0), is(users.get(1).getEmail()));
    }

    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
        } else {
            assertThat(userUpdate.getLevel(), is(user.getLevel()));
        }
    }

    @Test
    public void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4); //gold 유저
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
        assertThat(userWithoutLevelRead.getLevel(), is(userWithoutLevel.getLevel()));
    }

    @Test
    public void upgradeAllOrNothing() {
        UserServiceImpl.TestUserService testUserService =
                new UserServiceImpl.TestUserService(users.get(3).getId());
        testUserService.setUserDao(this.userDao);
        testUserService.setMailSender(this.mailSender);

        UserServiceTx txUserService = new UserServiceTx();
        txUserService.setTransactionManager(transactionManager);
        txUserService.setUserService(testUserService);

        userDao.deleteAll();
        for (User user : users) userDao.add(user);

        try {
            testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        } catch (UserServiceImpl.TestUserServiceException e) {
        }

        checkLevelUpgraded(users.get(1), false);
    }
}
