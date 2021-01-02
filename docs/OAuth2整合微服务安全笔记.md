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
4. 验证资源服务器功能
    ```text
    4.1 获取token是否正常: http://localhost:7777/oauth/token
        header参数 -> Authorization : Basic clientId clientSecret
        form参数 -> username: admin, password:secret, grand_type:password, scope: read write
    4.2 检验token是否正常：http://localhost:7777/oauth/check_token
        header参数 -> Authorization: Basic clientId clientSecret
        form参数 -> token: token (无需添加bearer前缀)
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
4. 验证资源服务器功能
    ```txt
    4.1 访问服务是否正常： http://localhost:8082/test
        header参数 --> Authorization: bearer token
    ```
#### 引入zuul做安全认证
1. 添加认证过滤器
    ```java
    @Slf4j
    @Component
    public class OAuth2AuthenticationFilter extends ZuulFilter {
        @Autowired
        private RestTemplate restTemplate ;
        @Override
        public String filterType() {
            return "pre";
        }
        @Override
        public int filterOrder() {
            return 1;
        }
        @Override
        public boolean shouldFilter() {
            return true;
        }
        @Override
        public Object run() throws ZuulException {
            log.info("====> authentication filter start..");
            RequestContext currentContext = RequestContext.getCurrentContext();
            HttpServletRequest request = currentContext.getRequest();
            String authorization = request.getHeader("Authorization");
            if (StringUtils.isBlank(authorization)){
                return null ;
            }
            // 校验token的合法性
            if (!StringUtils.startsWith(authorization,"bearer ")){
                return null ;
            }
            TokenInfo tokenInfo = getTokenInfo(authorization);
            request.setAttribute("tokenInfo", tokenInfo);
            return null;
        }
        // 校验token的合法性
        private TokenInfo getTokenInfo(String authorization) {
            String url = "http://localhost:7777/oauth/check_token" ;
            String token = StringUtils.substringAfter(authorization, "bearer ") ;
            HttpHeaders headers = new HttpHeaders() ;
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + CommonUtils.base64Encode("order_service:secret"));
    
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>() ;
            params.add("token", token);
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers) ;
            //String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
            //			Class<T> responseType
            ResponseEntity<TokenInfo> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, TokenInfo.class);
            log.info("token info : {}", exchange.getBody());
            return exchange.getBody() ;
        }
    }
    ```
2. 添加授权过滤器
    ```java
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
    ```
3. Token信息entity编写
    ```java
    @Data
    public class TokenInfo {
        private boolean active;
        private String user_name ;
        private String client_id ;
        private Date exp ;
        private String [] scope ;
        private String [] authorities ;
        private String [] aud ;
    }
    ```
4. 删除资源服务器（order-service）中认证授权相关代码
    ```txt
    3.1 删除Oauth2ResourceServerConfig.java
    3.2 删除Oauth2WebSecurityConfig.java
    3.3 删除pom依赖spring-cloud-starter-oauth2
    ```
#### 使用jwt替换默认uuid的token
##### 授权服务器修改
1. 授权服务器添加jwt的tokenStore及tokenEnhancer
    ```txt
    @Bean
    public TokenStore tokenStore(){
        return new JwtTokenStore(tokenEnhancer()) ;
    }
    // 这里注入到spring容器，否则资源服务器启动是，获取tokenKey时报404
    // 只有存在jwtTokenEnhancer的bean在容器中TokenKeyEndpoint端点才会发布
    @Bean
    public JwtAccessTokenConverter tokenEnhancer() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("123456");
        return converter ;
    }
    ```
2. AuthorizationServerSecurityConfigurer配置tokenKeyAccess发布获取tokenKey端点
    ```txt
       @Override
        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
            security
                // 获取tokenKey接口
                .tokenKeyAccess("isAuthenticated()")
                // 检验token是否合法，这里如果不设置，资源服务器将无法进行token校验
                .checkTokenAccess("isAuthenticated()");
        }
    ```
3. AuthorizationServerEndpointsConfigurer配置使用tokenStore和tokenEnhancer
    ```txt
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
            // 设置用来支持前四种授权类型的（用户名密码）
            .tokenStore(tokenStore())
            .tokenEnhancer(tokenEnhancer())
            .authenticationManager(authenticationManager)
            // 服务端如果要支持refresh则必须设置userDetailService
            // 这里用来支持refresh_token授权类型的（只有用户名）
            .userDetailsService(userDetailsService)
        ;
    }
    ```
##### 网关服务器修改
1. 添加依赖
    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-oauth2</artifactId>
    </dependency>
    ```
2. 删除自定义filter
3. 添加配置
    ```properties
    # 获取验签token的tokenKey地址(授权服务器)
    security.oauth2.resource.jwt.key-uri=http://localhost:7777/oauth/token_key
    # 身份认证的信息
    security.oauth2.client.client-id=zuul_server
    security.oauth2.client.client-secret=secret
    ```
4. 添加网关安全配置
    ```java
    // 网关作为资源服务器存在
    @Configuration
    @EnableResourceServer
    public class GatewaySecurityConfig extends ResourceServerConfigurerAdapter {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                .antMatchers("/oauth/**").permitAll()
                .anyRequest().authenticated();
        }
    }
    ```
##### 微服务端修改
1. 添加依赖
    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-oauth2</artifactId>
    </dependency>
    ```
2. 添加token验证配置(因为此时传递过来的是token而不是放在header中的明文，所以也需要校验)
    ```properties
    # 获取验签token的tokenKey地址(授权服务器)
    security.oauth2.resource.jwt.key-uri=http://localhost:7777/oauth/token_key
    security.oauth2.client.client-id=order_service
    security.oauth2.client.client-secret=secret
    ```
3. 启动类添加@EnableResourceServer注解
    ```java
    @EnableResourceServer
    @EnableDiscoveryClient
    @SpringBootApplication
    public class OrderServiceApiApplication {
        public static void main(String[] args) {
            SpringApplication.run(OrderServiceApiApplication.class, args) ;
        }
    }
    ```
4. Controller请求方法通过@AuthenticationPrincipal String username获取
    ```txt
    @GetMapping("/hello")
    public String hello(@AuthenticationPrincipal String username){
        return "hello world" ;
    }
    ```


    


