# 📋 Backlog de Tarefas: Meus Remedinhos

Este documento lista as próximas etapas de desenvolvimento, organizadas por prioridade e detalhadas para execução.

---

## 🟢 Prioridade Alta

### Task 1: Sincronização de Documentação e Lógica de Daily Reset
**Status:** Concluído ✅
- [x] Atualizar `docs/FEATURES.md` e `docs/PROJECT_OVERVIEW.md` para refletir a nova arquitetura de **Cards Expandidos**.
- [x] Corrigir cores hexadecimais nos documentos para baterem com `Color.kt`.
- [x] Implementar lógica de **Daily Reset** no `MainActivity`: ao abrir o app em um novo dia, zerar o status `isTakenToday` de todos os medicamentos.
- [x] Escrever teste unitário/instrumentado para o reset diário.

### Task 2: Acessibilidade e TalkBack (Fase 4)
**Status:** Concluído ✅
- [x] Melhorar a acessibilidade do `EventCard.kt` e `DashboardScreen.kt`.
- [x] Adicionar `contentDescription` semânticos para ícones (emojis de relógio, status, botões de ação).
- [x] Garantir alvos de clique mínimos de 48dp usando `minimumInteractiveComponentSize()`.
- [x] Implementar suporte fluido ao **TalkBack** agrupando informações do card via `semantics(mergeDescendants = true)`.
- [x] Escrever teste de UI (`AccessibilityTest.kt`) validando a presença de descrições semânticas e estados.

---

## 🟡 Prioridade Média

### Task 3: Refinamento e Teste de Sincronização do Widget
**Status:** Concluído ✅
- [x] Aprimorar o `MedicationWidget.kt`.
- [x] Garantir que o estado "riscado" (strikethrough) apareça instantaneamente no widget após ser marcado no app.
- [x] Ao clicar em um horário no widget, o app faz scroll e destaca o card no topo.
- [x] **Contraste**: Fundo bege original (#F0D4BD) restaurado para separar os cards brancos.
- [x] **UX Focus**: Card expandido agora ocupa a tela toda para evitar distrações.
- [x] **Auto-save**: Se houver texto no input de remédios ao clicar em "Salvar", ele é adicionado automaticamente.
- [x] **UI Clean**: FAB é escondido durante a edição para não sobrepor botões.
- [x] Escrever testes instrumentados (`DashboardRefinementTest.kt`) validando essas novas lógicas de UX.

---

## 🔴 Futuro Distante (Fora de Escopo Atual)
- Modo Escuro (Dark Mode).
- Histórico e Logs de Medicamentos.
- Interatividade avançada direto no Widget (Marcar como tomado sem abrir o app).
