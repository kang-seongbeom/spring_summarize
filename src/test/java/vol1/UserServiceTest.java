package vol1;

import com.ksb.spring.vol1.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import static com.ksb.spring.vol1.UserServiceImpl.MIN_LOG_COUNT_FOR_SILVER;
import static com.ksb.spring.vol1.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;
;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = AppContext.class)
public class UserServiceTest {

    @Autowired
    UserDao userDao;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    MailSender mailSender;

    @Autowired
    UserService userService;

    @Autowired
    UserService testUserService;

    @Autowired
    ApplicationContext context;

    @Autowired
    DefaultListableBeanFactory bf;

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
    public void upgradeLevels() {
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        UserServiceImpl.MockUserDao mockUserDao =
                new UserServiceImpl.MockUserDao(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MockMailSender mockMailSender = new MockMailSender();
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        List<User> updated = mockUserDao.getUpdated();
        assertThat(updated.size(), is(2));
        checkUserAndLevel(updated.get(0), "k2", Level.SILVER);
        checkUserAndLevel(updated.get(1), "k4", Level.GOLD);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size(), is(2));
//        assertThat(request.get(0), is(users.get(1).getEmail()));
//        assertThat(request.get(0), is(users.get(3).getEmail()));
    }

    @Test
    public void mockUpgradeLevels() {
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        //다이내믹한 목 오브젝트 생서와 메소드의 리턴 값 설정
        //그리고 DI까지 세줄이면 충분함
        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        //리턴 값이 없는 메소드를 가진 목 오브젝트는 더욱 간단함
        MailSender mockMailSender = mock(MailSender.class);
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        //목 오브젝트가 제공하는 검증 기능을 통해서 어떤 메소드가 몇 번 호출 됐는지,
        //파라미터는 무엇인지 확인
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        assertThat(users.get(1).getLevel(), is(Level.SILVER));
        verify(mockUserDao).update(users.get(3));
        assertThat(users.get(3).getLevel(), is(Level.GOLD));

        //ArgumentCaptor는 파라미터 내부 값 확인할 때 사용
        ArgumentCaptor<SimpleMailMessage> mailMessageArg =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockMailSender, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
//        assertThat(mailMessages.get(0), is(users.get(1).getEmail()));
//        assertThat(mailMessages.get(0), is(users.get(1).getEmail()));
    }

    private void checkUserAndLevel(User updated, String expectedId,
                                   Level expectedLevel) {
        assertThat(updated.getId(), is(expectedId));
        assertThat(updated.getLevel(), is(expectedLevel));
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
        userDao.deleteAll();
        for (User user : users) userDao.add(user);

        try {
            this.testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        } catch (UserServiceImpl.TestUserServiceException e) {
        }

        checkLevelUpgraded(users.get(0), false);
    }

    @Test(expected = TransientDataAccessResourceException.class)
    public void readOnlyTransactionAttribute() {
        testUserService.getAll();
    }

    @Test
    public void transactionSync() {
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
        try {
            userService.deleteAll();
            userService.add(users.get(0));
            userService.add(users.get(1));
        } finally {
            transactionManager.rollback(txStatus);
        }
    }

    @Test
    public void beans() {
        for (String n : bf.getBeanDefinitionNames())
            System.out.println("컨테이너 내부 빈 : " + n +
                    "\t" + bf.getBean(n).getClass().getName());
    }
}
