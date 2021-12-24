package com.ksb.spring;

import java.sql.SQLException;

public class DaoFactory {
    public UserDao userDao() throws SQLException, ClassNotFoundException {
        return new UserDao(connectionMaker());
    }

    public UserDao accountDao() throws SQLException, ClassNotFoundException {
        return new UserDao(connectionMaker());
    }

    public UserDao messageDao() throws SQLException, ClassNotFoundException {
        return new UserDao(connectionMaker());
    }

    private DConnectionMaker connectionMaker() {
        return new DConnectionMaker();
    }
}
