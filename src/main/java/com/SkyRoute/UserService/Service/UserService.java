package com.SkyRoute.UserService.Service;

import com.SkyRoute.UserService.DTO.ChangePasswordDto;
import com.SkyRoute.UserService.DTO.ResetPasswordDto;
import com.SkyRoute.UserService.DTO.UserLoginDto;
import com.SkyRoute.UserService.DTO.UserProfileDto;
import com.SkyRoute.UserService.DTO.UserRegistrationDto;

import jakarta.mail.MessagingException;

public interface UserService {
   public String register(UserRegistrationDto registerDto) throws MessagingException;
   public String login(UserLoginDto loginDto);
   public void sendOtpEmail(String email) throws MessagingException;
   public UserProfileDto getUserById(Long userId);
   public void changePassword(Long userId, ChangePasswordDto dto);
   public void resetPassword(ResetPasswordDto dto);
}
