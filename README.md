# Monexi 💰

O **Monexi** é um aplicativo Android de controle financeiro pessoal moderno, desenvolvido com Kotlin e Jetpack Compose. O projeto permite registrar receitas e despesas, organizar lançamentos por categorias personalizadas e métodos de pagamento, acompanhar o saldo acumulado e visualizar a distribuição dos gastos por meio de gráficos interativos e seletores mensais.

Além de uma ferramenta prática de gestão financeira, o Monexi serve como estudo de caso de **arquitetura Android moderna**, aplicando MVVM, Room Database, Firebase Auth, Firestore, Kotlin Coroutines, Flow/StateFlow, repositories e testes unitários.

---

## 📱 Demonstração em Vídeo

Confira a demonstração prática das funcionalidades do Monexi:

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
  - **Busca em tempo real** por descrição, categoria ou valor formatado (`R$`).
  - **Filtros rápidos:** *Todos*, *Entrada* (Receita) e *Saída* (Despesa).
  - Destaque visual automático para lançamentos futuros com a tag **"Agendado"**.
  - Formatação de data em português (`dd/MM/yyyy`).
- 💳 **Lançamentos (`TransactionScreen`):**
  - Criação e edição de receitas ou despesas com seleção inteligente de categoria e forma de pagamento.
- 🎨 **Categorias com Cores e Ícones Personalizados (`EditCategoriesScreen`):**
  - Tela dedicada para criar e editar categorias.
  - Seletor de cores em paleta de bolinhas e catálogo de ícones do Material Design.
- 📂 **Arquivamento e Desarquivamento:**
  - Telas de itens arquivados (`ArchivedCategoriesScreen` e `ArchivedPaymentMethodScreen`) para restaurar ou consultar categorias/métodos desativados sem apagar o histórico de lançamentos.
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

### Principais Mapeamentos e Fluxos:

```text
HomeViewModel -> CalculateBalanceUseCase
              -> CalculateIncomeUseCase
              -> CalculateExpenseUseCase
```

- **Domain:** Isolar modelos do negócio (`Transaction`, `Category`, `PaymentMethod`) livres de anotações do banco de dados.
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
│   │   ├── dao
│   │   │   ├── CategoryDao.kt
│   │   │   ├── PaymentMethodDao.kt
│   │   │   └── TransactionDao.kt
│   │   ├── entity
│   │   │   ├── CategoryEntity.kt
│   │   │   ├── PaymentMethodEntity.kt
│   │   │   └── TransactionEntity.kt
│   │   └── mapper
│   │       ├── CategoryMapper.kt
│   │       ├── PaymentMethodMapper.kt
│   │       └── TransactionMapper.kt
│   └── repository
│       ├── RoomCategoryRepository.kt
│       ├── RoomPaymentMethodRepository.kt
│       ├── RoomTransactionRepository.kt
│       └── local
│           └── ThemeManager.kt
├── domain
│   ├── model
│   │   ├── Category.kt
│   │   ├── PaymentMethod.kt
│   │   ├── Transaction.kt
│   │   └── TransactionType.kt
│   ├── repository
│   │   ├── CategoryRepository.kt
│   │   ├── PaymentMethodRepository.kt
│   │   └── TransactionRepository.kt
│   └── usecase
│       ├── CalculateBalanceUseCase.kt
│       ├── CalculateExpenseUseCase.kt
│       └── CalculateIncomeUseCase.kt
├── presentation
│   ├── auth
│   │   ├── login
│   │   │   ├── LoginScreen.kt
│   │   │   ├── LoginUiState.kt
│   │   │   └── LoginViewModel.kt
│   │   └── register
│   │       ├── RegisterScreen.kt
│   │       ├── RegisterUiState.kt
│   │       └── RegisterViewModel.kt
│   ├── extract
│   │   ├── ExtractScreen.kt
│   │   ├── ExtractUiState.kt
│   │   └── ExtractViewModel.kt
│   ├── home
│   │   ├── HomeScreen.kt
│   │   ├── HomeUiState.kt
│   │   └── HomeViewModel.kt
│   ├── navigation
│   │   └── AppNavigation.kt
│   ├── settings
│   │   ├── SettingsScreen.kt
│   │   ├── categories
│   │   │   ├── CategoriesScreen.kt
│   │   │   ├── archived/ArchivedCategoriesScreen.kt
│   │   │   └── edit/EditCategoriesScreen.kt
│   │   └── payment
│   │       ├── PaymentMethodScreen.kt
│   │       ├── archived/ArchivedPaymentMethodScreen.kt
│   │       └── edit/EditPaymentMethodScreen.kt
│   └── transaction
│       ├── TransactionScreen.kt
│       ├── TransactionUiState.kt
│       └── TransactionViewModel.kt
└── ui
    └── theme
        ├── CategoryUiMapper.kt
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## 🚦 Rotas de Navegação (`AppNavigation`)

```text
login                       -> Tela de Login
register                    -> Tela de Cadastro
home                        -> Resumo financeiro e gráficos
extract                     -> Histórico de lançamentos e extrato
transaction?id={id}         -> Criar ou editar lançamento
settings                    -> Configurações e tema
categories                  -> Gerenciamento de categorias
archivedCategories          -> Categorias arquivadas
editCategory?id={id}        -> Criar/Editar categoria (cores e ícones)
paymentMethods              -> Gerenciamento de métodos de pagamento
archivedPaymentMethod       -> Métodos de pagamento arquivados
editPaymentMethod?id={id}   -> Criar/Editar método de pagamento
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
versionName: 1.0
```

---

## 🗺️ Roadmap de Próximos Passos

- [ ] **Módulo de Contas Bancárias e Carteiras:** Suporte a múltiplas contas com saldos independentes (ex: Nubank, Itaú, Carteira).
- [ ] **Módulo de Cartões de Crédito:** Gestão de limites, dia de fechamento e fatura mensal.
- [ ] **Transferência entre Contas:** Mover saldo entre contas sem alterar receitas/despesas globais.
- [ ] **Leitura de Notificações Bancárias:** Leitura inteligente de notificações de compras de aplicativos bancários (via `NotificationListenerService`), oferecendo ao usuário a opção de adicionar a transação automaticamente.
- [ ] **Substituição de `fallbackToDestructiveMigration`:** Migrations reais explícitas no Room.
- [ ] **Notificações & Lembretes:** Alerta de contas a vencer/faturas.

---

## 👤 Autor

Desenvolvido por **Tiago Ribolli** como projeto de estudo e evolução prática em desenvolvimento Android moderno com Kotlin e Jetpack Compose.
