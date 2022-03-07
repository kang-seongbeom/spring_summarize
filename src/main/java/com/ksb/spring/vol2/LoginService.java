package com.ksb.spring.vol2;

import org.springframework.beans.factory.annotation.Autowired;

public class LoginService {
    //DL 방식으로 접근할 수 있도록 Provider를 사용해 DI 받음
    @Autowired LoginUser loginUser;

    public void login(Login login){
        this.loginUser.setLoginId(...);
        ...
    }
}
