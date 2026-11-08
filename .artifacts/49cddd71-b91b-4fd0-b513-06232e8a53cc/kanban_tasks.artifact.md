# 📋 Quadro Kanban - Monexi

Organização completa das tarefas do projeto Monexi, divididas entre **Pronto**, **Em Desenvolvimento** e **A Fazer**.

---

## ✅ Pronto (`Done`)

- [x] **Arquitetura Base & MVVM:** Configuração do Clean Architecture + MVVM com StateFlow.
- [x] **Banco de Local (Room v10):** Definição das entidades (`CategoryEntity`, `PaymentMethodEntity`, `TransactionEntity`, `AccountEntity`, `CreditCardEntity`) e DAOs.
- [x] **Autenticação (Firebase Auth):** Telas de Login e Cadastro com persistência de sessão e proteção de rotas.
- [x] **Sincronização em Nuvem (Cloud Firestore):** Sincronização bidirecional e regras de segurança para transações, categorias e métodos de pagamento.
- [x] **Dashboard na Tela Inicial (`HomeScreen`):**
  - Saldo acumulado e seletor de Mês/Ano.
  - Gráficos de rosca (*Donut Charts*) de Receitas vs Despesas e por Categoria.
  - Carrossel de saldos das Contas Bancárias.
- [x] **Extrato & Lançamentos (`ExtractScreen` & `TransactionScreen`):**
  - Listagem com busca, filtros rápidos e tag "Agendado".
  - Criação e edição de transações com suporte a parcelamento (`1x` a `12x`) e cálculo automático do mês de fatura.
- [x] **Gerenciamento de Configurações (`SettingsScreen`):**
  - CRUD de Categorias (com seletor de cores e ícones do Material Design).
  - CRUD de Métodos de Pagamento.
  - CRUD de Contas Bancárias e Carteiras.
  - CRUD de Cartões de Crédito (Limite, dia de fechamento e vencimento).
  - Telas de itens arquivados e restauração (*Archived Screens*).
- [x] **CI/CD & GitHub Actions:** Automação para compilar e publicar APKs de Release automaticamente ao enviar uma Tag (`v*`).

---

## 🚧 Em Desenvolvimento (`In Progress`)

*(Neste momento, todas as fases principais de Contas, Cartões, Parcelamentos e Sincronização foram concluídas! O quadro está limpo para iniciar a próxima grande épica).*

- [ ] **Validação de Limites de Cartão em Tempo Real:** Aviso visual caso o valor da compra ultrapasse o limite disponível do cartão selecionado.

---

## 🚀 A Fazer / Falta Iniciar (`To Do`)

### Épica: Faturas de Cartão de Crédito
- [ ] **Tela de Faturas Mensais:** Agrupamento dos gastos de cartão por fatura (vencimento e fechamento).
- [ ] **Ação de Pagar Fatura:** Botão para quitar a fatura debitando o saldo de uma conta bancária vinculada.

### Épica: Transferências entre Contas
- [ ] **Tela de Transferência (`TransferScreen`):** Interface para mover saldo entre duas contas do usuário (ex: Itaú -> Nubank).
- [ ] **Repositório de Transferências:** Regra de negócio para registrar a movimentação sem impactar os gráficos globais de receitas e despesas.

### Épica: Transações Recorrentes & Automatização
- [ ] **Gerenciamento de Despesas Fixas:** Configuração de repetição semanal, mensal ou anual.
- [ ] **Servidor/Worker de Lançamento Automático:** WorkManager para gerar lançamentos recorrentes na data prevista.

### Épica: Leitor de Notificações Bancárias
- [ ] **`NotificationListenerService`:** Serviço em background para ler notificações push de compras de bancos e sugerir o cadastro rápido no app.

### Épica: Qualidade & Testes
- [ ] **Testes Unitários:** Ampliação de testes para ViewModels de Contas, Cartões e UseCases de saldo.
- [ ] **Testes de UI (Jetpack Compose):** Testes instrumentados de navegação e formulários principais.
