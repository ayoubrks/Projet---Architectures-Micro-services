import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface ChatMessage {
  sender: 'user' | 'bot';
  text: string;
  time: string;
}

interface Customer {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  createdAt?: string;
}

interface BankAccount {
  id: string;
  balance: number;
  createdAt?: string;
  type: string;
  currency: string;
  customerId: number;
  customer?: Customer;
}

interface AccountOperation {
  id: number;
  operationDate: string;
  amount: number;
  type: 'CREDIT' | 'DEBIT';
  description: string;
}

interface McpServerInfo {
  serverInfo: string;
  tools: { name: string; description: string }[];
}

type Page = 'dashboard' | 'customers' | 'accounts' | 'transfer' | 'mcp';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  private http = inject(HttpClient);
  private readonly gatewayUrl = 'http://localhost:8888';

  page: Page = 'dashboard';
  toast = '';

  // Data
  customers: Customer[] = [];
  accounts: BankAccount[] = [];
  mcpServers: McpServerInfo[] = [];
  selectedAccountId: string | null = null;
  operations: AccountOperation[] = [];
  customerSearch = '';

  // Customer form
  showCustomerForm = false;
  newCustomer: Customer = { firstName: '', lastName: '', email: '', phone: '' };

  // Account operation form
  opAmount = 500;
  opDescription = 'Versement guichet';

  // Transfer form
  transferSource = 'ACC-001';
  transferDest = 'ACC-002';
  transferAmount = 1000;
  transferDesc = 'Virement interne';

  // Floating advisor chat
  chatOpen = false;
  conversationId = 'C1';
  chatInput = '';
  chatLoading = false;
  messages: ChatMessage[] = [
    {
      sender: 'bot',
      text: 'Bonjour, comment puis-je vous aider avec vos clients ou comptes bancaires ?',
      time: this.nowTime()
    }
  ];

  ngOnInit(): void {
    this.refreshAll();
  }

  refreshAll(): void {
    this.loadCustomers();
    this.loadAccounts();
    this.loadMcpTools();
  }

  get filteredCustomers(): Customer[] {
    const q = this.customerSearch.trim().toLowerCase();
    if (!q) return this.customers;
    return this.customers.filter(c =>
      `${c.firstName} ${c.lastName} ${c.email} ${c.phone}`.toLowerCase().includes(q)
    );
  }

  get totalMadBalance(): number {
    return this.accounts.filter(a => a.currency === 'MAD').reduce((s, a) => s + a.balance, 0);
  }

  get totalEurBalance(): number {
    return this.accounts.filter(a => a.currency === 'EUR').reduce((s, a) => s + a.balance, 0);
  }

  get totalMcpTools(): number {
    return this.mcpServers.reduce((s, srv) => s + srv.tools.length, 0);
  }

  get selectedAccount(): BankAccount | undefined {
    return this.accounts.find(a => a.id === this.selectedAccountId);
  }

  loadCustomers(): void {
    this.http.get<Customer[]>(`${this.gatewayUrl}/api/customers`).subscribe({
      next: data => (this.customers = data),
      error: () => {}
    });
  }

  createCustomer(): void {
    if (!this.newCustomer.firstName || !this.newCustomer.lastName || !this.newCustomer.email) return;
    this.http.post<Customer>(`${this.gatewayUrl}/api/customers`, this.newCustomer).subscribe({
      next: () => {
        this.newCustomer = { firstName: '', lastName: '', email: '', phone: '' };
        this.showCustomerForm = false;
        this.notify('Client ajouté avec succès.');
        this.loadCustomers();
      },
      error: () => this.notify('Erreur lors de la création du client.')
    });
  }

  loadAccounts(): void {
    this.http.get<BankAccount[]>(`${this.gatewayUrl}/api/accounts`).subscribe({
      next: data => {
        this.accounts = data;
        if (!this.selectedAccountId && data.length > 0) {
          this.selectAccount(data[0].id);
        }
      },
      error: () => {}
    });
  }

  selectAccount(accountId: string): void {
    this.selectedAccountId = accountId;
    this.http.get<AccountOperation[]>(`${this.gatewayUrl}/api/accounts/${accountId}/operations`).subscribe({
      next: ops => (this.operations = ops),
      error: () => (this.operations = [])
    });
  }

  creditAccount(accountId: string): void {
    if (!this.opAmount || this.opAmount <= 0) return;
    const desc = encodeURIComponent(this.opDescription || 'Crédit');
    this.http.post(`${this.gatewayUrl}/api/accounts/${accountId}/credit?amount=${this.opAmount}&description=${desc}`, {}).subscribe({
      next: () => {
        this.notify(`Crédit de ${this.opAmount} enregistré sur ${accountId}.`);
        this.loadAccounts();
        this.selectAccount(accountId);
      },
      error: () => this.notify('Erreur lors du crédit.')
    });
  }

  debitAccount(accountId: string): void {
    if (!this.opAmount || this.opAmount <= 0) return;
    const desc = encodeURIComponent(this.opDescription || 'Débit');
    this.http.post(`${this.gatewayUrl}/api/accounts/${accountId}/debit?amount=${this.opAmount}&description=${desc}`, {}).subscribe({
      next: () => {
        this.notify(`Débit de ${this.opAmount} enregistré sur ${accountId}.`);
        this.loadAccounts();
        this.selectAccount(accountId);
      },
      error: () => this.notify('Solde insuffisant ou erreur lors du débit.')
    });
  }

  executeTransfer(): void {
    if (!this.transferSource || !this.transferDest || this.transferSource === this.transferDest || this.transferAmount <= 0) {
      this.notify('Vérifiez les comptes et le montant du virement.');
      return;
    }
    const desc = encodeURIComponent(this.transferDesc || 'Virement');
    this.http.post(
      `${this.gatewayUrl}/api/accounts/transfer?sourceAccountId=${this.transferSource}&destinationAccountId=${this.transferDest}&amount=${this.transferAmount}&description=${desc}`,
      {},
      { responseType: 'text' }
    ).subscribe({
      next: () => {
        this.notify(`Virement de ${this.transferAmount} effectué (${this.transferSource} → ${this.transferDest}).`);
        this.loadAccounts();
        if (this.selectedAccountId) this.selectAccount(this.selectedAccountId);
      },
      error: () => this.notify('Échec du virement.')
    });
  }

  loadMcpTools(): void {
    this.http.get<McpServerInfo[]>(`${this.gatewayUrl}/api/mcp/tools`).subscribe({
      next: data => (this.mcpServers = data),
      error: () => {}
    });
  }

  sendPrompt(promptText?: string): void {
    const query = (promptText ?? this.chatInput).trim();
    if (!query || this.chatLoading) return;

    this.messages.push({ sender: 'user', text: query, time: this.nowTime() });
    if (!promptText) this.chatInput = '';
    this.chatLoading = true;

    this.http.post<{ response: string }>(`${this.gatewayUrl}/api/chat`, {
      query,
      conversationId: this.conversationId
    }).subscribe({
      next: res => {
        this.messages.push({
          sender: 'bot',
          text: this.cleanMarkdown(res.response),
          time: this.nowTime()
        });
        this.chatLoading = false;
        this.loadCustomers();
        this.loadAccounts();
        if (this.selectedAccountId) this.selectAccount(this.selectedAccountId);
      },
      error: () => {
        this.messages.push({
          sender: 'bot',
          text: 'Le service assistant est indisponible pour le moment.',
          time: this.nowTime()
        });
        this.chatLoading = false;
      }
    });
  }

  private cleanMarkdown(text: string): string {
    return text.replace(/\*\*/g, '').replace(/\|/g, ' ').trim();
  }

  private notify(msg: string): void {
    this.toast = msg;
    setTimeout(() => {
      if (this.toast === msg) this.toast = '';
    }, 4000);
  }

  private nowTime(): string {
    return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
}
