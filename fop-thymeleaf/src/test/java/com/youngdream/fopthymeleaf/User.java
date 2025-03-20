package com.youngdream.fopthymeleaf;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private String uid;
    private String name;
    private Integer age;
    private String extInfo;
}
