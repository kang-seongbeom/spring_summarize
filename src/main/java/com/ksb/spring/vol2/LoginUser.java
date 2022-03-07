package com.ksb.spring.vol2;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.Date;

@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LoginUser {
    String loginId;
    String name;
    Date LoginTime;
}
