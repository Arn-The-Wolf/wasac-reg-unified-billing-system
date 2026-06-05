/**
 * Service contract for generating WASAC-branded PDF receipts and reports.
 */
package rw.wasac.reg.billing.service;

public interface PdfReportService {

    /** Customer payment receipt (approved payments only). */
    byte[] generatePaymentReceipt(Long paymentId);

    /** Customer bill statement / receipt. */
    byte[] generateBillReceipt(Long billId);

    /** Admin billing summary for a calendar month. */
    byte[] generateAdminBillingReport(int month, int year);

    /** Inspector meter reading & consumption report for a calendar month. */
    byte[] generateInspectorMeterReport(int month, int year);
}
