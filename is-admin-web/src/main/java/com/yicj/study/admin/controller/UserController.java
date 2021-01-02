package com.yicj.study.admin.controller;

import com.yicj.study.admin.model.TokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private RestTemplate restTemplate ;

    @GetMapping("/oauth/callback")
    public void callback(@RequestParam String code, String state,
             HttpServletResponse response, HttpSession session) throws IOException {
        log.info("state is {}", state);
        String url = "http://localhost:8080/oauth/token" ;
        HttpHeaders headers = new HttpHeaders() ;
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("admin_service","secret");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() ;
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", "http://localhost:8280/oauth/callback");
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers) ;

        ResponseEntity<TokenInfo> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, TokenInfo.class);
        TokenInfo tokenInfo = exchange.getBody();
        log.info("token info : {}", tokenInfo);
        // 用户信息放入session中，后面访问会使用
        session.setAttribute("tokenInfo", tokenInfo);
        // 获取token信息成功以后放入session中，并跳转到首页上，也可根据state字段跳到指定页面
        response.sendRedirect("/");
    }


    @GetMapping("/logout")
    public boolean logout(HttpSession session){
        session.invalidate();
        return true ;
    }


    @GetMapping("/me")
    public boolean me(HttpSession session){
        TokenInfo tokenInfo = (TokenInfo)session.getAttribute("tokenInfo");
        if (tokenInfo != null){
            return true ;
        }
        return false ;
    }
}
