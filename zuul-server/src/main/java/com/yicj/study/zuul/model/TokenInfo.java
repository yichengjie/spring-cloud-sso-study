package com.yicj.study.zuul.model;

import lombok.Data;

import java.util.Date;


@Data
public class TokenInfo {
    private boolean active;
    private String username ;
    private String client_id ;
    private Date exp ;
    private String [] scope ;
    private String [] authorities ;
    private String [] aud ;

}
