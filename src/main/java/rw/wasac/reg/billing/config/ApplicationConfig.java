/**
 * Spring configuration component: ApplicationConfig.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class ApplicationConfig {
}
