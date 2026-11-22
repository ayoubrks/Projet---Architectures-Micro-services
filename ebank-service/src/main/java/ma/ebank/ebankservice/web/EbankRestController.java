package ma.ebank.ebankservice.web;

import lombok.RequiredArgsConstructor;
import ma.ebank.ebankservice.entities.AccountOperation;
import ma.ebank.ebankservice.entities.BankAccount;
import ma.ebank.ebankservice.enums.AccountType;
import ma.ebank.ebankservice.services.EbankService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class EbankRestController {

    private final EbankService ebankService;

    @GetMapping
    public List<BankAccount> getAllAccounts() {
        return ebankService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public BankAccount getAccountById(@PathVariable String id) {
        return ebankService.getAccountById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<BankAccount> getAccountsByCustomerId(@PathVariable Long customerId) {
        return ebankService.getAccountsByCustomerId(customerId);
    }

    @GetMapping("/{id}/operations")
    public List<AccountOperation> getAccountOperations(@PathVariable String id) {
        return ebankService.getAccountOperations(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BankAccount createAccount(
            @RequestParam Long customerId,
            @RequestParam(defaultValue = "0") double initialBalance,
            @RequestParam(defaultValue = "CURRENT") AccountType type,
            @RequestParam(defaultValue = "MAD") String currency) {
        return ebankService.createAccount(customerId, initialBalance, type, currency);
    }

    @PostMapping("/{id}/credit")
    public AccountOperation credit(
            @PathVariable String id,
            @RequestParam double amount,
            @RequestParam(defaultValue = "Credit") String description) {
        return ebankService.credit(id, amount, description);
    }

    @PostMapping("/{id}/debit")
    public AccountOperation debit(
            @PathVariable String id,
            @RequestParam double amount,
            @RequestParam(defaultValue = "Debit") String description) {
        return ebankService.debit(id, amount, description);
    }

    @PostMapping("/transfer")
    public String transfer(
            @RequestParam String sourceAccountId,
            @RequestParam String destinationAccountId,
            @RequestParam double amount,
            @RequestParam(defaultValue = "Transfer") String description) {
        return ebankService.transfer(sourceAccountId, destinationAccountId, amount, description);
    }
}
