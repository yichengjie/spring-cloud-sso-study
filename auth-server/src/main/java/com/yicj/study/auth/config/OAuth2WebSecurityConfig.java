package com.yicj.study.auth.config;

import com.yicj.study.auth.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class OAuth2WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService ;
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // auth.inMemoryAuthentication()
        // .withUser("guest").password("guest").authorities("ROLE_GUEST").and()
        // .withUser("admin").password("admin").authorities("ROLE_GUEST","ROLE_ADMIN") ;
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder())
        ;
    }

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance() ;
    }
}
