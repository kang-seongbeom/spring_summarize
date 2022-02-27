package com.ksb.spring.vol1;

import org.springframework.beans.factory.FactoryBean;

public class MessageFactoryBean implements FactoryBean<Message> {
    String text;

    public void setText(String text){
        this.text = text;
    }

    @Override
    public Message getObject() throws Exception {
        return Message.newMessage(this.text);
    }

    @Override
    public Class<?> getObjectType() {
        return Message.class;
    }

    //이 팩토리 빈은 매번 요청할 때마다 새로운 오브젝트 생성하므로 false
    //이것은 팩토리 빈의 설정이고, 만들어진 빈 오브젝트는 싱글톤으로 스프링이 관리 할 수 있음
    @Override
    public boolean isSingleton() {
        return false;
    }
}
