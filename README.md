# Monexi

Monexi e um aplicativo Android de controle financeiro pessoal, desenvolvido em Kotlin com Jetpack Compose. O objetivo do projeto e registrar receitas e despesas, organizar lancamentos por categoria e metodo de pagamento, acompanhar o saldo e evoluir gradualmente para uma experiencia mais completa com dashboard, graficos, autenticacao e sincronizacao.

O projeto tambem serve como estudo pratico de arquitetura Android moderna, usando MVVM, Room, Flow, StateFlow, repositories, use cases e testes unitarios.

## Status do projeto

Projeto em desenvolvimento.

Ja implementado:

- Cadastro local de transacoes financeiras.
- Edicao e exclusao de transacoes.
- Listagem de transacoes na tela inicial.
- Resumo financeiro com saldo, receitas e despesas.
- Categorias separadas por tipo de transacao: receita ou despesa.
- Metodos de pagamento configuraveis.
- Categorias e metodos de pagamento padrao via seed do banco local.
- Persistencia local com Room.
- Tema claro/escuro com DataStore.
- Navegacao com Jetpack Navigation Compose.
- Testes unitarios para use cases, categorias, metodos de pagamento e transacoes.

Ainda planejado:

- Home com dashboard mais completo e graficos.
- Tela separada para resumo/listagem de lancamentos.
- Firebase Authentication.
- Sincronizacao por usuario.
- Migrations reais do Room.
- Melhorias visuais e refinamento de UX.

## Tecnologias

- Kotlin
- Android SDK
- Jetpack Compose
- Material 3
- Navigation Compose
- ViewModel
- Kotlin Coroutines
- Flow e StateFlow
- Room
- KSP
- DataStore Preferences
- JUnit 4
- kotlinx-coroutines-test

## Arquitetura

O projeto segue uma organizacao inspirada em MVVM com separacao entre dominio, dados e apresentacao.

Fluxo principal:

```text
UI Compose -> ViewModel -> Repository Interface -> Room Repository -> DAO -> Room Database
```

Para calculos de resumo financeiro, a Home usa use cases de dominio:

```text
HomeViewModel -> CalculateBalanceUseCase
              -> CalculateIncomeUseCase
              -> CalculateExpenseUseCase
```

Os models de dominio nao possuem anotacoes do Room. As entidades do banco ficam separadas na camada `data/local/entity`, e os mappers convertem entre `Entity` e model de dominio.

## Estrutura de pastas

Estrutura principal do modulo `app`:

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
│   │   │   ├── TransactionEntity.kt
│   │   │   └── TransactionWithDetails.kt
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
│   ├── home
│   │   ├── HomeScreen.kt
│   │   ├── HomeUiState.kt
│   │   └── HomeViewModel.kt
│   ├── navigation
│   │   └── AppNavigation.kt
│   ├── settings
│   │   ├── SettingsScreen.kt
│   │   ├── SettingsUiState.kt
│   │   ├── SettingsViewModel.kt
│   │   ├── categories
│   │   │   ├── CategoriesScreen.kt
│   │   │   ├── CategoriesUiState.kt
│   │   │   └── CategoriesViewModel.kt
│   │   └── payment
│   │       ├── PaymentMethodScreen.kt
│   │       ├── PaymentMethodUiState.kt
│   │       └── PaymentMethodViewModel.kt
│   └── transaction
│       ├── TransactionScreen.kt
│       ├── TransactionUiState.kt
│       └── TransactionViewModel.kt
└── ui
    └── theme
```

Estrutura de testes:

```text
app/src/test/java/com/moduxi/monexi
├── MainDispatcherRule.kt
├── data/repository
│   ├── FakeCategoryRepository.kt
│   ├── FakePaymentMethodRepository.kt
│   └── FakeTransactionRepository.kt
├── domain/usecase
│   ├── CalculateBalanceUseCaseTest.kt
│   ├── CalculateExpenseUseCaseTest.kt
│   └── CalculateIncomeUseCaseTest.kt
└── presentation
    ├── settings
    │   ├── categories/CategoriesViewModelTest.kt
    │   └── payment/PaymentMethodViewModelTest.kt
    └── transaction/TransactionViewModelTest.kt
