package ma.ebank.customerservice;

import ma.ebank.customerservice.entities.Customer;
import ma.ebank.customerservice.mcp.CustomerMcpTools;
import ma.ebank.customerservice.repositories.CustomerRepository;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider customerToolCallbackProvider(CustomerMcpTools customerMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(customerMcpTools)
                .build();
    }

    @Bean
    CommandLineRunner seedCustomers(CustomerRepository customerRepository) {
        return args -> {
            if (customerRepository.count() == 0) {
                customerRepository.saveAll(List.of(
                        Customer.builder()
                                .firstName("Mohamed")
                                .lastName("Youssfi")
                                .email("mohamed@ebank.ma")
                                .phone("+212600000001")
                                .createdAt(LocalDateTime.now())
                                .build(),
                        Customer.builder()
                                .firstName("Imad")
                                .lastName("El Amrani")
                                .email("imad@ebank.ma")
                                .phone("+212600000002")
                                .createdAt(LocalDateTime.now())
                                .build(),
                        Customer.builder()
                                .firstName("Salma")
                                .lastName("Bennani")
                                .email("salma@ebank.ma")
                                .phone("+212600000003")
                                .createdAt(LocalDateTime.now())
                                .build()
                ));
            }
        };
    }
}
