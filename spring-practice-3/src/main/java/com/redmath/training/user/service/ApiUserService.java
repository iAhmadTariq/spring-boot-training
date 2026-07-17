package com.redmath.training.user.service;

import com.redmath.training.user.model.ApiUser;
import com.redmath.training.user.repository.ApiUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ApiUserService implements UserDetailsService {

    private final ApiUserRepository repository;

    ApiUserService(ApiUserRepository repository){
        this.repository = repository;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Optional<ApiUser> user = repository.findByUserName(username);

        if(user.isEmpty()){
            throw new UsernameNotFoundException("Username doesn't exist");
        }

        return User.withUsername(username).password(user.get().getPassword()).roles(user.get().getRoles().split(",")).build();

    }

    public ApiUser generateToken(String username){
        ApiUser user = repository.findByUserName(username).get();

        user.setToken(UUID.randomUUID().toString());
        return repository.save(user);
    }

    public ApiUser findByToken(String token){
        Optional<ApiUser> user = repository.findByToken(token);
        if(user.isEmpty()){
            throw new OAuth2AuthenticationException("Invalid Token");
        }
        return  user.get();
    }
}
