# Funcionalidades e Cenários de Teste - Meus Remedinhos

Este documento lista todas as funcionalidades do app para fins de auditoria e criação de testes futuros.

## 1. Onboarding (Primeiro Acesso)
- [ ] **Boas-vindas**: Exibir tela inicial com logo e breve explicação.
- [ ] **Configuração Inicial**: Se não houver dados, criar eventos padrão (Acordar, Café, Almoço, Jantar, Dormir).
- [ ] **Permissão de Notificação**: Solicitar permissão do sistema ao usuário na primeira tentativa de agendamento ou início.
- [ ] **Redirecionamento**: Se o usuário já completou o onboarding, ir direto para o Dashboard.

## 2. Dashboard (Tela Principal)
- [ ] **Listagem**: Mostrar todos os eventos ativos do dia.
- [ ] **Ordenação**: Eventos devem aparecer ordenados por Horário (Crescente).
- [ ] **Visualização**:
    - Ícone do evento.
    - Horário.
    - Nome do evento.
    - Lista de medicamentos associados.
- [ ] **Estado**:
    - Eventos passados/completos devem ter distinção visual (ex: opacidade, riscado).
    - Checkbox deve refletir o estado "Tomado" do dia atual.
- [ ] **Interação**:
    - Tocar no evento marca/desmarca como concluído.
    - Tocar no ícone ou área específica (se aplicável) abre edição (ou via Configurações).

## 3. Gerenciamento de Eventos (Configurações)
- [ ] **Criar Evento**:
    - Definir Nome (Label).
    - Definir Horário (Time Picker).
    - Escolher Ícone.
    - [Opcional] Adicionar lista inicial de remédios.
- [ ] **Editar Evento**:
    - Alterar Horário -> Deve reagendar notificação.
    - Alterar Nome.
    - Alterar Lista de Remédios.
    - Ativar/Desativar Evento -> Deve criar/cancelar notificação correspondente.
- [ ] **Excluir Evento**:
    - Remover do banco de dados.
    - Cancelar notificação agendada.

## 4. Gerenciamento de Medicamentos
- [ ] **Adicionar Medicamento**: Inserir texto livre em um evento.
- [ ] **Remover Medicamento**: Clicar no "X" ou ícone de remover no chip do medicamento.
- [ ] **Persistência**: Lista de medicamentos deve salvar e persistir após fechar o app.

## 5. Notificações
- [ ] **Agendamento**: Ao criar/editar um evento "Ativo", uma notificação local diária deve ser agendada.
- [ ] **Disparo**: Notificação deve tocar som e vibrar no horário configurado.
- [ ] **Conteúdo**: Título deve ser o horário + nome do evento. Corpo deve listar os medicamentos.
- [ ] **Cancelamento**: Ao desativar ou excluir evento, a notificação não deve ocorrer.

## 6. Persistência de Dados e Estado
- [ ] **Offline**: App deve funcionar 100% sem internet.
- [ ] **Reset Diário**: Eventos marcados como "Tomado" devem resetar para "Não Tomado" quando o dia virar (verificado ao abrir o app no dia seguinte).
- [ ] **SQLite**: Dados devem sobreviver ao fechamento total do app (kill process).

## 7. Internacionalização (i18n)
- [ ] **Detecção**: App deve detectar idioma do sistema (PT vs EN).
- [ ] **Textos**: Interface deve exibir textos no idioma correto.
- [ ] **Logo**: Logo do header deve mudar (Meus Remedinhos vs MedTracker).
