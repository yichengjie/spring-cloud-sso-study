package com.yicj.study.resource.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public String hello(){
        return "hello world" ;
    }
}
