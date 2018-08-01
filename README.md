# Spring Microservice Oauth authorization server

* part of [microservices demo](https://github.com/maurofokker/microservices-demo)

* token management within oauth protected app
* capabilities:
  * create token
  * validate token
  * refresh token
  
## Config Authorization Server

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

  * reference:
    * [secure-a-spring-boot-rest-api-with-json-web-token-reference-to-angular-integration](https://medium.com/@nydiarra/secure-a-spring-boot-rest-api-with-json-web-token-reference-to-angular-integration-e57a25806c50)
    * [spring security doc](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#getting-started)
    * [oauth-2-centralized-authorization-with-spring-boot-2-0-2-and-spring-security-5-and-jdbc-token-store](https://medium.com/@akourtim.ahmed/oauth-2-centralized-authorization-with-spring-boot-2-0-2-and-spring-security-5-and-jdbc-token-store-8dbc063bd5d4)
    
## Config Resource Server

* is the underlaying API server that is used to access user information 
* using a token from the authorization server allow to obtain acces to protected resources located in the rest API 
  
* to have the authorization server and resource server configuration in the same place is only applicable for the
  most trivial of applications 
* in order to establish the resource server we need:
  * add `@EnableResourceServer` annotation
  * and must exist a resource that is going to be protected by the resource server i.e.
  ```java
	@RequestMapping("/resource/endpoint")
	public String endpoint() {
		return "resource protected by the resource server";
	}
  ```
  * `@RestController` allow to expose the resource API (with `@RequestMapping`) and act as part
    of the Resource Server which works in tandem with the Auhorization Server to protect the resource
* Test
  * when hit direct to endpoint `http://localhost:9090/resource/endpoint` without access token the client receive a `401 Unauthorized`
  ```json
    {
        "error": "unauthorized",
        "error_description": "Full authentication is required to access this resource"
    }
  ```
  * in order to grant access to the resource a token must be created and give that token to the request of the endpoint
    1. hit authorization server to create token described above in this document (authorization server config)
    ```json
    {
        "access_token": "a4c6f9fd-7bf7-4823-9649-d1116f82df8e",
        "token_type": "bearer",
        "expires_in": 43199,
        "scope": "read,write,trust"
    }
    ```
    2. hit resource endpoint with token created above `http://localhost:9090/resource/endpoint?access_token=a4c6f9fd-7bf7-4823-9649-d1116f82df8e`
    ```
    resource protected by the resource server
    ```
    * `access_token`: is accepted by the resource server due to the relationship with authorization server within the authentication scheme

* Protect API endpoints with spring security `method security annotations`
  * add `@EnableGlobalMethodSecurity` annotation using the `prePostEnabled` set to true
  * add `@PreAuthorize("hasRole('ADMIN')")` method annotation to discerning who is able to invoke this method in the expression
    the user needs to have the ADMIN role to access this method
    ```java
      @RequestMapping("/resource/endpoint")
      @PreAuthorize("hasRole('ADMIN')")
      public String endpoint() {
          return "resource protected by the resource server";
      }
    ```
  * to test this configuration create an access token for user `admin` and then access to the annotated method
    if token is created with user `user` then resource server will not grant access

## Token management with JDBC

* database will store information about tokens that were issued by the authorization server 
* resource server will use the database to confirm the tokens received are valid and were issued by the authorization server
* this test is with an `HSQLDB`
  * download hsqldb
  * create `server.properties` in root directory of hsqldb
    ```properties
      server.database.0=file:hsqldb/poc
      server.dbname.0=testdb
      server.port=9137
    ```
  * build server from root directory of hsqldb
    ```
      $java -classpath lib/hsqldb.jar org.hsqldb.server.Server
    ```
  * start server
    ```
      $java -classpath lib/hsqldb.jar org.hsqldb.server.Server --database.0 file:hsqldb/poc --dbname.0 testdb 
    ```
  * hsqldb GUI can be found in `bin/runManagerSwing`
    * select `type: hsql database engine server` and user port defined in `server.properties`
* configure schema in spring boot creating `src/main/resources/schema.sql`
  * `schema.sql` is a special spring boot file name because it will pick the file and load any scripts within the file into 
    the jdbc store (hsqldb) prior to starting the application
    * copy schema from [spring-security-oauth2 github repository](https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/test/resources/schema.sql)
    * insert client details
    
## Oauth client

* Demo can be found [here](https://github.com/maurofokker/spring-microservices-oauth-client) 