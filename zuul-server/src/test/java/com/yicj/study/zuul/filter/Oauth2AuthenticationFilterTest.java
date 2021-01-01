package com.yicj.study.zuul.filter;

import com.yicj.study.zuul.model.TokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class Oauth2AuthenticationFilterTest {
    private RestTemplate restTemplate ;

    @Before
    public void before(){
        restTemplate = new RestTemplate() ;
    }

    @Test
    public void checkToken(){
        String authorization = "bearer 7800aaea-7669-4fba-8bbc-3c8c41db6349" ;
        String url = "http://localhost:7777/oauth/check_token" ;
        String token = StringUtils.substringAfter(authorization, "bearer ") ;
        HttpHeaders headers = new HttpHeaders() ;
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("order_service", "secret");
        //headers.set(HttpHeaders.AUTHORIZATION, "Basic " + CommonUtils.base64Encode("order_service:secret"));
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() ;
        params.add("token", token);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers) ;
        ResponseEntity<TokenInfo> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, TokenInfo.class);
        log.info("token info : {}", exchange.getBody());
    }
}
