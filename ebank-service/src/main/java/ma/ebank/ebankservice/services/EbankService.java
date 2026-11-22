package ma.ebank.ebankservice.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ma.ebank.ebankservice.clients.CustomerRestClient;
import ma.ebank.ebankservice.entities.AccountOperation;
import ma.ebank.ebankservice.entities.BankAccount;
import ma.ebank.ebankservice.enums.AccountType;
import ma.ebank.ebankservice.enums.OperationType;
import ma.ebank.ebankservice.repositories.AccountOperationRepository;
import ma.ebank.ebankservice.repositories.BankAccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class EbankService {

    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;
    private final CustomerRestClient customerRestClient;

    public List<BankAccount> getAllAccounts() {
        List<BankAccount> accounts = bankAccountRepository.findAll();
        accounts.forEach(acc -> acc.setCustomer(customerRestClient.findCustomerById(acc.getCustomerId())));
        return accounts;
    }

    public BankAccount getAccountById(String accountId) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Bank account not found: " + accountId));
        account.setCustomer(customerRestClient.findCustomerById(account.getCustomerId()));
        return account;
    }

    public List<BankAccount> getAccountsByCustomerId(Long customerId) {
        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customerId);
        accounts.forEach(acc -> acc.setCustomer(customerRestClient.findCustomerById(acc.getCustomerId())));
        return accounts;
    }

    public BankAccount createAccount(Long customerId, double initialBalance, AccountType type, String currency) {
        BankAccount account = BankAccount.builder()
                .id(UUID.randomUUID().toString())
                .balance(initialBalance)
                .createdAt(LocalDateTime.now())
                .type(type != null ? type : AccountType.CURRENT)
                .currency(currency != null && !currency.isBlank() ? currency : "MAD")
                .customerId(customerId)
                .build();
        BankAccount saved = bankAccountRepository.save(account);
        if (initialBalance > 0) {
            recordOperation(saved, initialBalance, OperationType.CREDIT, "Initial deposit");
        }
        saved.setCustomer(customerRestClient.findCustomerById(customerId));
        return saved;
    }

    public AccountOperation credit(String accountId, double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        BankAccount account = getAccountById(accountId);
        account.setBalance(account.getBalance() + amount);
        bankAccountRepository.save(account);
        return recordOperation(account, amount, OperationType.CREDIT, description != null ? description : "Credit");
    }

    public AccountOperation debit(String accountId, double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        BankAccount account = getAccountById(accountId);
        if (account.getBalance() < amount) {
            throw new IllegalStateException("Insufficient balance for account: " + accountId);
        }
        account.setBalance(account.getBalance() - amount);
        bankAccountRepository.save(account);
        return recordOperation(account, amount, OperationType.DEBIT, description != null ? description : "Debit");
    }

    public String transfer(String sourceAccountId, String destinationAccountId, double amount, String description) {
        String desc = description != null ? description : "Transfer";
        debit(sourceAccountId, amount, desc + " -> " + destinationAccountId);
        credit(destinationAccountId, amount, desc + " <- " + sourceAccountId);
        return String.format("Transferred %.2f from %s to %s", amount, sourceAccountId, destinationAccountId);
    }

    public List<AccountOperation> getAccountOperations(String accountId) {
        return accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId);
    }

    private AccountOperation recordOperation(BankAccount account, double amount, OperationType type, String description) {
        AccountOperation operation = AccountOperation.builder()
                .operationDate(LocalDateTime.now())
                .amount(amount)
                .type(type)
                .description(description)
                .bankAccount(account)
                .build();
        return accountOperationRepository.save(operation);
    }
}
