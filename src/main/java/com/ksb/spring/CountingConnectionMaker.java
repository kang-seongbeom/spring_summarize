package com.ksb.spring;

import java.sql.Connection;
import java.sql.SQLException;

public class CountingConnectionMaker implements ConnectionMaker {
    int cnt = 0;
    private ConnectionMaker realConnectionMaker;

    public CountingConnectionMaker(ConnectionMaker connectionMaker) {
        realConnectionMaker = connectionMaker;
    }

    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        this.cnt++;
        return realConnectionMaker.getConnection();
    }

    public int getCnt() {
        return cnt;
    }
}
