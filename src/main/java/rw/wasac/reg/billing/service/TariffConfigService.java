/**
 * Service contract defining TariffConfigService operations.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.dto.request.*;
import rw.wasac.reg.billing.dto.response.*;

import java.util.List;

public interface TariffConfigService {
    TariffResponse createTariff(TariffRequest request);
    List<TariffResponse> getAllTariffs();
    TariffResponse getTariffById(Long id);

    FixedChargeResponse createFixedCharge(FixedChargeRequest request);
    List<FixedChargeResponse> getAllFixedCharges();

    TaxResponse createTax(TaxRequest request);
    List<TaxResponse> getAllTaxes();

    PenaltyResponse createPenalty(PenaltyRequest request);
    List<PenaltyResponse> getAllPenalties();
}
