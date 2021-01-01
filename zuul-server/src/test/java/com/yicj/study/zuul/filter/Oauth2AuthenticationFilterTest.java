package com.yicj.study.zuul.filter;

import com.yicj.study.zuul.common.CommonUtils;
import com.yicj.study.zuul.model.TokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
public class Oauth2AuthenticationFilterTest {

    private RestTemplate restTemplate ;

    @Before
    public void before(){
        restTemplate = new RestTemplate() ;
    }

    @Test
    public void checkToken(){
        String authorization = "bearer ef1844da-af96-446a-87a7-04d37ddab385" ;
        String url = "http://localhost:7777/oauth/check_token" ;
        String token = StringUtils.substringAfter(authorization, "bearer ") ;
        HttpHeaders headers = new HttpHeaders() ;
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + CommonUtils.base64Encode("order_service:secret"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() ;
        params.add("token", token);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers, params) ;
        //String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
        //			Class<T> responseType
        restTemplate.exchange(url, HttpMethod.POST, httpEntity, Object.class);
        //log.info("token info : {}", exchange.getBody());
    }
}
