package me.critiq.backend.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.constant.SystemConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;

@Slf4j
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${jwt.key.public}")
    private RSAPublicKey publicKey;

    @Value("${jwt.key.private}")
    private RSAPrivateKey privateKey;

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;

    @Bean
//    @Order(1)
    public SecurityFilterChain authorizationFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        // swagger
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/**").permitAll()
                        // register and login
                        .requestMatchers(HttpMethod.GET, "/user/code").anonymous()
                        .requestMatchers(HttpMethod.POST, "/user/register").anonymous()
                        .requestMatchers(HttpMethod.POST, "/user/login").anonymous()
                        .requestMatchers("/oauth2/authorization/github").anonymous()
                        .requestMatchers("/login/oauth2/code/github").anonymous()

                        .anyRequest().authenticated()
                ).cors(Customizer.withDefaults())
                .csrf(CsrfConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .logout(LogoutConfigurer::disable)
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                ).oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userinfo -> userinfo.userService(oAuth2UserService))
                )
                // .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                );
        return http.build();
    }

//    @Bean
//    @Order(2)
//    public SecurityFilterChain authenticationFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests((authorize) -> authorize
//                        // swagger
//                        .requestMatchers("/swagger-ui/**").permitAll()
//                        .requestMatchers("/v3/**").permitAll()
//                        // swagger
//                        .requestMatchers(HttpMethod.GET, "/user/code").anonymous()
//                        .requestMatchers(HttpMethod.POST, "/user/register").anonymous()
//                        .requestMatchers(HttpMethod.POST, "/user/login").anonymous()
//                        .anyRequest().authenticated()
//                ).oauth2Login(oauth2 -> oauth2
//                        .userInfoEndpoint(userinfo -> userinfo.userService(oAuth2UserService()))
//                );
//        return http.build();
//    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
                .withPublicKey(this.publicKey)
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        var jwk = new RSAKey.Builder(this.publicKey)
                .privateKey(this.privateKey)
                .build();
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName(SystemConstant.LEVEL);
        grantedAuthoritiesConverter.setAuthorityPrefix(SystemConstant.LEVEL_);

        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
//        return userRequest -> {
//            DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
//            OAuth2User oAuth2User = delegate.loadUser(userRequest);
//
//            log.info(oAuth2User.toString());
//
//            // GitHub 用户信息 JSON 示例：
//            // {
//            //   "login": "octocat",
//            //   "id": 1,
//            //   "name": "monalisa octocat",
//            //   "email": "octocat@github.com",
//            //   ...
//            // }
//
//            String username = oAuth2User.getAttribute("login");
//            String email = oAuth2User.getAttribute("email");
//
//            // 你可以在这里做：
//            // 1. 把用户存数据库
//            // 2. 绑定到本地账号
//            // 3. 决定角色权限
//
//            return new DefaultOAuth2User(
//                    Collections.singleton(new SimpleGrantedAuthority("level_0")),
//                    oAuth2User.getAttributes(),
//                    "login" // key for username
//            );
//        };
//    }
}