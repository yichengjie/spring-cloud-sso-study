package com.yicj.study.admin.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.yicj.study.admin.model.TokenInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@Component
public class SessionTokenFilter extends ZuulFilter {

    @Autowired
    private RestTemplate restTemplate ;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        TokenInfo tokenInfo = (TokenInfo)request.getSession().getAttribute("tokenInfo");
        if (tokenInfo !=null){
            // 判断令牌是否过期，如果过期则刷新令牌
            if (tokenInfo.isExpired()){
                tokenInfo = refreshToken(tokenInfo.getRefresh_token()) ;
                request.getSession().setAttribute("tokenInfo", tokenInfo);
            }
            String accessToken = tokenInfo.getAccess_token();
            currentContext.addZuulRequestHeader("Authorization", "bearer " +accessToken);
        }
        return null;
    }


    private TokenInfo refreshToken(String refreshToken){
        String url = "http://localhost:8080/oauth/token" ;
        HttpHeaders headers = new HttpHeaders() ;
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("admin_service","secret");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>() ;
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", refreshToken);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers) ;

        ResponseEntity<TokenInfo> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, TokenInfo.class);
        TokenInfo tokenInfo = exchange.getBody();
        // 注意这里调用了init方法，将过期时间赋值
        tokenInfo.init() ;
        return tokenInfo ;
    }
}
