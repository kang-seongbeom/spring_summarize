package com.ksb.spring.vol1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;


//@Repository("userDao")
@Repository
public class UserDaoJdbc implements UserDao {

    @Autowired
    private SqlService sqlService;

    public void setSqlService(SqlService sqlService){
        this.sqlService = sqlService;
    }
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

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

    public void add(final User user) {
        String id = user.getId();
        String name = user.getName();
        String password = user.getPassword();
        int level = user.getLevel().intValue();
        int login = user.getLogin();
        int recommend = user.getRecommend();
        this.jdbcTemplate.update(sqlService.getSql("userAdd"), id, name, password,
                level, login, recommend);
    }

    public void deleteAll() {
        this.jdbcTemplate.update(sqlService.getSql("userDeleteAll"));
    }

    public User get(String id) {
        return this.jdbcTemplate.queryForObject(sqlService.getSql("userGet"),
                new Object[]{id}, this.userMapper);
    }

    public List<User> getAll() {
        return this.jdbcTemplate.query(sqlService.getSql("userGetAll"), this.userMapper);
    }

    public int getCount() {
        return this.jdbcTemplate.queryForObject(sqlService.getSql("userGetCount"), Integer.class);
    }

    public void update(User user1) {
        String id = user1.getId();
        String name = user1.getName();
        String password = user1.getPassword();
        int level = user1.getLevel().intValue();
        int login = user1.getLogin();
        int recommend = user1.getRecommend();
        this.jdbcTemplate.update(sqlService.getSql("userUpdate"), name, password,
                level, login, recommend, id);
    }
}
