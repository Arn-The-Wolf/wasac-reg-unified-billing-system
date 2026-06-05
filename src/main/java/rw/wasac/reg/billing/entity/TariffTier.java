/**
 * JPA entity representing the TariffTier domain model.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tariff_tiers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TariffTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fromUnits;

    @Column(precision = 12, scale = 2)
    private BigDecimal toUnits;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal ratePerUnit;
}
