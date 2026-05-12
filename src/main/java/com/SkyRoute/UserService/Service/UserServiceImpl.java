package com.SkyRoute.UserService.Service;

import com.SkyRoute.UserService.DTO.ChangePasswordDto;

import com.SkyRoute.UserService.DTO.ResetPasswordDto;
import com.SkyRoute.UserService.DTO.UserLoginDto;
import com.SkyRoute.UserService.DTO.UserProfileDto;
import com.SkyRoute.UserService.DTO.UserRegistrationDto;
import com.SkyRoute.UserService.Entity.OtpEntity;
import com.SkyRoute.UserService.Entity.User;
import com.SkyRoute.UserService.Exception.EmailAlreadyExistsException;
import com.SkyRoute.UserService.Repository.OtpRepository;
import com.SkyRoute.UserService.Repository.UserRepository;
import com.SkyRoute.UserService.util.JWTUtil;
import com.SkyRoute.UserService.util.OtpUtil;

import jakarta.mail.MessagingException;
import templates.OtpEmailTemplate;
import templates.RegistrationEmailTemplate;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserServiceImpl implements UserService {
    
	@Autowired
    private UserRepository userRepository;
	
	@Autowired
	private OtpRepository otpRepository;
	
	@Autowired
    private final JWTUtil jwtutil;
	
	@Autowired
	private EmailService emailService;
	@Autowired
    private  PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    // ✅ All dependencies via constructor → easier testing / no field injection
    public UserServiceImpl(UserRepository userRepository,
                           JWTUtil jwtutil,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtutil = jwtutil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String register(UserRegistrationDto registerDto) {
       try {
        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(registerDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + registerDto.getEmail());
        }

        User user = new User();
        user.setName(registerDto.getName());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRole(registerDto.getRole());

        userRepository.save(user);
        logger.info("User registered successfully: {}", registerDto.getEmail());
        String html = RegistrationEmailTemplate.buildRegistrationHtml(user.getName());

        emailService.sendHtmlEmail(
                "prajwalchikkagalagali@gmail.com",
                "Registration Successful",
                html
        );
        sendOtpEmail(user.getEmail());
        return "User Registered Successfully and Verify OTP";
        
       }
       catch (MessagingException e) {
           throw new RuntimeException("Failed to send email", e);
       }

    }

    @Override
    public String login(UserLoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
            .orElseThrow(() -> new RuntimeException("User Email is not Registered"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            logger.warn("Invalid login attempt for email: {}", loginDto.getEmail());
            throw new RuntimeException("Invalid Credentials");
        }

        logger.info("User logged in successfully: {}", loginDto.getEmail());
        return jwtutil.generateToken(user);
    }
    
    @Override
    public void sendOtpEmail(String email)  {
    	try {
        // generate otp
        String otp = OtpUtil.generateOtp();

        // build html
        String html = OtpEmailTemplate.buildOtpHtml(otp);

        // send email
        emailService.sendHtmlEmail(email, "Your OTP Code", html);

        // save otp in DB or cache (Redis) with expiry
        saveOtpForUser(email, otp);  // implement this
        
    }
        catch (MessagingException e) {
               throw new RuntimeException("Failed to send otp", e);
           }

    }
    
    public void saveOtpForUser(String email, String otp) {
        OtpEntity entity = new OtpEntity();
        entity.setEmail(email);
        entity.setOtp(otp);
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(entity);
    }

    @Override
    public UserProfileDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return new UserProfileDto(user.getUserId(), user.getName(), user.getEmail(), user.getRole());
    }
    
    

    @Override
    public void changePassword(Long userId, ChangePasswordDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed successfully for userId: {}", userId);
    }

    @Override
    public void resetPassword(ResetPasswordDto dto) {
        OtpEntity otp = otpRepository.findTopByEmailOrderByExpiryTimeDesc(dto.getEmail());
        if (otp == null || otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired or not found");
        }
        if (!otp.getOtp().equals(dto.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        otpRepository.delete(otp);
        logger.info("Password reset successfully for email: {}", dto.getEmail());
    }
}



























