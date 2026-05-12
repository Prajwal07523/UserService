package com.SkyRoute.UserService.Repository;

import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;

import com.SkyRoute.UserService.Entity.User;
import org.springframework.stereotype.Repository;
@Repository
public interface UserRepository extends JpaRepository<User,Long>{

	Optional<User> findByEmail(String email);
	boolean existsByEmailIgnoreCase(String email);
}
