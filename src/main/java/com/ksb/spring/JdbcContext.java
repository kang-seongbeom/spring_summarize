package com.ksb.spring;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcContext {
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeSql(final String query, final String... str) throws SQLException {
        workWithStatementStrategy(new StatementStrategy() {
            public PreparedStatement makePrepareStatement(Connection c) throws SQLException {
                PreparedStatement ps = c.prepareStatement(query);
                for (int i = 0; i < str.length; i++)
                    ps.setString(i + 1, str[i]);
                return ps;
            }
        });
    }

    public void workWithStatementStrategy(StatementStrategy stmt) throws SQLException {
        PreparedStatement ps = null;
        Connection c = null;
        try {
            c = this.dataSource.getConnection();
            ps = stmt.makePrepareStatement(c);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}