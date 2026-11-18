# Monexi 💰

O **Monexi** é um aplicativo Android de controle financeiro pessoal moderno, desenvolvido com Kotlin e Jetpack Compose. O projeto permite registrar receitas, despesas e transferências, gerenciar múltiplas contas bancárias e cartões de crédito, organizar lançamentos por categorias personalizadas, acompanhar saldos acumulados e automatizar pagamentos recorrentes e faturas.

Além de uma ferramenta prática de gestão financeira, o Monexi serve como estudo de caso de **arquitetura Android moderna**, aplicando MVVM, Room Database, Firebase Auth, Firestore, Kotlin Coroutines, Flow/StateFlow, repositories e testes unitários.

---

## 📱 Demonstração em Vídeo

Confira a demonstração prática das funcionalidades do Monexi v1.0.1:

[![Demonstração do Monexi no YouTube Shorts](https://img.youtube.com/vi/7M0kxMYyMic/hqdefault.jpg)](https://youtube.com/shorts/7M0kxMYyMic?is=9g5Ray-7U07idqet)

> 💡 *Clique na imagem acima para assistir ao vídeo de teste do aplicativo no YouTube Shorts.*

---

## 🚀 Novidades da Versão v1.1.2

- 💳 **Redirecionamento Direto para Faturas no Extrato:** Clicar em uma compra de cartão pelo extrato abre diretamente a tela de detalhes e pagamento da fatura (`creditCardBillDetail`) daquele ciclo.
- 📅 **Rolagem Automática Pós-Fechamento:** Compras realizadas após a data de fechamento (`closingDay`) rolam automaticamente para o ciclo e mês da fatura seguinte.
- ✏️ **Edição de Lançamentos na Fatura:** Ícone de edição (lápis) em cada item da fatura com recálculo automático do grupo de parcelas nas faturas futuras.
- 🏠 **Home com Cards Clicáveis:** Clicar nos cards de contas bancárias abre o extrato específico da conta (`bankAccountStatement`); clicar nos cards de cartões abre a tela de faturas do cartão (`creditCardBills`).
- 🛡️ **Validação de Cartão Obrigatório:** Alerta em `AlertDialog` ao tentar selecionar Cartão de Crédito sem possuir cartões cadastrados, oferecendo atalho para criar um novo.
- 🗑️ **Exclusão Flexível de Recorrentes:** Diálogo ao excluir um lançamento recorrente no extrato com as opções *"Apagar apenas esta transação"* ou *"Apagar agendamento completo"*.
- 🎨 **Cores Padronizadas no Extrato:** Todas as despesas pagas em conta e lançamentos de fatura exibidos em vermelho (`#C62828`), reservando a cor azul para compras pendentes do cartão.

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
  - **Filtros rápidos:** *Todos*, *Entradas*, *Contas*, *Cartões* e *Faturas Pagas*.
  - **Agendados:** recorrências futuras com a tag *Agendado*, card de resumo e extrato filtrado por tipo.
  - Cores por situação: verde para receita, vermelho para despesa paga e azul para fatura de cartão e agendados.
  - Formatação de data em português (`dd/MM/yyyy`).
- 💳 **Lançamentos & Transferências (`TransactionScreen`):**
  - Criação e edição de receitas, despesas e **Transferências entre Contas** com validação em tempo real de saldo disponível na conta de origem.
  - **Forma de pagamento x conta:** a lista de contas respeita a forma de pagamento escolhida (e, em receitas, a conta define a forma de pagamento).
  - **Validação de Saldo e Limite:** Travas automáticas para impedir despesas acima do saldo da conta bancária ou acima do limite disponível no cartão de crédito.
  - Compras parceladas no Cartão de Crédito (de 1x até 12x com distribuição automática nas faturas).
- 🏦 **Contas Bancárias e Múltiplas Carteiras (`BankAccountScreen`):**
  - Gestão de contas por tipo (*Conta Corrente*, *Conta Poupança*, *Vale Alimentação/Refeição*, *Investimentos*, *Dinheiro/Carteira*).
  - Conta padrão `"Carteira"` criada automaticamente para cada usuário e sincronizada com a nuvem.
  - **Extrato Específico da Conta (`BankAccountStatementScreen`):** Histórico de transações exclusivo de cada conta com saldo atualizado e botão de edição rápido no header.
- 💳 **Cartões de Crédito & Faturas (`CreditCardScreen`):**
  - Cadastro de cartões com controle de limite, dia de fechamento e dia de vencimento.
  - Visualização de Faturas mensais e listagem de lançamentos da fatura.
  - **Pagamento de Fatura:** Diálogo de confirmação para quitar a fatura debitando o saldo de uma conta bancária e registrando na categoria fixa `"Fatura de Cartão"`.
- 🔄 **Lançamentos Recorrentes & Assinaturas (`RecurringTransactionsScreen`):**
  - Cadastro de despesas e receitas recorrentes mensais (ex: Aluguel, Netflix, Salário).
  - Configuração de dia de repetição e duração (Infinito ou até data final).
  - Geração automática dos lançamentos no extrato e projeção dos próximos meses como *Agendado*.
  - Edição da recorrência e escolha do alcance ao alterar ou excluir (*apenas este* ou *este e os próximos*).
- 🎨 **Categorias com Cores e Ícones Personalizados (`EditCategoriesScreen`):**
  - Tela dedicada para criar e editar categorias.
  - Seletor de cores em paleta de bolinhas e catálogo de ícones do Material Design.
- 📂 **Arquivamento e Desarquivamento:**
  - Telas de itens arquivados (`ArchivedCategoriesScreen`, `ArchivedBankAccountScreen`, `ArchivedCreditCardScreen`) para restaurar ou consultar itens desativados sem apagar histórico.
- 👋 **Onboarding (`OnboardingScreen`):** Configuração inicial da conta logo após o cadastro.
- ☁️ **Sincronização Offline-First (Firebase Firestore):**
  - O Room é a fonte de dados do app; o Firestore é o backup e a ponte entre aparelhos, por usuário autenticado.
  - Escrita sem bloqueio com fila persistente de reenvio (`SyncOutbox` / `RemoteStore`).
  - Sincronização completa e em ordem no login e na abertura (`SyncManager`); nos lançamentos, vence a edição mais recente.
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
UI Compose -> ViewModel -> Repository Interface -> Room Repository -> Room Database
                                                        └-> RemoteStore (fila) -> Cloud Firestore
```

- **Domain:** Isolar modelos do negócio (`Transaction`, `Category`, `Account`, `CreditCard`, `RecurringTransaction`) livres de anotações do banco de dados.
- **Data:** Mappers convertem bidirecionalmente entre `Entity` (Room), documentos do Firestore e modelos de `Domain`.
- **Sync:** `SyncManager` orquestra o download em ordem e o reenvio de pendentes; `RemoteStore` envia ao Firestore sem bloquear a UI.
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
│   │   ├── DefaultData.kt
│   │   └── dao / entity / mapper
│   ├── repository
│   └── sync
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── presentation
│   ├── auth
│   ├── extract
│   ├── home
│   ├── navigation
│   ├── onboarding
│   ├── settings
│   │   ├── accounts
│   │   ├── categories
│   │   ├── credit
│   │   └── recurring
│   └── transaction
└── ui
    ├── components
    └── theme
```

---

## 🚦 Rotas de Navegação (`AppNavigation`)

```text
splash                                  -> Decide entre login, onboarding e home
login                                   -> Tela de Login
forgotPassword                          -> Tela de Recuperação de Senha
register                                -> Tela de Cadastro
onboarding                              -> Configuração inicial pós-cadastro
home                                    -> Resumo financeiro e gráficos
extract                                 -> Histórico de lançamentos e extrato
transaction?id={id}                     -> Criar ou editar lançamento / transferência
recurringEdit?recurringId={id}&...      -> Editar uma recorrência (ou um agendado do extrato)
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
recurring                               -> Lançamentos recorrentes e assinaturas
```

---

## 🧪 Testes

O projeto possui testes unitários para os casos de uso, com repositórios em memória (fakes) como apoio.

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
versionCode: 4
versionName: 1.1.2
```

---

## 🗺️ Roadmap de Próximos Passos

- [ ] **Orçamento e Metas de Gastos por Categoria:** Teto de gastos mensal por categoria com alertas visuais.
- [ ] **Ocultar Saldos (Ícone de Olho) & Biometria:** Proteção de privacidade e segurança ao abrir o app.
- [ ] **Exportação de Relatórios:** Geração de extratos em PDF ou Excel/CSV.
- [ ] **Leitura de Notificações Bancárias:** Leitura inteligente de notificações de compras (via `NotificationListenerService`).
- [ ] **Substituição de `fallbackToDestructiveMigration`:** Migrations reais explícitas no Room.
- [ ] **Logout seguro:** Enviar os pendentes antes de limpar e limpar todos os dados locais do usuário.
- [ ] **Categoria de sistema para Transferências:** Tirar transferências dos totais de receita e despesa.

---

## 👤 Autor

Desenvolvido por **Tiago Ribolli** como projeto de estudo e evolução prática em desenvolvimento Android moderno com Kotlin e Jetpack Compose.
