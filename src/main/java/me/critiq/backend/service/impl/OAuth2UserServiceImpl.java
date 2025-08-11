package me.critiq.backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.domain.entity.ThirdUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // oAuth2用户类
        var oAuth2User = super.loadUser(userRequest);

        // 转换oAuth2User -> ThirdUser
        var attributes = oAuth2User.getAttributes();
        var token = userRequest.getAccessToken();
        var thirdUser = ThirdUser.builder()
                .email(attributes.get("email").toString())
                .nickName(attributes.get("name").toString())
                .icon(attributes.get("avatar_url").toString())
                .thirdId(Long.valueOf(attributes.get("id").toString()))
                .credentials(token.getTokenValue())
                .credentialsExpiry(token.getExpiresAt())
                .registrationId(userRequest.getClientRegistration().getRegistrationId())
                .build();

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("level_0")),
                oAuth2User.getAttributes(),
                "name" // key for username
        );
    }
}
