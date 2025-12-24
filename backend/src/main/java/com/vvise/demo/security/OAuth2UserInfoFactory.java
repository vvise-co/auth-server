package com.vvise.demo.security;

import com.vvise.demo.entity.User;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(User.AuthProvider.GOOGLE.name())) {
            return new OAuth2UserInfo.GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(User.AuthProvider.GITHUB.name())) {
            return new OAuth2UserInfo.GithubOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(User.AuthProvider.MICROSOFT.name())) {
            return new OAuth2UserInfo.MicrosoftOAuth2UserInfo(attributes);
        } else {
            throw new RuntimeException("Login with " + registrationId + " is not supported.");
        }
    }

    public static User.AuthProvider getAuthProvider(String registrationId) {
        return User.AuthProvider.valueOf(registrationId.toUpperCase());
    }
}
