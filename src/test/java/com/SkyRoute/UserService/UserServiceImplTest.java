package com.SkyRoute.UserService;

import com.SkyRoute.UserService.DTO.UserLoginDto;

import com.SkyRoute.UserService.DTO.UserRegistrationDto;
import com.SkyRoute.UserService.Entity.Role;
import com.SkyRoute.UserService.Entity.User;
import com.SkyRoute.UserService.Repository.UserRepository;
import com.SkyRoute.UserService.Service.UserServiceImpl;
import com.SkyRoute.UserService.util.JWTUtil;

import jakarta.mail.MessagingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private JWTUtil jwtUtil;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl userService;

    @BeforeEach
    void init() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtUtil = new TestJWTUtil();
        userService = new UserServiceImpl(userRepository, jwtUtil, passwordEncoder);
    }

    static class TestJWTUtil extends JWTUtil {
        @Override
        public String generateToken(User user) {
            return "test-jwt-token-" + user.getEmail();
        }
    }

    @Test
    void register_success_returnsMessageAndSavesEncodedPassword() throws MessagingException {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Alice");
        dto.setEmail("alice@example.com");
        dto.setPassword("plainPass");
        dto.setRole(Role.CUSTOMER);

        when(passwordEncoder.encode("plainPass")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setUserId(1L);
            return u;
        });

        String result = userService.register(dto);

        assertEquals("User Registered Successfully", result);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("Alice", saved.getName());
        assertEquals("alice@example.com", saved.getEmail());
        assertEquals("ENCODED", saved.getPassword());
        assertEquals(Role.CUSTOMER, saved.getRole());
        verify(passwordEncoder, times(1)).encode("plainPass");
    }

    @Test
    void register_success_withAdminRole() throws MessagingException {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Admin");
        dto.setEmail("admin@example.com");
        dto.setPassword("adminPass");
        dto.setRole(Role.ADMIN);

        when(passwordEncoder.encode("adminPass")).thenReturn("ENCODED_ADMIN");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setUserId(2L);
            return u;
        });

        String result = userService.register(dto);

        assertEquals("User Registered Successfully", result);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals(Role.ADMIN, saved.getRole());
    }

    @Test
    void register_success_withComplexPassword() throws MessagingException {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Bob");
        dto.setEmail("bob@example.com");
        dto.setPassword("P@ssw0rd!#$%^&*()");
        dto.setRole(Role.CUSTOMER);

        when(passwordEncoder.encode("P@ssw0rd!#$%^&*()")).thenReturn("ENCODED_COMPLEX");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setUserId(3L);
            return u;
        });

        String result = userService.register(dto);

        assertEquals("User Registered Successfully", result);
        verify(passwordEncoder).encode("P@ssw0rd!#$%^&*()");
    }

    @Test
    void register_success_withLongName() throws MessagingException {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Alexander Christopher Benjamin Montgomery");
        dto.setEmail("long@example.com");
        dto.setPassword("pass123");
        dto.setRole(Role.CUSTOMER);

        when(passwordEncoder.encode("pass123")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setUserId(4L);
            return u;
        });

        String result = userService.register(dto);

        assertEquals("User Registered Successfully", result);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("Alexander Christopher Benjamin Montgomery", saved.getName());
    }

    @Test
    void register_success_user1() throws MessagingException {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("User0");
        dto.setEmail("user0@example.com");
        dto.setPassword("pass0");
        dto.setRole(Role.CUSTOMER);

        when(passwordEncoder.encode("pass0")).thenReturn("ENCODED0");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setUserId(1L);
            return u;
        });

        String result = userService.register(dto);
        assertEquals("User Registered Successfully", result);
    }

    @Test
    void register_success_user2() throws MessagingException {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("User1");
        dto.setEmail("user1@example.com");
        dto.setPassword("pass1");
        dto.setRole(Role.CUSTOMER);

        when(passwordEncoder.encode("pass1")).thenReturn("ENCODED1");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setUserId(2L);
            return u;
        });

        String result = userService.register(dto);
        assertEquals("User Registered Successfully", result);
    }

    @Test
    void register_success_user3() throws MessagingException {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("User2");
        dto.setEmail("user2@example.com");
        dto.setPassword("pass2");
        dto.setRole(Role.CUSTOMER);

        when(passwordEncoder.encode("pass2")).thenReturn("ENCODED2");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setUserId(3L);
            return u;
        });

        String result = userService.register(dto);
        assertEquals("User Registered Successfully", result);
    }

    @Test
    void login_success_returnsJwtToken() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("bob@example.com");
        login.setPassword("secret");

        User existing = new User();
        existing.setUserId(2L);
        existing.setName("Bob");
        existing.setEmail("bob@example.com");
        existing.setPassword("ENCODED_SECRET");

        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("secret", "ENCODED_SECRET")).thenReturn(true);

        String token = userService.login(login);

        assertNotNull(token);
        assertTrue(token.contains("bob@example.com"));
        verify(userRepository, times(1)).findByEmail("bob@example.com");
        verify(passwordEncoder, times(1)).matches("secret", "ENCODED_SECRET");
    }

    @Test
    void login_success_withComplexPassword() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("complex@example.com");
        login.setPassword("C0mpl3x!@#$%");

        User existing = new User();
        existing.setUserId(5L);
        existing.setEmail("complex@example.com");
        existing.setPassword("ENCODED_COMPLEX");

        when(userRepository.findByEmail("complex@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("C0mpl3x!@#$%", "ENCODED_COMPLEX")).thenReturn(true);

        String token = userService.login(login);

        assertNotNull(token);
        assertTrue(token.contains("complex@example.com"));
    }

    @Test
    void login_success_user1() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("user0@example.com");
        login.setPassword("pass0");

        User existing = new User();
        existing.setUserId(1L);
        existing.setEmail("user0@example.com");
        existing.setPassword("ENCODED0");

        when(userRepository.findByEmail("user0@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("pass0", "ENCODED0")).thenReturn(true);

        String token = userService.login(login);

        assertNotNull(token);
        assertTrue(token.contains("user0@example.com"));
    }

    @Test
    void login_success_user2() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("user1@example.com");
        login.setPassword("pass1");

        User existing = new User();
        existing.setUserId(2L);
        existing.setEmail("user1@example.com");
        existing.setPassword("ENCODED1");

        when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("pass1", "ENCODED1")).thenReturn(true);

        String token = userService.login(login);

        assertNotNull(token);
        assertTrue(token.contains("user1@example.com"));
    }

    @Test
    void login_success_user3() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("user2@example.com");
        login.setPassword("pass2");

        User existing = new User();
        existing.setUserId(3L);
        existing.setEmail("user2@example.com");
        existing.setPassword("ENCODED2");

        when(userRepository.findByEmail("user2@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("pass2", "ENCODED2")).thenReturn(true);

        String token = userService.login(login);

        assertNotNull(token);
        assertTrue(token.contains("user2@example.com"));
    }

    @Test
    void login_emailNotRegistered_throws() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("missing@example.com");
        login.setPassword("whatever");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login(login));
        assertEquals("User Email is not Registered", ex.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_wrongPassword_throws() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("carol@example.com");
        login.setPassword("wrongPass");

        User existing = new User();
        existing.setUserId(3L);
        existing.setName("Carol");
        existing.setEmail("carol@example.com");
        existing.setPassword("ENCODED_CORRECT");

        when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrongPass", "ENCODED_CORRECT")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login(login));
        assertEquals("Invalid Credentials", ex.getMessage());
    }

    @Test
    void login_emptyPassword_throws() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("test@example.com");
        login.setPassword("");

        User existing = new User();
        existing.setUserId(6L);
        existing.setEmail("test@example.com");
        existing.setPassword("ENCODED_PASS");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("", "ENCODED_PASS")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.login(login));
    }

    @Test
    void login_nullPassword_throws() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("test@example.com");
        login.setPassword(null);

        User existing = new User();
        existing.setUserId(7L);
        existing.setEmail("test@example.com");
        existing.setPassword("ENCODED_PASS");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches(null, "ENCODED_PASS")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.login(login));
    }

    @Test
    void login_caseInsensitiveEmail() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("Test@Example.COM");
        login.setPassword("secret");

        User existing = new User();
        existing.setUserId(8L);
        existing.setEmail("test@example.com");
        existing.setPassword("ENCODED_SECRET");

        when(userRepository.findByEmail("Test@Example.COM")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("secret", "ENCODED_SECRET")).thenReturn(true);

        String token = userService.login(login);

        assertNotNull(token);
    }

    @Test
    void login_failedAttempt1() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("secure@example.com");
        login.setPassword("wrongPass");

        User existing = new User();
        existing.setUserId(9L);
        existing.setEmail("secure@example.com");
        existing.setPassword("ENCODED_CORRECT");

        when(userRepository.findByEmail("secure@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrongPass", "ENCODED_CORRECT")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.login(login));
    }

    @Test
    void login_failedAttempt2() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("secure@example.com");
        login.setPassword("wrongPass");

        User existing = new User();
        existing.setUserId(9L);
        existing.setEmail("secure@example.com");
        existing.setPassword("ENCODED_CORRECT");

        when(userRepository.findByEmail("secure@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrongPass", "ENCODED_CORRECT")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.login(login));
    }

    @Test
    void login_failedAttempt3() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("secure@example.com");
        login.setPassword("wrongPass");

        User existing = new User();
        existing.setUserId(9L);
        existing.setEmail("secure@example.com");
        existing.setPassword("ENCODED_CORRECT");

        when(userRepository.findByEmail("secure@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrongPass", "ENCODED_CORRECT")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.login(login));
    }

    @Test
    void register_nullName_savesWithNull() throws MessagingException {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName(null);
        dto.setEmail("null@example.com");
        dto.setPassword("pass");
        dto.setRole(Role.CUSTOMER);

        when(passwordEncoder.encode("pass")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setUserId(10L);
            return u;
        });

        String result = userService.register(dto);

        assertEquals("User Registered Successfully", result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertNull(saved.getName());
    }

    @Test
    void register_emptyEmail_savesWithEmpty() throws MessagingException {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Test");
        dto.setEmail("");
        dto.setPassword("pass");
        dto.setRole(Role.CUSTOMER);

        when(passwordEncoder.encode("pass")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setUserId(11L);
            return u;
        });

        String result = userService.register(dto);

        assertEquals("User Registered Successfully", result);
    }

    @Test
    void register_nullRole_savesWithNull() throws MessagingException {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("NoRole");
        dto.setEmail("norole@example.com");
        dto.setPassword("pass");
        dto.setRole(null);

        when(passwordEncoder.encode("pass")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setUserId(12L);
            return u;
        });

        String result = userService.register(dto);

        assertEquals("User Registered Successfully", result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertNull(saved.getRole());
    }

    @Test
    void register_passwordEncodingFails_throwsException() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Test");
        dto.setEmail("test@example.com");
        dto.setPassword("pass");
        dto.setRole(Role.CUSTOMER);

        when(passwordEncoder.encode("pass")).thenThrow(new RuntimeException("Encoding failed"));

        assertThrows(RuntimeException.class, () -> userService.register(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_repositorySaveFails_throwsException() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Test");
        dto.setEmail("test@example.com");
        dto.setPassword("pass");
        dto.setRole(Role.CUSTOMER);

        when(passwordEncoder.encode("pass")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> userService.register(dto));
    }

    @Test
    void register_then_login_success() throws MessagingException {
        UserRegistrationDto regDto = new UserRegistrationDto();
        regDto.setName("TestUser");
        regDto.setEmail("testuser@example.com");
        regDto.setPassword("testpass");
        regDto.setRole(Role.CUSTOMER);

        when(passwordEncoder.encode("testpass")).thenReturn("ENCODED_TEST");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setUserId(13L);
            return u;
        });

        String regResult = userService.register(regDto);
        assertEquals("User Registered Successfully", regResult);

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("testuser@example.com");
        loginDto.setPassword("testpass");

        User existing = new User();
        existing.setUserId(13L);
        existing.setEmail("testuser@example.com");
        existing.setPassword("ENCODED_TEST");

        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("testpass", "ENCODED_TEST")).thenReturn(true);

        String token = userService.login(loginDto);
        assertNotNull(token);
        assertTrue(token.contains("testuser@example.com"));
    }

    @Test
    void login_jwtTokenGeneration_success() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("jwt@example.com");
        login.setPassword("jwtpass");

        User existing = new User();
        existing.setUserId(14L);
        existing.setEmail("jwt@example.com");
        existing.setPassword("ENCODED_JWT");

        when(userRepository.findByEmail("jwt@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("jwtpass", "ENCODED_JWT")).thenReturn(true);

        String token = userService.login(login);

        assertNotNull(token);
        assertTrue(token.contains("jwt@example.com"));
    }

    @Test
    void register_verifyPasswordEncoding() throws MessagingException {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("EncTest");
        dto.setEmail("enctest@example.com");
        dto.setPassword("plainPassword123");
        dto.setRole(Role.CUSTOMER);

        when(passwordEncoder.encode("plainPassword123")).thenReturn("$2a$10$encryptedHashValue");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setUserId(15L);
            return u;
        });

        userService.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("$2a$10$encryptedHashValue", saved.getPassword());
        assertNotEquals("plainPassword123", saved.getPassword());
    }

    @Test
    void login_passwordMatchingVerification() {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("match@example.com");
        login.setPassword("userPassword");

        User existing = new User();
        existing.setUserId(16L);
        existing.setEmail("match@example.com");
        existing.setPassword("$2a$10$hashedPassword");

        when(userRepository.findByEmail("match@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("userPassword", "$2a$10$hashedPassword")).thenReturn(true);

        String token = userService.login(login);

        assertNotNull(token);
        verify(passwordEncoder).matches("userPassword", "$2a$10$hashedPassword");
    }
}
