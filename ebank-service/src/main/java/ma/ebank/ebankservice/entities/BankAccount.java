package ma.ebank.ebankservice.entities;

import jakarta.persistence.*;
import lombok.*;
import ma.ebank.ebankservice.enums.AccountType;
import ma.ebank.ebankservice.model.Customer;

import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {
    @Id
    private String id;

    private double balance;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    private String currency;

    @Column(nullable = false)
    private Long customerId;

    @Transient
    private Customer customer;
}
