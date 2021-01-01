package com.yicj.study.admin.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.yicj.study.admin.common.CommonUtils;
import com.yicj.study.admin.model.TokenInfo;

import javax.servlet.http.HttpServletRequest;

public class SessionTokenFilter extends ZuulFilter {
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
            String accessToken = tokenInfo.getAccess_token();
            currentContext.addZuulRequestHeader("Authorization", "bearer " +accessToken);
        }
        return null;
    }
}
