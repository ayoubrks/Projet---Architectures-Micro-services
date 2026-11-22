package ma.ebank.ebankservice.mcp;

import lombok.RequiredArgsConstructor;
import ma.ebank.ebankservice.entities.AccountOperation;
import ma.ebank.ebankservice.entities.BankAccount;
import ma.ebank.ebankservice.enums.AccountType;
import ma.ebank.ebankservice.services.EbankService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EbankMcpTools {

    private final EbankService ebankService;

    @Tool(description = "Get all bank accounts with their balances, types, currencies, and associated customer details")
    public List<BankAccount> getAllBankAccounts() {
        return ebankService.getAllAccounts();
    }

    @Tool(description = "Get a bank account by its account ID")
    public BankAccount getBankAccountById(@ToolParam(description = "The bank account ID") String accountId) {
        return ebankService.getAccountById(accountId);
    }

    @Tool(description = "Get all bank accounts belonging to a specific customer ID")
    public List<BankAccount> getBankAccountsByCustomerId(@ToolParam(description = "The customer ID") Long customerId) {
        return ebankService.getAccountsByCustomerId(customerId);
    }

    @Tool(description = "Create a new bank account for a customer")
    public BankAccount createBankAccount(
            @ToolParam(description = "The customer ID") Long customerId,
            @ToolParam(description = "Initial deposit balance") double initialBalance,
            @ToolParam(description = "Account type: CURRENT or SAVINGS") AccountType type,
            @ToolParam(description = "Currency code, e.g., MAD, EUR, USD") String currency) {
        return ebankService.createAccount(customerId, initialBalance, type, currency);
    }

    @Tool(description = "Credit (deposit money into) a bank account")
    public AccountOperation creditBankAccount(
            @ToolParam(description = "The bank account ID") String accountId,
            @ToolParam(description = "Amount to credit") double amount,
            @ToolParam(description = "Operation description") String description) {
        return ebankService.credit(accountId, amount, description);
    }

    @Tool(description = "Debit (withdraw money from) a bank account")
    public AccountOperation debitBankAccount(
            @ToolParam(description = "The bank account ID") String accountId,
            @ToolParam(description = "Amount to debit") double amount,
            @ToolParam(description = "Operation description") String description) {
        return ebankService.debit(accountId, amount, description);
    }

    @Tool(description = "Transfer money from one bank account to another")
    public String transferBetweenAccounts(
            @ToolParam(description = "Source bank account ID") String sourceAccountId,
            @ToolParam(description = "Destination bank account ID") String destinationAccountId,
            @ToolParam(description = "Amount to transfer") double amount,
            @ToolParam(description = "Transfer description") String description) {
        return ebankService.transfer(sourceAccountId, destinationAccountId, amount, description);
    }

    @Tool(description = "Get the operation history (credits and debits) for a bank account")
    public List<AccountOperation> getAccountOperations(@ToolParam(description = "The bank account ID") String accountId) {
        return ebankService.getAccountOperations(accountId);
    }
}
