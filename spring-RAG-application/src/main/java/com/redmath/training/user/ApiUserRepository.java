package com.redmath.training.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiUserRepository extends JpaRepository<ApiUser, Long> {

  Optional<ApiUser> findByUserName(String username);

  Optional<ApiUser> findByToken(String token);

}
