package com.SkyRoute.UserService.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SkyRoute.UserService.Entity.OtpEntity;

import org.springframework.data.jpa.repository.JpaRepository;
public interface OtpRepository extends JpaRepository<OtpEntity,Long>{

	OtpEntity findByEmail(String email);


	OtpEntity findTopByEmailOrderByExpiryTimeDesc(String email);

}
