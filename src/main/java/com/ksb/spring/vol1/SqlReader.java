package com.ksb.spring.vol1;

public interface SqlReader {
    //예외 발생 시 대부분 복구가 불가능 하므로 굳이 예외 선언X
    void read(SqlRegistry sqlRegistry);
}
