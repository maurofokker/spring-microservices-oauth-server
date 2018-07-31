# Spring Microservice Oauth authorization server

* part of [microservices demo](https://github.com/maurofokker/microservices-demo)

* token management within oauth protected app
* capabilities:
  * create token
  * validate token
  * refresh token

* Add `@EnableAuthorizationServer` annotation 
  * will support with a number of defaults for the oauth authorization server
  * will expose a number of endpoints that can be use to receive, validate or refresh a token
* Create a configuration class (`AuthorizationServerConfig.java`) that extends `AuthorizationServerConfigurerAdapter` 
  that is going to facilitate the configuration of the project
  * add `@Configuration` annotation to new class so spring's IoC container will pick up this class and begin building beans
  * inject an `AuthenticationManager` because this demo is using password grant that is not enabled by default within Spring Cloud 
    support for Oauth 
  * override `configure(AuthorizationServerEndpointsConfigurer endpoints)` method and inject `AuthenticationManager` bean
    ```java
    @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.authenticationManager(authenticationManager);
        }
    ```
  * override `configure(ClientDetailsServiceConfigurer clients)` method that will allow to set up the oauth client that is need
    available within the application or within the authorization server (for this demo it will be in memory)
    ```java
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // establish a config for a web app client in the auth server config
        clients.inMemory()
                .withClient("webapp")
                .secret("{noop}secret")  // to avoid There is no PasswordEncoder mapped for the id \"null\"
                .authorizedGrantTypes("password")       // it is how user authenticate with the auth server
                .scopes("read,write,trust") // determine the access level to the different pieces of the API
    }
    ```
* Create a web security config class (`WebSecurityConfig.java`) that extends `WebSecurityConfigurerAdapter.class`
  ```java
    @Configuration
    public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    
        @Bean
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }
    
        // allow to specify the user within the web application
        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                    .withUser("user1")
                    .password("{noop}password") // to avoid There is no PasswordEncoder mapped for the id \"null\"
                    .roles("USER")
                    ;
        }
    }
  ```
* Test with Postman
  * POST method
  * URL: http://localhost:9090/oauth/token?grant_type=password&username=user1&password=password
    * QUERY STRING PARAMS: are according the oauth specification that denotes how we exchange the different calls
      between the different pieces of the architecture within oauth security in order for them to follow specific
      process to grant users access to some resource 
      * `grant_type`: password (see AuthorizationServerConfig.java) - oauth2 spec
      * `username`
      * `password`
  * Authentication: Basic
    * username: webapp
    * password: secret
  * Expected response:
  ```json
  {
      "access_token": "1355ede7-3a32-43df-9fb0-75883bab0636",
      "token_type": "bearer",
      "expires_in": 43199,
      "scope": "read,write,trust"
  }
  ```
  * a request agains the authorization server is made to get an access token `"1355ede7-3a32-43df-9fb0-75883bab0636"` 
    and that access token can be used to gain access to resources that are protected by the security architecture