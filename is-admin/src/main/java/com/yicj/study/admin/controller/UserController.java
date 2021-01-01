package com.yicj.study.admin.controller;

import com.yicj.study.admin.model.Credentials;
import com.yicj.study.admin.model.TokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private RestTemplate restTemplate ;

    @PostMapping("/login")
    public void login(@RequestBody  Credentials credentials, HttpSession session){
        String url = "http://localhost:7777/oauth/token" ;
        HttpHeaders headers = new HttpHeaders() ;
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("admin_service","secret");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() ;
        params.add("username", credentials.getUsername());
        params.add("password", credentials.getPassword());
        params.add("grant_type", "password");
        params.add("scope", "read write");
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers) ;

        ResponseEntity<TokenInfo> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, TokenInfo.class);
        TokenInfo tokenInfo = exchange.getBody();
        log.info("token info : {}", tokenInfo);
        // 用户信息放入session中，后面访问会使用
        session.setAttribute("tokenInfo", tokenInfo);
    }
}
