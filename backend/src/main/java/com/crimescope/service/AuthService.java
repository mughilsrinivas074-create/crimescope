package com.crimescope.service;

import com.crimescope.config.JwtTokenProvider;
import com.crimescope.domain.enums.RoleName;
import com.crimescope.domain.model.Role;
import com.crimescope.domain.model.User;
import com.crimescope.repository.RoleRepository;
import com.crimescope.repository.UserRepository;
import com.crimescope.web.dto.AuthRequest;
import com.crimescope.web.dto.AuthResponse;
import com.crimescope.web.dto.RegisterRequest;
import com.crimescope.web.exception.InvalidDataException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditLogService = auditLogService;
    }

    public AuthResponse login(AuthRequest request, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidDataException("User record not found"));

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        auditLogService.recordAction(user.getId(), user.getUsername(), "USER_LOGIN_SUCCESS",
                "User", user.getId(), "User logged in successfully", ipAddress);

        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getFullName(), roles);
    }

    public AuthResponse register(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidDataException("Username '" + request.getUsername() + "' is already registered.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidDataException("Email '" + request.getEmail() + "' is already registered.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setBadgeNumber(request.getBadgeNumber());
        user.setDepartment(request.getDepartment() != null ? request.getDepartment() : "General Police Division");
        user.setIsActive(true);

        Role defaultRole = roleRepository.findByName(RoleName.ROLE_ANALYST.name())
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ANALYST.name(), "Intelligence Analyst")));
        user.addRole(defaultRole);

        User saved = userRepository.save(user);

        auditLogService.recordAction(saved.getId(), saved.getUsername(), "USER_REGISTERED",
                "User", saved.getId(), "New operator account registered", ipAddress);

        AuthRequest authReq = new AuthRequest(request.getUsername(), request.getPassword());
        return login(authReq, ipAddress);
    }
}
