#### 授权服务器开发
1. 添加依赖
    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-oauth2</artifactId>
    </dependency>
    ```
2. Oauth2授权服务器配置
    ```java
    @Configuration
    @EnableAuthorizationServer
    public class OAuth2AuthServerConfig extends AuthorizationServerConfigurerAdapter {
        @Autowired
        private AuthenticationManager authenticationManager ;
        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.inMemory()
                    .withClient("zuul_server")
                    .secret("secret")
                    .scopes("read","write").autoApprove(true)
                    .accessTokenValiditySeconds(360000)
                    .resourceIds("ZUUL_SERVER")
                    .authorizedGrantTypes("implicit","refresh_token","password","authorization_code")
                    .and()
                    //
                    .withClient("order_service")
                    .secret("secret")
                    .scopes("read","write").autoApprove(true)
                    .accessTokenValiditySeconds(360000)
                    .resourceIds("ORDER_SERVICE")
                    .authorizedGrantTypes("implicit","refresh_token","password","authorization_code") ;
        }
        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.authenticationManager(authenticationManager) ;
        }
        @Override
        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
            security
                // 获取tokenKey接口
                .tokenKeyAccess("isAuthenticated()")
                // 检验token是否合法，这里如果不设置，资源服务器将无法进行token校验
                .checkTokenAccess("isAuthenticated()");
        }
    }
    ```