package com.ksb.spring.vol1;

import org.springframework.context.annotation.Import;

@Import(value = SqlServiceContext.class)
public @interface EnableSqlService {
}
