package com.ksb.spring;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface UserService {
    void add(User user);
    void deleteAll();
    void update(User user1);
    void upgradeLevels();

    @Transactional(readOnly = true)
    User get(String id);

    @Transactional(readOnly = true)
    List<User> getAll();
}
