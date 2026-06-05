/**
 * Service implementation providing TariffConfig business logic.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.wasac.reg.billing.dto.request.*;
import rw.wasac.reg.billing.dto.response.*;
import rw.wasac.reg.billing.entity.*;
import rw.wasac.reg.billing.enums.TariffType;
import rw.wasac.reg.billing.exception.BadRequestException;
import rw.wasac.reg.billing.exception.ResourceNotFoundException;
import rw.wasac.reg.billing.repository.*;
import rw.wasac.reg.billing.service.TariffConfigService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TariffConfigServiceImpl implements TariffConfigService {

    private final TariffRepository tariffRepository;
    private final FixedChargeRepository fixedChargeRepository;
    private final TaxRepository taxRepository;
    private final PenaltyRepository penaltyRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public TariffResponse createTariff(TariffRequest request) {
        validateTariffRequest(request);

        int nextVersion = tariffRepository.findTopByMeterTypeOrderByVersionDesc(request.getMeterType())
                .map(t -> t.getVersion() + 1)
                .orElse(1);

        Tariff tariff = Tariff.builder()
                .name(request.getName())
                .tariffType(request.getTariffType())
                .meterType(request.getMeterType())
                .version(nextVersion)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .flatRate(request.getFlatRate())
                .tiers(new ArrayList<>())
                .build();

        if (request.getTariffType() == TariffType.TIER) {
            for (TariffTierRequest tierReq : request.getTiers()) {
                TariffTier tier = TariffTier.builder()
                        .tariff(tariff)
                        .fromUnits(tierReq.getFromUnits())
                        .toUnits(tierReq.getToUnits())
                        .ratePerUnit(tierReq.getRatePerUnit())
                        .build();
                tariff.getTiers().add(tier);
            }
        }

        return toTariffResponse(tariffRepository.save(tariff));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TariffResponse> getAllTariffs() {
        return tariffRepository.findAllWithTiers().stream().map(this::toTariffResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TariffResponse getTariffById(Long id) {
        return toTariffResponse(tariffRepository.findByIdWithTiers(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff not found with id: " + id)));
    }

    @Override
    @Transactional
    public FixedChargeResponse createFixedCharge(FixedChargeRequest request) {
        FixedCharge charge = modelMapper.map(request, FixedCharge.class);
        return modelMapper.map(fixedChargeRepository.save(charge), FixedChargeResponse.class);
    }

    @Override
    public List<FixedChargeResponse> getAllFixedCharges() {
        return fixedChargeRepository.findAll().stream()
                .map(c -> modelMapper.map(c, FixedChargeResponse.class)).toList();
    }

    @Override
    @Transactional
    public TaxResponse createTax(TaxRequest request) {
        Tax tax = modelMapper.map(request, Tax.class);
        return modelMapper.map(taxRepository.save(tax), TaxResponse.class);
    }

    @Override
    public List<TaxResponse> getAllTaxes() {
        return taxRepository.findAll().stream()
                .map(t -> modelMapper.map(t, TaxResponse.class)).toList();
    }

    @Override
    @Transactional
    public PenaltyResponse createPenalty(PenaltyRequest request) {
        Penalty penalty = modelMapper.map(request, Penalty.class);
        return modelMapper.map(penaltyRepository.save(penalty), PenaltyResponse.class);
    }

    @Override
    public List<PenaltyResponse> getAllPenalties() {
        return penaltyRepository.findAll().stream()
                .map(p -> modelMapper.map(p, PenaltyResponse.class)).toList();
    }

    private void validateTariffRequest(TariffRequest request) {
        if (request.getEffectiveTo() != null && !request.getEffectiveTo().isAfter(request.getEffectiveFrom())) {
            throw new BadRequestException("Effective end date must be after the effective start date");
        }
        if (request.getTariffType() == TariffType.FLAT && request.getFlatRate() == null) {
            throw new BadRequestException("Flat tariff requires a flat rate amount");
        }
        if (request.getTariffType() == TariffType.TIER
                && (request.getTiers() == null || request.getTiers().isEmpty())) {
            throw new BadRequestException("Tier-based tariff requires at least one consumption tier");
        }
    }

    private TariffResponse toTariffResponse(Tariff tariff) {
        List<TariffTierResponse> tiers = tariff.getTiers().stream()
                .map(t -> TariffTierResponse.builder()
                        .id(t.getId())
                        .fromUnits(t.getFromUnits())
                        .toUnits(t.getToUnits())
                        .ratePerUnit(t.getRatePerUnit())
                        .build())
                .toList();

        return TariffResponse.builder()
                .id(tariff.getId())
                .name(tariff.getName())
                .tariffType(tariff.getTariffType())
                .meterType(tariff.getMeterType())
                .version(tariff.getVersion())
                .effectiveFrom(tariff.getEffectiveFrom())
                .effectiveTo(tariff.getEffectiveTo())
                .flatRate(tariff.getFlatRate())
                .tiers(tiers)
                .createdAt(tariff.getCreatedAt())
                .build();
    }
}
