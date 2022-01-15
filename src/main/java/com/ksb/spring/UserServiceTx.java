package com.ksb.spring;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserServiceTx implements UserService {

    UserService userService;
    PlatformTransactionManager transactionManager;

    public void setTransactionManager(
            PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void add(User user) {
        //위임
        this.userService.add(user);
    }

    @Override
    public void upgradeLevels() {
        TransactionStatus status =
                this.transactionManager.getTransaction(
                        new DefaultTransactionDefinition());
        try {

            //위임
            this.userService.upgradeLevels();

            this.transactionManager.commit(status); //커밋
        } catch (Exception e) {
            this.transactionManager.rollback(status); //롤백
            throw e;
        }
    }
}
