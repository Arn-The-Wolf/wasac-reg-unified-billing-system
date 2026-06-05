/**
 * Service contract defining MeterService operations.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.dto.request.MeterRequest;
import rw.wasac.reg.billing.dto.response.MeterResponse;

import java.util.List;

public interface MeterService {
    MeterResponse create(MeterRequest request);
    MeterResponse update(Long id, MeterRequest request);
    MeterResponse getById(Long id);
    List<MeterResponse> getAll();
    List<MeterResponse> getByCustomerId(Long customerId);
    void delete(Long id);
    MeterResponse activate(Long id);
    MeterResponse deactivate(Long id);
}
