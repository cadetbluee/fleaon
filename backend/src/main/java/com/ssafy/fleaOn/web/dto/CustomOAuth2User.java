package com.ssafy.fleaOn.web.dto;

import com.ssafy.fleaOn.web.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final User user;

    public CustomOAuth2User(User user) {

        this.user = user;
    }

    @Override
    public Map<String, Object> getAttributes() {

        return Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "userIdentifier", user.getUserIdentifier(),
                "role", user.getRole()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return user.getRole();
            }
        });

        return collection;
    }

    @Override
    public String getName() {

        return user.getName();
    }

    public String getUserIdentifier() {

        return user.getUserIdentifier();
    }

//    public String getProfilePicture() {
//
//        return user.getProfilePicture();
//    }
//
//    public int getUserId(){
//
//        return user.getUserId();
//    }
    public String getEmail() {
        return user.getEmail();
    }
}
