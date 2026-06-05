/**
 * Service contract defining MeterReadingService operations.
 */
package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.dto.request.MeterReadingRequest;
import rw.wasac.reg.billing.dto.response.MeterReadingResponse;

import java.math.BigDecimal;
import java.util.List;

public interface MeterReadingService {
    MeterReadingResponse create(MeterReadingRequest request);
    MeterReadingResponse getById(Long id);
    List<MeterReadingResponse> getByMeterId(Long meterId);
    List<MeterReadingResponse> getAll();
    BigDecimal getSuggestedPreviousReading(Long meterId);
    void voidReading(Long id);
}
