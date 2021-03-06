package com.yicj.study.resource.controller;

import com.yicj.study.resource.model.OrderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Slf4j
@RestController
public class HomeController {

    @RequestMapping("/test")
    public String test(HttpServletRequest request){
        Enumeration<String> headerNames = request.getHeaderNames();
        log.info("---------------------header-----------------------");
        while (headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            log.info("====> {} : {}", headerName, request.getHeader(headerName));
        }
        log.info("---------------------header-----------------------");
        return "hello world !" ;
    }

    @GetMapping("/hello")
    public String hello(@AuthenticationPrincipal String username){
        log.info("====> username : {}", username);
        return "hello world" ;
    }

    @GetMapping("/orders/{id}")
    public OrderInfo orders(@PathVariable String id){
        OrderInfo orderInfo = new OrderInfo() ;
        orderInfo.setId(id);
        orderInfo.setProductId("5");
        return orderInfo ;
    }
}
