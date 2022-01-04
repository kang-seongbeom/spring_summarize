package com.ksb.spring;

import org.springframework.dao.EmptyResultDataAccessException;

import javax.sql.DataSource;
import java.sql.*;

public class UserDao {
//    private ConnectionMaker connectionMaker;

//    public UserDao(ConnectionMaker connectionMaker){
//        this.connectionMaker = connectionMaker;
//    }

//    public void setConnectionMaker(ConnectionMaker connectionMaker){
//        this.connectionMaker = connectionMaker;
//    }

    private JdbcContext jdbcContext;
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.jdbcContext = new JdbcContext();
        this.jdbcContext.setDataSource(dataSource);

        //아직 JdbcContext를 적용하지 않은 메소드를 위해 저장해둠
        this.dataSource = dataSource;
    }

//    public void setJdbcContext(JdbcContext jdbcContext){
//        this.jdbcContext = jdbcContext;
//    }

    public void add(final User user) throws SQLException {
        String id = user.getId();
        String name = user.getName();
        String password = user.getPassword();
        String query = "insert into users(id, name, password) value (?,?,?)";
        this.jdbcContext.executeSql(query, id, name, password);
    }

    public void deleteAll() throws SQLException{
        String query = "delete from users";
        this.jdbcContext.executeSql(query);
    }

    public User get(String id) throws SQLException {
        PreparedStatement ps = null;
        Connection c = null;
        ResultSet rs = null;
        User user = null;
        try {
            c = dataSource.getConnection();
            ps = c.prepareStatement(
                    "select * from users where id = ?"
            );
            ps.setString(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                user = new User();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setPassword(rs.getString("password"));
            }
            if (user == null) throw new EmptyResultDataAccessException(1);
            return user;
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
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getCount() throws SQLException {
        PreparedStatement ps = null;
        Connection c = null;
        ResultSet rs = null;

        try {
            c = dataSource.getConnection();
            ps = c.prepareStatement("select count(*) from users");
            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
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
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
