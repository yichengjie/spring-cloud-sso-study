package com.yicj.study.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.yicj.study.zuul.common.CommonUtils;
import com.yicj.study.zuul.model.TokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

// 认证过滤器
@Slf4j
@Component
public class OAuth2AuthenticationFilter extends ZuulFilter {

    @Autowired
    private RestTemplate restTemplate ;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        log.info("====> authentication filter start..");
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization)){
            return null ;
        }
        // 校验token的合法性
        if (!StringUtils.startsWith(authorization,"bearer ")){
            return null ;
        }
        TokenInfo tokenInfo = getTokenInfo(authorization);
        request.setAttribute("tokenInfo", tokenInfo);
        return null;
    }

    // 校验token的合法性
    private TokenInfo getTokenInfo(String authorization) {
        String url = "http://localhost:7777/oauth/check_token" ;
        String token = StringUtils.substringAfter(authorization, "bearer ") ;
        HttpHeaders headers = new HttpHeaders() ;
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + CommonUtils.base64Encode("order_service:secret"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() ;
        params.add("token", token);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers) ;
        //String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
        //			Class<T> responseType
        ResponseEntity<TokenInfo> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, TokenInfo.class);
        log.info("token info : {}", exchange.getBody());
        return exchange.getBody() ;
    }
}
