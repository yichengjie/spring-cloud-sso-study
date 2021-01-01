#### 授权服务器开发
1. 添加依赖
    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-oauth2</artifactId>
    </dependency>
    ```
2. OAuth2授权服务器配置类
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
3. OAuth2 Web安全配置类
    ```java
    @Configuration
    @EnableWebSecurity
    public class OAuth2WebSecurityConfig extends WebSecurityConfigurerAdapter {
        @Bean
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }
        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                    .withUser("guest").password("guest").authorities("ROLE_GUEST").and()
                    .withUser("admin").password("admin").authorities("ROLE_GUEST","ROLE_ADMIN") ;
        }
        @Bean
        public static PasswordEncoder passwordEncoder(){
            return NoOpPasswordEncoder.getInstance() ;
        }
    }
    ```
#### 资源服务器开发
1. 添加依赖
    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-oauth2</artifactId>
    </dependency>
    ```
2. OAuth2资源服务器配置类
    ```java
    @Configuration
    @EnableResourceServer
    public class Oauth2ResourceServerConfig extends ResourceServerConfigurerAdapter {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                .antMatchers(HttpMethod.GET,"/test").hasRole("ADMIN")
                .antMatchers("/**").authenticated()
                .and().csrf().disable() ;
        }
        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.resourceId("ORDER_SERVICE") ;
        }
    }
    ```
3. OAuth2 Web安全配置类
    ```java
    @Configuration
    @EnableWebSecurity
    public class Oauth2WebSecurityConfig extends WebSecurityConfigurerAdapter {
        // 资源服务器的令牌服务// 如何验证令牌
        @Bean
        public ResourceServerTokenServices tokenServices(){
            RemoteTokenServices tokenServices = new RemoteTokenServices() ;
            tokenServices.setClientId("order_service");
            tokenServices.setClientSecret("secret");
            tokenServices.setCheckTokenEndpointUrl("http://localhost:7777/oauth/check_token");
            return tokenServices ;
        }
        // 认证跟用户相关信息就要配置AuthenticationManager
        @Bean
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            OAuth2AuthenticationManager authenticationManager = new OAuth2AuthenticationManager() ;
            authenticationManager.setTokenServices(tokenServices());
            return authenticationManager;
        }
    }
    ```
    


