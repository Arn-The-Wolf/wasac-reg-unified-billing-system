/**
 * Spring configuration component: ModelMapperConfig.
 *
 * @author WASAC/REG Billing System
 */
package rw.wasac.reg.billing.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rw.wasac.reg.billing.dto.request.CustomerRequest;
import rw.wasac.reg.billing.entity.Customer;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        mapper.createTypeMap(CustomerRequest.class, Customer.class)
                .addMappings(m -> m.skip(Customer::setId));
        return mapper;
    }
}
