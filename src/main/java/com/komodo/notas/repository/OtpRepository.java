package com.komodo.notas.repository;

import com.komodo.notas.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Optional<Otp> findByEmailAndTypeOrderByIdDesc(String email, Otp.OtpType type);
}
