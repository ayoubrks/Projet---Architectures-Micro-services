package ma.ebank.ebankservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import ma.ebank.ebankservice.enums.OperationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime operationDate;
    private double amount;

    @Enumerated(EnumType.STRING)
    private OperationType type;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    @JsonIgnore
    private BankAccount bankAccount;
}
