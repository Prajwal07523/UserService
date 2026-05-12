package com.SkyRoute.UserService.Controller;
import org.springframework.web.bind.annotation.RestController;




import com.SkyRoute.UserService.DTO.ChangePasswordDto;
import com.SkyRoute.UserService.DTO.ResetPasswordDto;
import com.SkyRoute.UserService.DTO.UserLoginDto;
import com.SkyRoute.UserService.DTO.UserProfileDto;
import com.SkyRoute.UserService.DTO.UserRegistrationDto;
import com.SkyRoute.UserService.Entity.OtpEntity;
import com.SkyRoute.UserService.Repository.OtpRepository;
import com.SkyRoute.UserService.Service.UserService;
import com.SkyRoute.UserService.util.OtpUtil;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/auth")
public class UserController {
   
	@Autowired
	private UserService userService;
	
	@Autowired
	private OtpRepository otpRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
	
	@PostMapping("/register")
	public ResponseEntity<String>registerUser(@Valid @RequestBody UserRegistrationDto registerDto) throws MessagingException{
		logger.info("Request to register user with email: {}", registerDto.getEmail());
		String user=userService.register(registerDto);

		
		return new ResponseEntity<>(user,HttpStatus.CREATED);
	}
	
	@PostMapping("/login")
	public ResponseEntity<String>login(@RequestBody UserLoginDto loginDto){
		logger.info("Request to login user: {}", loginDto.getEmail());
		return new ResponseEntity<>(userService.login(loginDto),HttpStatus.OK);
	}
	
	@PostMapping("/verify-otp")
	public boolean verifyOtp(@RequestParam String email, @RequestParam String enteredOtp) {

		 OtpEntity otp = otpRepository.findTopByEmailOrderByExpiryTimeDesc(email);

	    if (otp == null) return false;
	    if (otp.getExpiryTime().isBefore(LocalDateTime.now())) return false;

	    return otp.getOtp().equals(enteredOtp);
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<String> forgotPassword(@RequestParam String email) throws MessagingException {
		logger.info("Forgot password request for email: {}", email);
		userService.sendOtpEmail(email);
		return ResponseEntity.ok("OTP sent to " + email);
	}

	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
		logger.info("Reset password request for email: {}", dto.getEmail());
		userService.resetPassword(dto);
		return ResponseEntity.ok("Password reset successfully");
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<UserProfileDto> getUserById(@PathVariable Long userId) {
		logger.info("Request to get user profile for userId: {}", userId);
		return ResponseEntity.ok(userService.getUserById(userId));
	}

	@PutMapping("/user/{userId}/change-password")
	public ResponseEntity<String> changePassword(@PathVariable Long userId,
			@Valid @RequestBody ChangePasswordDto dto) {
		logger.info("Request to change password for userId: {}", userId);
		userService.changePassword(userId, dto);
		return ResponseEntity.ok("Password changed successfully");
	}
}
