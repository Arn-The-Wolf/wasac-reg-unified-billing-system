package rw.wasac.reg.billing.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WasacPdfBuilderTest {

    @Test
    void build_generatesValidPdfWithWasacBranding() {
        byte[] pdf = WasacPdfBuilder.build("Test Report", document -> {
            WasacPdfBuilder.addSectionTitle(document, "Summary");
            WasacPdfBuilder.addKeyValueRow(document, "Field", "Value");
            WasacPdfBuilder.addHighlightBox(document, "WASAC Utility Billing");
        });

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }
}
