/**
 * Spring Boot application entry point for the WASAC/REG billing system.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingApplication.class, args);
    }
}
