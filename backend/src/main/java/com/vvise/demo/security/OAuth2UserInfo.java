package com.vvise.demo.security;

import java.util.Map;

public abstract class OAuth2UserInfo {

    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();

    public static class GoogleOAuth2UserInfo extends OAuth2UserInfo {
        public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
            super(attributes);
        }

        @Override
        public String getId() {
            return (String) attributes.get("sub");
        }

        @Override
        public String getName() {
            return (String) attributes.get("name");
        }

        @Override
        public String getEmail() {
            return (String) attributes.get("email");
        }

        @Override
        public String getImageUrl() {
            return (String) attributes.get("picture");
        }
    }

    public static class GithubOAuth2UserInfo extends OAuth2UserInfo {
        public GithubOAuth2UserInfo(Map<String, Object> attributes) {
            super(attributes);
        }

        @Override
        public String getId() {
            return ((Integer) attributes.get("id")).toString();
        }

        @Override
        public String getName() {
            String name = (String) attributes.get("name");
            if (name == null || name.isEmpty()) {
                return (String) attributes.get("login");
            }
            return name;
        }

        @Override
        public String getEmail() {
            return (String) attributes.get("email");
        }

        @Override
        public String getImageUrl() {
            return (String) attributes.get("avatar_url");
        }
    }

    public static class MicrosoftOAuth2UserInfo extends OAuth2UserInfo {
        public MicrosoftOAuth2UserInfo(Map<String, Object> attributes) {
            super(attributes);
        }

        @Override
        public String getId() {
            return (String) attributes.get("sub");
        }

        @Override
        public String getName() {
            return (String) attributes.get("name");
        }

        @Override
        public String getEmail() {
            return (String) attributes.get("email");
        }

        @Override
        public String getImageUrl() {
            return null; // Microsoft doesn't provide image URL in OIDC userinfo
        }
    }
}
