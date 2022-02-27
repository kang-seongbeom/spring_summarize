package com.ksb.spring.vol1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Driver;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "com.ksb.spring")
//@Import(SqlServiceContext.class)
@EnableSqlService
@PropertySource("classpath:/vol1/database.properties")
public class AppContext implements SqlMapConfig{

    @Autowired
    SqlService sqlService;

    @Autowired
    UserDao userDao;

    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        //현재 Enviroment의 값이 null이라 값을 주입할 수 없음
        try {
            dataSource.setDriverClass((Class<? extends Driver>) Class.forName("com.mysql.cj.jdbc.Driver"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        dataSource.setUrl("jdbc:mysql://localhost/toby?serverTimezone=UTC");
        dataSource.setUsername("root");
        dataSource.setPassword("1234");

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager tm = new DataSourceTransactionManager();
        tm.setDataSource(dataSource());
        return tm;
    }

    @Override
    public Resource getSqlMapResource() {
        return new ClassPathResource("/vol1/sqlmap.xml", UserDao.class);
    }

    @Configuration
    @Profile("production")
    public static class ProductionAppContext {
        @Bean
        public MailSender mailSender(){
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("localhost");
            return mailSender;
        }
    }

    @Configuration
    @Profile("test")
    public static class TestAppContext {

        @Bean
        public UserService testUserService() {
            return new UserServiceImpl.TestUserService();
        }

        @Bean
        public MailSender mailSender(){
            return new DummyMailSender();
        }
    }
}
