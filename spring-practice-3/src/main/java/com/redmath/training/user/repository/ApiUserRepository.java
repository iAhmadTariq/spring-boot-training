package com.redmath.training.user.repository;

import com.redmath.training.user.model.ApiUser;
import com.redmath.training.user.model.ApiUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiUserRepository extends JpaRepository<ApiUser,Long> {

    public Optional<ApiUser> findByUserName(String username);

    public Optional<ApiUser> findByToken(String token);

}
