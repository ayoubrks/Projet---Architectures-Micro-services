package ma.ebank.ebankservice;

import ma.ebank.ebankservice.entities.AccountOperation;
import ma.ebank.ebankservice.entities.BankAccount;
import ma.ebank.ebankservice.enums.AccountType;
import ma.ebank.ebankservice.enums.OperationType;
import ma.ebank.ebankservice.mcp.EbankMcpTools;
import ma.ebank.ebankservice.repositories.AccountOperationRepository;
import ma.ebank.ebankservice.repositories.BankAccountRepository;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class EbankServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbankServiceApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider ebankToolCallbackProvider(EbankMcpTools ebankMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(ebankMcpTools)
                .build();
    }

    @Bean
    CommandLineRunner seedBankAccounts(
            BankAccountRepository bankAccountRepository,
            AccountOperationRepository accountOperationRepository) {
        return args -> {
            if (bankAccountRepository.count() == 0) {
                BankAccount acc1 = bankAccountRepository.save(BankAccount.builder()
                        .id("ACC-001")
                        .balance(25000.0)
                        .createdAt(LocalDateTime.now())
                        .type(AccountType.CURRENT)
                        .currency("MAD")
                        .customerId(1L)
                        .build());

                BankAccount acc2 = bankAccountRepository.save(BankAccount.builder()
                        .id("ACC-002")
                        .balance(80000.0)
                        .createdAt(LocalDateTime.now())
                        .type(AccountType.SAVINGS)
                        .currency("MAD")
                        .customerId(1L)
                        .build());

                BankAccount acc3 = bankAccountRepository.save(BankAccount.builder()
                        .id("ACC-003")
                        .balance(14500.0)
                        .createdAt(LocalDateTime.now())
                        .type(AccountType.CURRENT)
                        .currency("MAD")
                        .customerId(2L)
                        .build());

                BankAccount acc4 = bankAccountRepository.save(BankAccount.builder()
                        .id("ACC-004")
                        .balance(42000.0)
                        .createdAt(LocalDateTime.now())
                        .type(AccountType.SAVINGS)
                        .currency("EUR")
                        .customerId(3L)
                        .build());

                accountOperationRepository.saveAll(List.of(
                        AccountOperation.builder()
                                .operationDate(LocalDateTime.now().minusDays(2))
                                .amount(30000.0)
                                .type(OperationType.CREDIT)
                                .description("Initial salary deposit")
                                .bankAccount(acc1)
                                .build(),
                        AccountOperation.builder()
                                .operationDate(LocalDateTime.now().minusDays(1))
                                .amount(5000.0)
                                .type(OperationType.DEBIT)
                                .description("Rent payment")
                                .bankAccount(acc1)
                                .build(),
                        AccountOperation.builder()
                                .operationDate(LocalDateTime.now().minusDays(3))
                                .amount(80000.0)
                                .type(OperationType.CREDIT)
                                .description("Savings transfer")
                                .bankAccount(acc2)
                                .build(),
                        AccountOperation.builder()
                                .operationDate(LocalDateTime.now().minusDays(1))
                                .amount(14500.0)
                                .type(OperationType.CREDIT)
                                .description("Account opening deposit")
                                .bankAccount(acc3)
                                .build(),
                        AccountOperation.builder()
                                .operationDate(LocalDateTime.now().minusDays(1))
                                .amount(42000.0)
                                .type(OperationType.CREDIT)
                                .description("EUR savings deposit")
                                .bankAccount(acc4)
                                .build()
                ));
            }
        };
    }
}
