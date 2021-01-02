#### 授权码模式认证
1. 访问认证服务器地址
    ```txt
    let url = 'http://localhost:7777/oauth/authorize?' ;
    url +=  'client_id=admin_service&'  ;
    url +=  'redirect_uri=http://localhost:8280/oauth/callback&' ;
    url += 'response_type=code&' ;
    url += 'state=123' ;
    window.location.href = url ;
    ```
2. 授权服务器跳转到登录页面
    ```txt
    2.1 填写用户登录信息并登录
    2.2 登录完成后授权服务器自动重定向到redirect_uri地址
    ```
3. 在redirect_uri所在的Controller中根据code、client_id、clientSecret等信息换取token并保存
    ```txt
    String url = "http://localhost:8080/oauth/token" ;
    HttpHeaders headers = new HttpHeaders() ;
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth("admin_service","secret");
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() ;
    params.add("code", code);
    params.add("grant_type", "authorization_code");
    params.add("redirect_uri", "http://localhost:8280/oauth/callback");
    HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers) ;
    ResponseEntity<TokenInfo> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, TokenInfo.class);
    TokenInfo tokenInfo = exchange.getBody();
    ```
4. 在步骤3中获取token后，保存token，并跳转到合适的前端页面（这部分后端代码中实现）
    ```txt
    // 用户信息放入session中，后面访问会使用
    session.setAttribute("tokenInfo", tokenInfo);
    // 获取token信息成功以后放入session中，并跳转到首页上，也可根据state字段跳到指定页面
    // 这里需要手动跳转页面的原因是因为步骤3是授权服务器自动跳过来的，而非前端页面发送的ajax请求
    response.sendRedirect("/");
    ```
#### 密码模式认证
1. 发送Post请求获取token
    ```txt
    String url = "http://localhost:8080/oauth/token" ;
    HttpHeaders headers = new HttpHeaders() ;
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth("admin_service","secret");
    
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() ;
    params.add("username", credentials.getUsername());
    params.add("password", credentials.getPassword());
    params.add("grant_type", "password");
    params.add("scope", "read write");
    HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers) ;
    
    ResponseEntity<TokenInfo> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, TokenInfo.class);
    TokenInfo tokenInfo = exchange.getBody();
    log.info("token info : {}", tokenInfo);
    
    ```
2. 将步骤1中获取的token后，保存token, 这里不需要跳到主页，步骤1是前端页面通过js发送的ajax请求
    ```txt
    // 用户信息放入session中，后面访问会使用
    session.setAttribute("tokenInfo", tokenInfo);
    ```
#### 刷新令牌
1. 授权服务器授权模式添加refresh_token且refreshTokenValiditySeconds设置过期时间
    ```txt
    // 刷新令牌过期时间
    .withClient("admin_service")
    .secret("secret")
    .scopes("read","write").autoApprove(true)
    .redirectUris("http://localhost:8280/oauth/callback")
    .accessTokenValiditySeconds(3600)
    // 刷新令牌过期时间
    .refreshTokenValiditySeconds(360000)
    .authorizedGrantTypes("implicit","refresh_token","password","authorization_code","refresh_token") ;
    ```
2. 授权服务器AuthorizationServerEndpointsConfigurer端点配置userDetailsService
    ```txt
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
        // 服务端如果要支持refresh则必须设置userDetailService
        //.userDetailsService(userDetailsService)
        ;
    }
    ```
3. 客户端编写刷新token代码
    ```txt
    // 注意授权服务器也要添加refresh_token模式支持
    //authorizedGrantTypes("implicit","refresh_token","password","authorization_code","refresh_token") ;
    String url = "http://localhost:8080/oauth/token" ;
    HttpHeaders headers = new HttpHeaders() ;
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth("admin_service","secret");
    
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() ;
    params.add("grant_type", "refresh_token");
    params.add("refresh_token", refreshToken);
    HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers) ;
    
    ResponseEntity<TokenInfo> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, TokenInfo.class);
    TokenInfo tokenInfo = exchange.getBody();
    // 注意这里调用了init方法，将过期时间赋值
    tokenInfo.init() ;
    ```
