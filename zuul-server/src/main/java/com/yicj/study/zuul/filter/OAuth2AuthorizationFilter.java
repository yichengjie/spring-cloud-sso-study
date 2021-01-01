package com.yicj.study.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.yicj.study.zuul.model.TokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

// 授权过滤器
@Slf4j
@Component
public class OAuth2AuthorizationFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 3;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        log.info("====> authorization filter start..");
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        TokenInfo tokenInfo = (TokenInfo) request.getAttribute("tokenInfo");
        if (tokenInfo != null && tokenInfo.isActive()){
            if (!hasPermission(tokenInfo, request)){
                log.info("audit log update fail 403");
                handleError(403, currentContext) ;
            }
            // 将用户信息放在header中传给后面的微服务
            String username = tokenInfo.getUser_name();
            currentContext.addZuulRequestHeader("username", username);
        }else {
            if (!isOauthServerRequest(request)){
                log.info("audit log update fail 401");
                handleError(401, currentContext) ;
            }
        }
        return null;
    }

    private boolean isOauthServerRequest(HttpServletRequest request){
        String uri = request.getRequestURI();
        return StringUtils.startsWith(uri, "/oauth") ;
    }

    // 是否有权限访问资源
    private boolean hasPermission(TokenInfo tokenInfo, HttpServletRequest request) {
        return RandomUtils.nextInt() %2 == 0 ;
    }

    private void handleError(int status, RequestContext ctx) {
        ctx.getResponse().setContentType("application/json");
        ctx.setResponseStatusCode(status);
        ctx.setResponseBody("{\"message\":\"audit fail !\"}");
        ctx.setSendZuulResponse(false);
    }
}
