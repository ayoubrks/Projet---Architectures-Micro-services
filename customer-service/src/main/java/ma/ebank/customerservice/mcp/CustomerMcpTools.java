package ma.ebank.customerservice.mcp;

import lombok.RequiredArgsConstructor;
import ma.ebank.customerservice.entities.Customer;
import ma.ebank.customerservice.services.CustomerService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerMcpTools {

    private final CustomerService customerService;

    @Tool(description = "Get the list of all bank customers")
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @Tool(description = "Find a customer by their unique ID")
    public Customer getCustomerById(@ToolParam(description = "The customer ID") Long id) {
        return customerService.getCustomerById(id);
    }

    @Tool(description = "Find a customer by their email address")
    public Customer getCustomerByEmail(@ToolParam(description = "The customer email address") String email) {
        return customerService.getCustomerByEmail(email);
    }

    @Tool(description = "Search customers by first name, last name, or email keyword")
    public List<Customer> searchCustomers(@ToolParam(description = "Keyword to search in name or email") String keyword) {
        return customerService.searchCustomers(keyword);
    }

    @Tool(description = "Create a new bank customer")
    public Customer createCustomer(
            @ToolParam(description = "Customer first name") String firstName,
            @ToolParam(description = "Customer last name") String lastName,
            @ToolParam(description = "Customer email address") String email,
            @ToolParam(description = "Customer phone number") String phone) {
        Customer customer = Customer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .build();
        return customerService.saveCustomer(customer);
    }
}