```

## Camadas do projeto

### Domain

Contem as regras e modelos centrais do app.

Principais models:

- `Transaction`: representa um lancamento financeiro.
- `TransactionType`: define `INCOME` ou `EXPENSE`.
- `Category`: categoria vinculada a um tipo de transacao.
- `PaymentMethod`: metodo de pagamento usado no lancamento.

Use cases:

- `CalculateIncomeUseCase`: soma receitas.
- `CalculateExpenseUseCase`: soma despesas.
- `CalculateBalanceUseCase`: calcula saldo final.

Interfaces de repository:

- `TransactionRepository`
- `CategoryRepository`
- `PaymentMethodRepository`

### Data

Contem a persistencia local com Room e as implementacoes concretas dos repositories.

O banco principal e `MonexiDatabase`, configurado com as entidades:

- `CategoryEntity`
- `PaymentMethodEntity`
- `TransactionEntity`

No primeiro uso do banco, sao criadas categorias e metodos de pagamento padrao.

Categorias padrao:

- Alimentacao
- Transporte
- Casa
- Saude
- Salario
- Freelance
- Investimentos

Metodos de pagamento padrao:

- Dinheiro
- Cartao de Credito
- Cartao de Debito
- Pix

Observacao: durante o desenvolvimento, o banco usa `fallbackToDestructiveMigration(true)`. Isso facilita alteracoes no schema, mas apaga dados locais quando a versao do banco muda sem migration real.

### Presentation

Contem telas Compose, ViewModels e UiStates.

Telas principais:

- `HomeScreen`: resumo financeiro e lista de ultimas transacoes.
- `TransactionScreen`: criacao, edicao e exclusao de lancamentos.
- `SettingsScreen`: acesso as configuracoes.
- `CategoriesScreen`: gerenciamento de categorias.
- `PaymentMethodScreen`: gerenciamento de metodos de pagamento.

Navegacao principal:

```text
home
transaction?id={id}
settings
categories
paymentMethods
```

A rota de transacao aceita um `id` opcional. Quando o `id` e informado, o `TransactionViewModel` carrega a transacao correspondente e entra em modo de edicao.

## Funcionalidades

### Transacoes

O app permite:

- Criar transacoes de receita ou despesa.
- Informar descricao, valor, data, categoria e metodo de pagamento.
- Editar transacoes existentes.
- Excluir transacoes.
- Filtrar categorias automaticamente de acordo com o tipo da transacao.

Exemplo de regra aplicada:

```text
Transacao de despesa -> mostra apenas categorias EXPENSE
Transacao de receita -> mostra apenas categorias INCOME
```

### Categorias

Categorias possuem:

- Nome
- Tipo: `INCOME` ou `EXPENSE`
- Flag `isDefault`

Regras principais:

- Nao permite categoria sem nome.
- Nao permite categoria duplicada para o mesmo tipo.
- Permite mesmo nome em tipos diferentes.
- Categorias padrao nao devem ser editadas ou excluidas.

### Metodos de pagamento

Metodos de pagamento possuem:

- Nome
- Flag `isDefault`

Regras principais:

- Nao permite metodo sem nome.
- Nao permite metodo duplicado.
- Metodos padrao nao devem ser editados ou excluidos.

### Resumo financeiro

A Home calcula:

- Saldo atual
- Total de receitas
- Total de despesas
- Lista de transacoes

Os calculos ficam em use cases para manter a regra de negocio fora da UI.

### Tema

O app possui gerenciamento de tema com `ThemeManager`, usando DataStore Preferences para persistencia local da preferencia.

## Testes

O projeto possui testes unitarios para regras de negocio e ViewModels.

Principais areas cobertas:

- Calculo de receitas, despesas e saldo.
- Cadastro, edicao, exclusao e validacoes de categorias.
- Cadastro, edicao, exclusao e validacoes de metodos de pagamento.
- Criacao, validacao, edicao, exclusao e carregamento de transacoes.

Os testes de ViewModel usam repositories fake em memoria, evitando depender diretamente do Room em testes unitarios.

Para rodar os testes:

```bash
./gradlew test
```

No Windows:

```powershell
.\gradlew.bat test
```

Para gerar o build debug:

```bash
./gradlew assembleDebug
```

No Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Como executar o projeto

1. Clone o repositorio:

```bash
git clone <url-do-repositorio>
```

2. Abra o projeto no Android Studio.

3. Aguarde a sincronizacao do Gradle.

4. Execute o app em um emulador ou dispositivo fisico.

Requisitos recomendados:

- Android Studio recente.
- JDK compativel com o Android Gradle Plugin usado no projeto.
- Emulador ou dispositivo com Android 7.0 ou superior, pois o `minSdk` e 24.

## Configuracao Android

Configuracao atual do modulo `app`:

```text
applicationId: com.moduxi.monexi
minSdk: 24
targetSdk: 37
compileSdk: 37
versionCode: 1
versionName: 1.0
```

## Decisoes tecnicas

- Models de dominio nao dependem do Room.
- DAOs e entidades ficam isolados em `data/local`.
- Repositories do dominio sao interfaces.
- Implementacoes concretas usam Room.
- ViewModels expõem `StateFlow` com `UiState` imutavel.
- Testes unitarios usam fakes para validar comportamento sem banco real.
- Categorias sao filtradas pelo tipo da transacao.
- Transacoes salvam `categoryId` e `paymentMethodId` no banco, mas usam objetos completos no dominio.

## Roadmap

Proximos passos planejados:

- Finalizar refinamentos dos testes de transacao.
- Criar tela dedicada para lista/resumo de lancamentos.
- Transformar a Home em dashboard com graficos e indicadores.
- Melhorar componentes visuais do formulario de transacao.
- Implementar Firebase Authentication.
- Adicionar vinculo dos dados a um usuario autenticado.
- Planejar sincronizacao local/remota.
- Substituir `fallbackToDestructiveMigration` por migrations reais do Room.
- Adicionar testes instrumentados para fluxo de UI.

## Autor

Projeto desenvolvido por Ribol como estudo e evolucao pratica em desenvolvimento Android moderno com Kotlin.
