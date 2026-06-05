/**
 * Service contract defining BillService operations.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.service;

import rw.wasac.reg.billing.dto.request.BillGenerateRequest;
import rw.wasac.reg.billing.dto.response.BillResponse;

import java.util.List;

public interface BillService {
    BillResponse generateBill(BillGenerateRequest request);
    BillResponse getById(Long id);
    BillResponse getByReference(String reference);
    List<BillResponse> getAll();
    List<BillResponse> getByCustomerId(Long customerId);
}
