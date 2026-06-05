package rw.wasac.reg.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.wasac.reg.billing.entity.Otp;
import rw.wasac.reg.billing.enums.OtpPurpose;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmailAndPurposeAndVerifiedFalse(String email, OtpPurpose purpose);
    Optional<Otp> findByEmailAndPurpose(String email, OtpPurpose purpose);
    void deleteByEmailAndPurpose(String email, OtpPurpose purpose);
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
