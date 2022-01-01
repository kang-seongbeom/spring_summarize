# 3.1 다시 보는 초난감 DAO

3.1.1 개방 폐쇄 원칙과 템플릿

- 개방 폐쇄 원칙은 확장은 열리고 변경에는 닫여야 한다는 뜻임
- 즉, 코드 수정을 통해 그 기능이 다양해지고 **확장하려는 성질**이 있고, 어떤 부분은 고정되어 있고 변하지 않으려 **고정되는 성질**이 있다는 뜻임
- 템플릿이란, **일정 패턴으로 유지**되는 부분을 **확장되는 부분**으로부터 **독립**시켜 효과적으로 활용할 수 있도록 하는 방법임

3.1.2 예외처리 기능을 갖춘 DAO

- JDBC는 예외처리를 반드시 해야하는 원칙이 있음
- 예외 발생시에도 리소스 반환을 꼭 해야함
- Connection과 PrepareStatement는 보통 **풀(pool)** 방식으로 운영됨
- 풀 방식은 미리 만든 리소스를 돌려가며 필요할 때 할당을 하고, 반환하면 다시 풀에 넣는 방법임
- 이 제한된 리소스를 고갈되지 않게 해야함
- UserDao는 JDBC를 사용하므로 예외 발생시에도 리소스를 반환하게 예외 처리를 해야함
- close() 중에도 SQLException이 발생할 수 있음

```java
public class UserDao {
		...
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void add(User user) throws SQLException {
        PreparedStatement ps = null;
        Connection c = null;

        try {
            c = dataSource.getConnection();
            ps = c.prepareStatement(
                    "insert into users(id, name, password) value (?,?,?)"
            );
            ps.setString(1, user.getId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getPassword());
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

    public void deleteAll() throws SQLException {
        PreparedStatement ps = null;
        Connection c = null;
        try {
            c = dataSource.getConnection();
            ps = c.prepareStatement("delete from users");
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
```