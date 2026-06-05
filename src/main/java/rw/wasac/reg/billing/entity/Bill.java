/**
 * JPA entity representing the Bill domain model.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import rw.wasac.reg.billing.enums.BillStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"meter_id", "billing_month", "billing_year"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_reading_id", nullable = false)
    private MeterReading meterReading;

    @Column(nullable = false)
    private Integer billingMonth;

    @Column(nullable = false)
    private Integer billingYear;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal consumption;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tariffAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fixedChargeAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BillStatus status = BillStatus.PENDING;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
