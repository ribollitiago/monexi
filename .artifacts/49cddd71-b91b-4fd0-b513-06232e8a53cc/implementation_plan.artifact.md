# Plano de Implementação: Faturas de Cartão de Crédito

Este plano detalha a arquitetura e os passos para implementar o módulo de **Faturas de Cartão de Crédito (Credit Card Bills)** no Monexi.

## User Review Required

> [!IMPORTANT]
> - As faturas serão calculadas dinamicamente com base nas transações do cartão e no dia de fechamento (`closingDay`) e vencimento (`dueDay`).
> - O pagamento de fatura debitará o valor total da fatura da conta bancária vinculada ao cartão (ou selecionada pelo usuário no momento do pagamento).

## Proposta de Mudanças

### Domain Layer (`domain/model/creditcard/`)
- **[NEW]** `CreditCardBill.kt`: Modelo representando a fatura mensal de um cartão.

### Domain UseCases (`domain/usecase/`)
- **[NEW]** `CalculateCreditCardBillsUseCase.kt`: Lógica para agrupar transações do cartão em ciclos mensais de fatura (considerando o dia de fechamento).
- **[NEW]** `PayCreditCardBillUseCase.kt`: Lógica para quitar uma fatura, criando a transação de pagamento na conta bancária vinculada.

### Data & Repository (`data/repository/` & `domain/repository/`)
- **[MODIFY/NEW]** Métodos no `CreditCardRepository` para gerenciar faturas pagas e sincronizar com o Firestore.

### Presentation Layer (`presentation/settings/credit/bills/`)
- **[NEW]** `CreditCardBillsScreen.kt`: Tela de listagem de faturas do cartão (Abertas, Fechadas, Pagas).
- **[NEW]** `CreditCardBillDetailScreen.kt`: Detalhes da fatura com a lista de gastos do mês e botão **"Pagar Fatura"**.
- **[NEW]** `CreditCardBillsViewModel.kt`: ViewModel gerenciando o estado das faturas.

## Plano de Verificação

### Testes Automatizados
- Testes unitários para `CalculateCreditCardBillsUseCase` validando o agrupamento correto de compras antes e depois do dia de fechamento.
- Testes unitários para `PayCreditCardBillUseCase` validando a criação da transação de pagamento na conta.

### Verificação Manual
- Navegar até a tela de um cartão de crédito, visualizar as faturas geradas e testar o fluxo de pagamento da fatura com débito em conta.
