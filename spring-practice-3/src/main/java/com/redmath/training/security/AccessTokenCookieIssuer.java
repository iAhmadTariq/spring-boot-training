package com.redmath.training.security;

import com.redmath.training.user.model.ApiUser;
import com.redmath.training.user.service.ApiUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AccessTokenCookieIssuer {

    private static final String COOKIE_NAME = "access_token";
    private static final int COOKIE_MAX_AGE_SECONDS = 24 * 60 * 60;

    private final ApiUserService userService;

    public AccessTokenCookieIssuer(ApiUserService userService) {
        this.userService = userService;
    }

    public void issueFor(String username, HttpServletResponse response) throws IOException {
        ApiUser apiUser = userService.generateToken(username);

        Cookie cookie = new Cookie(COOKIE_NAME, apiUser.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        response.addCookie(cookie);

        response.sendRedirect("/");
    }
}
