package com.ksb.spring;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public class UserDaoJdbc implements UserDao {

    private RowMapper<User> userMapper =
            new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));
                    user.setLevel(Level.valueOf(rs.getInt("level")));
                    user.setLogin(rs.getInt("login"));
                    user.setRecommend(rs.getInt("recommend"));
                    return user;
                }
            };

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void add(final User user) {
        String id = user.getId();
        String name = user.getName();
        String password = user.getPassword();
        int level = user.getLevel().intValue();
        int login = user.getLogin();
        int recommend = user.getRecommend();
        String query = "insert into users(id, name, password, " +
                "level, login, recommend) value (?,?,?,?,?,?)";
        this.jdbcTemplate.update(query, id, name, password,
                level, login, recommend);
    }

    public void deleteAll() {
        String query = "delete from users";
        this.jdbcTemplate.update(query);
    }

    public User get(String id) {
        String query = "select * from users where id = ?";
        return this.jdbcTemplate.queryForObject(query,
                new Object[]{id}, this.userMapper);
    }

    public List<User> getAll() {
        String query = "select * from users order by id";
        return this.jdbcTemplate.query(query, this.userMapper);
    }

    public int getCount() {
        String query = "select count(*) from users";
        return this.jdbcTemplate.queryForObject(query, Integer.class);
    }

    public void update(User user1) {
        String query = "update users set name=?, password=?, level=?, " +
                "login=?, recommend=? where id=?";
        String id = user1.getId();
        String name = user1.getName();
        String password = user1.getPassword();
        int level = user1.getLevel().intValue();
        int login = user1.getLogin();
        int recommend = user1.getRecommend();
        this.jdbcTemplate.update(query, name, password,
                level, login, recommend, id);
    }
}
