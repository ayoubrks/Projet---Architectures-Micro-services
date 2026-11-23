# E-Bank — Architecture Microservices, Spring AI & MCP

Plateforme bancaire distribuée construite avec **Spring Boot / Spring Cloud**, intégrant un **Agent AI (Spring AI)** connecté aux microservices métier via le protocole **MCP (Model Context Protocol)**, une interface web **Angular 19**, une **API REST/Swagger**, ainsi que des bots **Telegram** et **Discord**.

---

## Architecture du projet

```text
[ Angular (4200) ]   [ REST / Swagger ]   [ Telegram ]   [ Discord ]
         │                   │                 │              │
         ▼                   └────────┬────────┴──────────────┘
[ gateway-service (8888) ]            │
         │                            ▼
         └──────────────────► [ ebank-bot : Agent AI (8085) ]
                              (Spring AI + ChatMemory + MCP Client)
                                    │                  │
                           MCP (SSE)│                  │MCP (SSE)
                                    ▼                  ▼
                  [ customer-service (8081) ]   [ ebank-service (8082) ]
                      MCP Server (5 outils)        MCP Server (8 outils)
                                    ▲                  │
                                    └── OpenFeign + Resilience4j ──┘
                                              │
                                 [ discovery-service (8761) ]
```

### Modules

| Module | Port | Description |
| :--- | :--- | :--- |
| **`discovery-service`** | `8761` | Serveur **Eureka** pour l'enregistrement et la découverte des services |
| **`gateway-service`** | `8888` | **Spring Cloud Gateway** (routage `lb://...` et configuration CORS) |
| **`customer-service`** | `8081` | Gestion des clients (Spring Data JPA + H2) + **MCP Server** (`customer-mcp-server`) |
| **`ebank-service`** | `8082` | Gestion des comptes & opérations (JPA + H2, **OpenFeign**, **Resilience4j**) + **MCP Server** (`ebank-mcp-server`) |
| **`ebank-bot`** | `8085` | Agent AI **Spring AI** (`EbankAiAgent`), **MCP Client**, API REST/Swagger, bots **Telegram** & **Discord** |
| **`ebank-frontend`** | `4200` | Portail web **Angular 19** (Tableau de bord, Clients, Comptes, Virements, Outils MCP, Assistant E-Bank) |

---

## Stack technique

- **Backend** : Java 21, Spring Boot `3.4.5`, Spring Cloud `2024.0.1` (Eureka, Gateway, OpenFeign, Resilience4j)
- **IA & MCP** : Spring AI `1.0.0` (`spring-ai-starter-model-openai`, `spring-ai-starter-mcp-server-webmvc`, `spring-ai-starter-mcp-client`)
- **Base de données** : H2 (en mémoire, une base par microservice : `customerdb` et `ebankdb`)
- **Bots** : `telegrambots-springboot-longpolling-starter` (`7.10.0`), `JDA` (`5.3.0`)
- **Frontend** : Angular 19, TypeScript, Node.js 22
- **Documentation API** : Springdoc OpenAPI / Swagger UI

---

## Outils exposés via MCP

### `customer-mcp-server` (`customer-service`)
- `getAllCustomers` — Liste tous les clients
- `getCustomerById` — Recherche un client par ID
- `getCustomerByEmail` — Recherche un client par email
- `searchCustomers` — Recherche par mot-clé (nom, prénom, email)
- `createCustomer` — Crée un nouveau client

### `ebank-mcp-server` (`ebank-service`)
- `getAllBankAccounts` — Liste tous les comptes avec leur titulaire
- `getBankAccountById` — Consulte un compte par son ID
- `getBankAccountsByCustomerId` — Liste les comptes d'un client
- `createBankAccount` — Ouvre un nouveau compte (`CURRENT` / `SAVINGS`)
- `creditBankAccount` — Crédite un compte
- `debitBankAccount` — Débite un compte
- `transferBetweenAccounts` — Effectue un virement entre deux comptes
- `getAccountOperations` — Retourne l'historique des opérations d'un compte

---

## Mémoire conversationnelle (`Spring AI`)

`EbankAiAgent` utilise `MessageChatMemoryAdvisor` et `ChatMemory.CONVERSATION_ID` pour isoler l'historique de chaque session/utilisateur :
- **REST / Angular** : `conversationId` passé en paramètre (ex. `C1`)
- **Telegram** : `"tg-" + chatId`
- **Discord** : `"dc-" + channelId`

---

## Configuration

Dans `ebank-bot/src/main/resources/application.yml` ou via variables d'environnement :

```bash
# LLM (OpenAI ou fournisseur compatible comme Groq)
export OPENAI_API_KEY=<VOTRE_CLE_API>

# Bot Telegram (optionnel)
export TELEGRAM_BOT_ENABLED=true
export TELEGRAM_BOT_TOKEN=<VOTRE_TOKEN_TELEGRAM>

# Bot Discord (optionnel - activer MESSAGE CONTENT INTENT sur le portail Discord)
export DISCORD_BOT_ENABLED=true
export DISCORD_BOT_TOKEN=<VOTRE_TOKEN_DISCORD>
```

---

## Compilation et lancement

### 1. Compiler et packager les microservices (Backend)
```bash
mvn clean package
```

### 2. Démarrer les services dans l'ordre
```bash
java -jar discovery-service/target/discovery-service-1.0.0-SNAPSHOT.jar
java -jar customer-service/target/customer-service-1.0.0-SNAPSHOT.jar
java -jar ebank-service/target/ebank-service-1.0.0-SNAPSHOT.jar
java -jar gateway-service/target/gateway-service-1.0.0-SNAPSHOT.jar
java -jar ebank-bot/target/ebank-bot-1.0.0-SNAPSHOT.jar
```

### 3. Démarrer le frontend Angular
```bash
cd ebank-frontend
npm install
npm start
```

---

## URLs & Tests rapides

- **Interface Angular** : http://localhost:4200
- **Eureka Dashboard** : http://localhost:8761
- **Swagger UI (`ebank-bot`)** : http://localhost:8085/swagger-ui.html
- **Swagger UI (`customer-service`)** : http://localhost:8081/swagger-ui.html
- **Swagger UI (`ebank-service`)** : http://localhost:8082/swagger-ui.html
- **Découverte des outils MCP** : `GET http://localhost:8085/api/mcp/tools`
- **Test Chatbot REST** :
  ```bash
  curl -X POST http://localhost:8888/api/chat \
    -H "Content-Type: application/json" \
    -d '{"query":"Liste tous les clients et le solde du compte ACC-001","conversationId":"C1"}'
  ```
