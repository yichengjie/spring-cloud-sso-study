package com.yicj.study.admin.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TokenInfo {
    private String access_token ;
    private String token_type ;
    private String refresh_token ;
    private Long expires_in ;
    private String scope ;
    private LocalDateTime exp ;

    public TokenInfo init(){
        this.exp = LocalDateTime.now().plusSeconds(this.expires_in -2);
        return this ;
    }

    // 是否已过期
    public boolean isExpired(){
        return this.exp.isBefore(LocalDateTime.now()) ;
    }

}
