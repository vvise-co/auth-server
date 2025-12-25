package com.vvise.demo.security;

import com.vvise.demo.entity.User;
import com.vvise.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        try {
            return processOidcUser(userRequest, oidcUser);
        } catch (Exception ex) {
            log.error("OIDC authentication error", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId,
                oidcUser.getAttributes());

        String email = oAuth2UserInfo.getEmail();
        if (!StringUtils.hasText(email)) {
            // Try to get email from OIDC claims
            email = oidcUser.getEmail();
        }

        if (!StringUtils.hasText(email)) {
            throw new RuntimeException("Email not found from OAuth2 provider");
        }

        User.AuthProvider provider = OAuth2UserInfoFactory.getAuthProvider(registrationId);

        User user = userService.createOrUpdateOAuth2User(
                email,
                oAuth2UserInfo.getName(),
                oAuth2UserInfo.getImageUrl(),
                provider,
                oAuth2UserInfo.getId()
        );

        return UserPrincipal.createOidc(user, oidcUser.getAttributes(), oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
