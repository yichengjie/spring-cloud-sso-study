package com.yicj.study.auth.config;

import com.yicj.study.auth.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager ;

    @Autowired
    private UserDetailsServiceImpl userDetailsService ;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("zuul_server")
                .secret("secret")
                .scopes("read","write").autoApprove(true)
                .accessTokenValiditySeconds(3600)
                .refreshTokenValiditySeconds(360000)
                //.resourceIds("ZUUL_SERVER")
                .authorizedGrantTypes("implicit","refresh_token","password","authorization_code","refresh_token")
                .and()
                //
                .withClient("order_service")
                .secret("secret")
                .scopes("read","write").autoApprove(true)
                .accessTokenValiditySeconds(3600)
                .refreshTokenValiditySeconds(360000)
                //.resourceIds("ORDER_SERVICE")
                .authorizedGrantTypes("implicit","refresh_token","password","authorization_code","refresh_token")
                .and()
                //注意这里没有填写resourceIds，表示使用整个clientId获取的token能访问其他所有的资源服务器
                .withClient("admin_service")
                .secret("secret")
                .scopes("read","write").autoApprove(true)
                .redirectUris("http://localhost:8280/oauth/callback")
                .accessTokenValiditySeconds(3600)
                // 刷新令牌过期时间
                .refreshTokenValiditySeconds(360000)
                .authorizedGrantTypes("implicit","refresh_token","password","authorization_code","refresh_token") ;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
            // 设置用来支持前四种授权类型的（用户名密码）
            .tokenStore(tokenStore())
            .tokenEnhancer(jwtTokenEnhancer())
            .authenticationManager(authenticationManager)
            // 服务端如果要支持refresh则必须设置userDetailService
            // 这里用来支持refresh_token授权类型的（只有用户名）
            .userDetailsService(userDetailsService)
        ;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
            // 获取tokenKey接口
            .tokenKeyAccess("isAuthenticated()")
            // 检验token是否合法，这里如果不设置，资源服务器将无法进行token校验
            .checkTokenAccess("isAuthenticated()");
    }

    @Bean
    public TokenStore tokenStore(){
        return new JwtTokenStore(jwtTokenEnhancer()) ;
    }

    // 这里注入到spring容器，否则资源服务器启动是，获取tokenKey时报404
    // 只有存在jwtTokenEnhancer的bean在容器中TokenKeyEndpoint端点才会发布
    @Bean
    public JwtAccessTokenConverter jwtTokenEnhancer() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("123456");
        return converter ;
    }
}
