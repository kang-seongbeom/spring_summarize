package com.ksb.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.sql.SQLException;

public class UserDaoTest {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        //UserDao dao = new DaoFactory().userDao();
        ApplicationContext applicationContext =
                new GenericXmlApplicationContext("applicationContext.xml");
        UserDao dao = applicationContext.getBean("userDao", UserDao.class);

//        CountingConnectionMaker ccm = applicationContext.getBean("connectionMaker",
//                CountingConnectionMaker.class);
//        System.out.println(ccm.getCnt());

        User user = new User();
        user.setId("ksb 1");
        user.setName("ksb");
        user.setPassword("ksb-pwd");

        dao.add(user);

        System.out.println(user.getId() + "등록");

        User user2 = dao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());

        System.out.println(user2.getId() + "조회");

    }
}