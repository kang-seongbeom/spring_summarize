package com.ksb.spring;

import java.util.List;

public interface UserService {
    void add(User user);
    User get(String id);
    List<User> getAll();
    void deleteAll();
    void update(User user1);
    void upgradeLevels();
}
