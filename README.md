# Monexi 💰

O **Monexi** é um aplicativo Android de controle financeiro pessoal moderno, desenvolvido com Kotlin e Jetpack Compose. O projeto permite registrar receitas, despesas e transferências, gerenciar múltiplas contas bancárias e cartões de crédito, organizar lançamentos por categorias personalizadas, acompanhar saldos acumulados e automatizar pagamentos recorrentes e faturas.

Além de uma ferramenta prática de gestão financeira, o Monexi serve como estudo de caso de **arquitetura Android moderna**, aplicando MVVM, Room Database, Firebase Auth, Firestore, Kotlin Coroutines, Flow/StateFlow, repositories e testes unitários.

---

## 📱 Demonstração em Vídeo

Confira a demonstração prática das funcionalidades do Monexi v0.1.0:

[![Demonstração do Monexi no YouTube Shorts](https://img.youtube.com/vi/6fFqFGbdQvc/hqdefault.jpg)](https://youtube.com/shorts/6fFqFGbdQvc)

> 💡 *Clique na imagem acima para assistir ao vídeo de teste do aplicativo no YouTube Shorts.*

---

## 📌 Status do Projeto

Projeto em constante evolução. **Recursos já implementados:**

- 🔐 **Autenticação:** Login, Cadastro de novos usuários e Logout integrado ao **Firebase Auth**.
- 📊 **Dashboard na Tela Inicial (`HomeScreen`):**
  - Saldo geral acumulado.
  - **Seletor de Mês/Ano** (`< Mês Anterior` / `Próximo Mês >`).
  - **Gráfico de Rosca (Donut Chart) de Resumo do Mês:** Comparativo de Receitas vs Despesas desenhado nativamente com `Canvas`.
  - **Gráfico de Rosca por Categoria:** Gráfico dinâmico com proporções, percentuais (`%`) e lista detalhada por categoria com ícones e cores.
- 📜 **Tela de Extrato / Lançamentos (`ExtractScreen`):**
  - Lista completa de lançamentos com **Seletor de Mês**.
  - **Busca em tempo real** por descrição, categoria, valor formatado (`R$`) ou número.
  - **Filtros rápidos:** *Todos*, *Entrada* (Receita) e *Saída* (Despesa).
  - Formatação de data em português (`dd/MM/yyyy`).
- 💳 **Lançamentos & Transferências (`TransactionScreen`):**
  - Criação e edição de receitas, despesas e **Transferências entre Contas** com validação em tempo real de saldo disponível na conta de origem.
  - **Validação de Saldo e Limite:** Travas automáticas para impedir despesas acima do saldo da conta bancária ou acima do limite disponível no cartão de crédito.
  - Compras parceladas no Cartão de Crédito (de 1x até 12x com distribuição automática nas faturas).
- 🏦 **Contas Bancárias e Múltiplas Carteiras (`BankAccountScreen`):**
  - Gestão de contas por tipo (*Conta Corrente*, *Conta Poupança*, *Vale Alimentação/Refeição*, *Investimentos*, *Dinheiro/Carteira*).
  - Conta padrão `"Carteira"` criada automaticamente na instalação.
  - **Extrato Específico da Conta (`BankAccountStatementScreen`):** Histórico de transações exclusivo de cada conta com saldo atualizado e botão de edição rápido no header.
- 💳 **Cartões de Crédito & Faturas (`CreditCardScreen`):**
  - Cadastro de cartões com controle de limite, dia de fechamento e dia de vencimento.
  - Visualização de Faturas mensais e listagem de lançamentos da fatura.
  - **Pagamento de Fatura:** Diálogo de confirmação para quitar a fatura debitando o saldo de uma conta bancária e registrando na categoria fixa `"Fatura de Cartão"`.
- 🔄 **Lançamentos Recorrentes & Assinaturas (`RecurringTransactionsScreen`):**
  - Cadastro de despesas, receitas ou transferências recorrentes mensais (ex: Aluguel, Netflix, Salário).
  - Configuração de dia de repetição e duração (Infinito ou até data final).
  - Geração automática dos lançamentos no extrato e opção avançada de exclusão (*"Apenas as futuras"* vs *"Excluir tudo"*).
- 🎨 **Categorias com Cores e Ícones Personalizados (`EditCategoriesScreen`):**
  - Tela dedicada para criar e editar categorias.
  - Seletor de cores em paleta de bolinhas e catálogo de ícones do Material Design.
- 📂 **Arquivamento e Desarquivamento:**
  - Telas de itens arquivados (`ArchivedCategoriesScreen`, `ArchivedBankAccountScreen`, `ArchivedCreditCardScreen`) para restaurar ou consultar itens desativados sem apagar histórico.
- ☁️ **Sincronização em Nuvem (Firebase Firestore):**
  - Sincronização em background entre o banco local (Room) e o banco na nuvem (Firestore) por usuário autenticado.
- 🌙 **Tema Claro e Escuro:** Gerenciado com `ThemeManager` e persistido via **DataStore Preferences**.

---

## 🛠️ Tecnologias

- **Linguagem:** Kotlin
- **Interface:** Jetpack Compose & Material 3
- **Navegação:** Navigation Compose
- **Arquitetura:** MVVM (Model-View-ViewModel)
- **Persistência Local:** Room Database (KSP) & DataStore Preferences
- **Nuvem & Backend:** Firebase Authentication & Cloud Firestore
- **Assincronismo:** Kotlin Coroutines, Flow e StateFlow
- **Testes:** JUnit 4 & `kotlinx-coroutines-test`

---

## 🏗️ Arquitetura

O projeto segue uma arquitetura inspirada nos princípios do **Clean Architecture** e **MVVM**:

```text
UI Compose -> ViewModel -> Repository Interface -> Room Repository / Firestore -> Room Database / Cloud Firestore
```

- **Domain:** Isolar modelos do negócio (`Transaction`, `Category`, `Account`, `CreditCard`, `RecurringTransaction`) livres de anotações do banco de dados.
- **Data:** Mappers convertem bidirecionalmente entre `Entity` (Room), documentos do Firestore e modelos de `Domain`.
- **Presentation:** ViewModels expõem um único `StateFlow<UiState>` imutável reativo para a UI Compose.

---

## 📁 Estrutura de Pastas

```text
app/src/main/java/com/moduxi/monexi
├── MonexiApplication.kt
├── MainActivity.kt
├── data
│   ├── local
│   │   ├── MonexiDatabase.kt
│   │   └── dao / entity / mapper
│   └── repository
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── presentation
│   ├── auth
│   ├── extract
│   ├── home
│   ├── navigation
│   ├── settings
│   │   ├── accounts
│   │   ├── categories
│   │   ├── credit
│   │   └── recurring
│   └── transaction
└── ui
    └── theme
```

---

## 🚦 Rotas de Navegação (`AppNavigation`)

```text
login                                   -> Tela de Login
register                                -> Tela de Cadastro
home                                    -> Resumo financeiro e gráficos
extract                                 -> Histórico de lançamentos e extrato
transaction?id={id}                     -> Criar ou editar lançamento / transferência
settings                                -> Configurações gerais
categories                              -> Gerenciamento de categorias
archivedCategories                      -> Categorias arquivadas
editCategory?id={id}                    -> Criar/Editar categoria (cores e ícones)
bankAccounts                            -> Gerenciamento de contas bancárias
bankAccountStatement?accountId={id}     -> Extrato específico da conta bancária
archivedBankAccount                     -> Contas bancárias arquivadas
editBankAccount?id={id}                 -> Criar/Editar conta bancária
creditCards                             -> Gerenciamento de cartões de crédito
creditCardBills?cardId={cardId}         -> Faturas do cartão de crédito
creditCardBillDetail?cardId={id}&...    -> Detalhes e pagamento da fatura
archivedCreditCard                      -> Cartões de crédito arquivados
editCreditCard?id={id}                  -> Criar/Editar cartão de crédito
archivedCreditCard                      -> Cartões arquivados
recurring                               -> Lançamentos recorrentes e assinaturas
```

---

## 🧪 Testes

O projeto possui testes unitários para casos de uso, repositórios em memória e ViewModels.

Para executar os testes:

```bash
# No Linux/macOS
./gradlew test

# No Windows
.\gradlew.bat test
```

Para gerar a build Debug:

```bash
# No Linux/macOS
./gradlew assembleDebug

# No Windows
.\gradlew.bat assembleDebug
```

---

## ⚙️ Configurações do Android

```text
applicationId: com.moduxi.monexi
minSdk: 24 (Android 7.0+)
targetSdk: 37
compileSdk: 37
versionCode: 1
versionName: 0.1.0
```

---

## 🗺️ Roadmap de Próximos Passos

- [ ] **Orçamento e Metas de Gastos por Categoria:** Teto de gastos mensal por categoria com alertas visuais.
- [ ] **Ocultar Saldos (Ícone de Olho) & Biometria:** Proteção de privacidade e segurança ao abrir o app.
- [ ] **Exportação de Relatórios:** Geração de extratos em PDF ou Excel/CSV.
- [ ] **Leitura de Notificações Bancárias:** Leitura inteligente de notificações de compras (via `NotificationListenerService`).
- [ ] **Substituição de `fallbackToDestructiveMigration`:** Migrations reais explícitas no Room.

---

## 👤 Autor

Desenvolvido por **Tiago Ribolli** como projeto de estudo e evolução prática em desenvolvimento Android moderno com Kotlin e Jetpack Compose.
