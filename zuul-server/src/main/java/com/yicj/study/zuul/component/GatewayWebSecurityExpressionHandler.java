package com.yicj.study.zuul.component;

import com.yicj.study.zuul.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

@Component
public class GatewayWebSecurityExpressionHandler extends OAuth2WebSecurityExpressionHandler {
    @Autowired
    private PermissionService permissionService ;

    @Override
    protected StandardEvaluationContext createEvaluationContextInternal(Authentication authentication, FilterInvocation invocation) {
        StandardEvaluationContext evaluationContextInternal = super.createEvaluationContextInternal(authentication, invocation);
        evaluationContextInternal.setVariable("permissionService", permissionService);
        return evaluationContextInternal ;
    }
}
