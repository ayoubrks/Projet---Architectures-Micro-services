package ma.ebank.ebankservice.clients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import ma.ebank.ebankservice.model.Customer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CUSTOMER-SERVICE")
public interface CustomerRestClient {

    @GetMapping("/api/customers/{id}")
    @CircuitBreaker(name = "customerService", fallbackMethod = "getDefaultCustomer")
    Customer findCustomerById(@PathVariable("id") Long id);

    default Customer getDefaultCustomer(Long id, Exception exception) {
        return Customer.builder()
                .id(id)
                .firstName("Unknown")
                .lastName("Customer")
                .email("unavailable@ebank.ma")
                .build();
    }
}
