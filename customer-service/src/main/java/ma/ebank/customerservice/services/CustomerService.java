package ma.ebank.customerservice.services;

import lombok.RequiredArgsConstructor;
import ma.ebank.customerservice.entities.Customer;
import ma.ebank.customerservice.repositories.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
    }

    public List<Customer> searchCustomers(String keyword) {
        String lower = keyword.toLowerCase();
        return customerRepository.findAll().stream()
                .filter(c -> c.getFirstName().toLowerCase().contains(lower)
                        || c.getLastName().toLowerCase().contains(lower)
                        || c.getEmail().toLowerCase().contains(lower))
                .toList();
    }

    public Customer saveCustomer(Customer customer) {
        if (customer.getCreatedAt() == null) {
            customer.setCreatedAt(LocalDateTime.now());
        }
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer updated) {
        Customer existing = getCustomerById(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        return customerRepository.save(existing);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
