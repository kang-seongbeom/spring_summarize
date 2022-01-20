package com.ksb.spring;

public class Message {
    String text;

    private Message(String text){ //private
        this.text = text;
    }

    public String getText(){
        return text;
    }
    //생성자를 제공하는 스태틱 팩토리 메소드
    public static Message newMessage(String text){
        return new Message(text);
    }
}
