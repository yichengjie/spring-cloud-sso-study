package com.yicj.study.zuul.service.impl;

import com.yicj.study.zuul.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {
    @Override
    public boolean hasPermission(HttpServletRequest request, Authentication authentication) {

        return checkPermission(request, authentication);
    }
    // 校验用户的操作权限
    private boolean checkPermission(HttpServletRequest request, Authentication authentication){
        log.info("===> uri : {}", request.getRequestURI());
        log.info("===> authentication :{}", authentication);
        // 这里去校验用户是否有操作权限
        return RandomUtils.nextInt() %2 ==0 ;
    }
}
