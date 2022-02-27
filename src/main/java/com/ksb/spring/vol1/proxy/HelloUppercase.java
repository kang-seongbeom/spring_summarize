package com.ksb.spring.vol1.proxy;

public class HelloUppercase implements Hello {
    Hello hello;

    public HelloUppercase(Hello hello) {
        this.hello = hello;
    }

    //toUpperCase()가 모든 메소드에서 중복. 즉, 부가기능이 중복됨
    @Override
    public String sayHello(String name) {
        return this.hello.sayHello(name).toUpperCase();
    }

    @Override
    public String sayHi(String name) {
        return this.hello.sayHi(name).toUpperCase();
    }

    @Override
    public String sayThankYou(String name) {
        return this.hello.sayThankYou(name).toUpperCase();
    }
}
