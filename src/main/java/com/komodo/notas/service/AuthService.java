package com.komodo.notas.service;

import com.komodo.notas.dto.AuthDto.*;
import com.komodo.notas.model.Otp;
import com.komodo.notas.model.User;
import com.komodo.notas.repository.OtpRepository;
import com.komodo.notas.repository.UserRepository;
import com.komodo.notas.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtils jwtUtils;

    private String generateOtpCode() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

    public AuthResponse register(RegisterRequest request) {
        Optional<User> existing = userRepository.findByEmail(request.getEmail());
        if (existing.isPresent() && existing.get().isActive()) {
            throw new RuntimeException("El correo ya está registrado y activo.");
        }

        User user = existing.orElse(new User());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true); // Activated immediately without OTP
        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, "Registro exitoso");
    }

    public AuthResponse verifyRegistrationOtp(VerifyOtpRequest request) {
        Optional<Otp> optOtp = otpRepository.findByEmailAndTypeOrderByIdDesc(request.getEmail(), Otp.OtpType.REGISTRATION);
        
        if (optOtp.isEmpty() || !optOtp.get().getCode().equals(request.getOtp()) || optOtp.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP inválido o expirado");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setActive(true);
        userRepository.save(user);
        
        // Invalidate OTP (optional delete)
        otpRepository.delete(optOtp.get());

        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, "Registro exitoso");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, "Login exitoso");
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Si el correo existe, se ha enviado un OTP")); // Evitar filtración de correos
        
        String code = generateOtpCode();
        Otp otp = new Otp();
        otp.setEmail(user.getEmail());
        otp.setCode(code);
        otp.setType(Otp.OtpType.PASSWORD_RESET);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(otp);

        emailService.sendOtpEmail(user.getEmail(), code, false);
    }

    public AuthResponse resetPassword(ResetPasswordRequest request) {
        Optional<Otp> optOtp = otpRepository.findByEmailAndTypeOrderByIdDesc(request.getEmail(), Otp.OtpType.PASSWORD_RESET);
        
        if (optOtp.isEmpty() || !optOtp.get().getCode().equals(request.getOtp()) || optOtp.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP inválido o expirado");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        otpRepository.delete(optOtp.get());

        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, "Contraseña actualizada exitosamente");
    }
}
