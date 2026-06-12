# 📊 Guia do Produto: Meus Remedinhos

> Documentação para gestores de produto, analistas, QA e stakeholders sobre funcionalidades, fluxos de usuário e capacidades da aplicação.

---

## 1. Visão Geral do Produto

### O que é Meus Remedinhos?

**Meus Remedinhos** é um aplicativo mobile nativo para **Android** que ajuda pessoas a:

✅ **Organizar** medicações em horários específicos (café, almoço, janta, etc.)  
✅ **Receber lembretes** confiáveis em horários agendados  
✅ **Rastrear** quais medicações já foram tomadas hoje  
✅ **Visualizar** sua rotina de medicamentos na tela inicial (widget)  

### Público-Alvo

| Grupo | Características | Necessidades |
|-------|-----------------|--------------|
| **Idosos** | Menor familiaridade com tech; memória irregular | Interface simples, texto grande, lembretes confiáveis |
| **Pacientes Crônicos** | Múltiplos medicamentos; horários complexos | Organização clara, agendamento preciso |
| **Cuidadores** | Gerenciam medicações de terceiros | Visibilidade simples, controle descomplicado |

### Proposição de Valor

| Aspecto | Diferencial |
|--------|------------|
| **Confiabilidade** | Lembretes funcionam mesmo offline; 100% persisten no dispositivo (sem Cloud síncrono) |
| **Privacidade** | Dados nunca saem do telefone; sem rastreamento externo |
| **Simplicidade** | Apenas 3 ações principais: Adicionar horário, Listar medicações, Marcar como tomado |
| **Acessibilidade** | Interface com alto contraste, texto grande, gestos simples |
| **Sem Custos** | Gratuito e open-source |

---

## 2. Fluxos de Usuário Principais

### 2.1 Primeiro Acesso (Onboarding)

```
┌─────────────────────────────────────────────────┐
│  Tela de Boas-vindas                            │
│  "Bem-vindo ao Meus Remedinhos"                │
│  [Imagem decorativa + Logo]                     │
│  [Botão "Começar Agora"]                        │
└────────────┬────────────────────────────────────┘
             │
             ↓
┌─────────────────────────────────────────────────┐
│  Tela de Instrução                              │
│  "Vamos configurar sua rotina"                  │
│  ✓ Defina seus horários                         │
│  ✓ Adicione os medicamentos                     │
│  ✓ Receba lembretes automáticos                │
│  [Botão "Próximo" ou "Pular"]                   │
└────────────┬────────────────────────────────────┘
             │
             ↓
┌─────────────────────────────────────────────────┐
│  Tela de Permissões                             │
│  "Precisamos de permissão para notificações"    │
│  [Checkbox] Aceitar notificações                │
│  [Botão "Permitir"]                             │
└────────────┬────────────────────────────────────┘
             │
             ↓
┌─────────────────────────────────────────────────┐
│  Dashboard com Defaults                         │
│  🕐 Ao acordar                                  │
│  🍳 Café da manhã                               │
│  ☀️ Manhã                                       │
│  🍽️ Almoço                                      │
│  🌤️ Tarde                                       │
│  🍴 Janta                                        │
│  🌙 Antes de dormir                             │
│  [Botão FAB "+" para adicionar mais]            │
└─────────────────────────────────────────────────┘
```

**Duração:** ~1 minuto  
**Objetivo:** Configuração rápida; usuário já pode receber primeiros lembretes  
**Resultado:** Usuário vê lista de horários padrão e pode personalizar

### 2.2 Visualizar Agenda do Dia (Dashboard)

```
┌──────────────────────────────── Dashboard ──────────────────┐
│ Meus Remedinhos                                    [⚙️ Menu] │
│                                                                 │
│  🕐 08:00 • Café da Manhã                                     │
│  └─ Vitamina D, Ômega-3                                       │
│  ☑️ [checkbox] Marcado como tomado                           │
│                                                                 │
│  🍽️ 12:00 • Almoço                                            │
│  └─ Metiformina 500mg, Losartana 50mg                         │
│  ☐ [checkbox] Não tomado ainda                               │
│                                                                 │
│  🌙 21:00 • Antes de Dormir                                   │
│  └─ Melatonina                                                │
│  ☐ [checkbox] Não tomado ainda                               │
│                                                                 │
│ ┌─────────────────────────────────────────────────────┐     │
│ │ [+] Adicionar Novo Horário                         │     │
│ └─────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────┘
```

**Ações Disponíveis:**
- **Tap no checkbox** → Marca/desmarca evento como tomado (visual feedback: riscado ou normal)
- **Tap no card em "Editar"** → Abre opções: Editar, Deletar, Remover
- **Tap no "+" (FAB)** → Abre modal para adicionar novo horário
- **⚙️ Menu** → Configurações futuras (ainda não implementado)

### 2.3 Adicionar Novo Horário de Medicação

```
┌─────────────────────────────────────────────────┐
│  Novo Horário                                   │
├─────────────────────────────────────────────────┤
│                                                  │
│  Nome/Rótulo:                                   │
│  [_________________________________]            │
│  Ex: Lanche da Tarde                            │
│                                                  │
│  Hora:                                          │
│  [🕐 14:30]  [↑][↓]                            │
│  Toque para selecionar hora                     │
│                                                  │
│  Medicamentos (opcional):                       │
│  [_________________________________]            │
│  Insira nome do medicamento                     │
│  [Adicionar] ← Botão                           │
│                                                  │
│  Medicamentos Adicionados:                      │
│  ✓ Ibuprofeno [X]                               │
│  ✓ Dipirona [X]                                 │
│                                                  │
│  Status:                                        │
│  ⚫ Ativo  [ON/OFF Toggle]  Inativo⚪           │
│                                                  │
│  [Cancelar]  [Criar]                           │
└─────────────────────────────────────────────────┘
```

**Fluxo Detalhado:**

1. **Preencher Nome** (obrigatório)
   - Campo livre, ex: "Café", "Lanche", "Antes de dormir"
   - Validação: não permite vazio, máximo 25 caracteres

2. **Selecionar Hora** (obrigatório)
   - Toque abre time picker nativo do Android
   - Usuário escolhe 14:30, por exemplo
   - Ícone emoji é gerado automaticamente (🕐, 🕑, etc.)

3. **Adicionar Medicamentos** (opcional)
   - Digita nome do remédio ("Ibuprofeno")
   - Toca "Adicionar" → Aparece como chip/tag
   - Pode adicionar múltiplos
   - Pode remover tocando "X" no medicamento

4. ~~**Toggle de Status**~~
   > Esta funcionalidade não passou nos testes de usabilidade e está sendo replanejada.
   - Ativo (cor primária): receberá notificações
   - Inativo (cinza): não receberá notificações

5. **Confirmar**
   - Toca "Criar" → Novo horário aparece no Dashboard
   - Alarme é automaticamente agendado no SO Android

### 2.4 Editar Horário Existente

```
┌─────────────────────────────────────────────────┐
│  Editar Horário                                 │
├─────────────────────────────────────────────────┤
│  Nome: [Café da Manhã_____________]            │
│  Hora: [08:30]                                  │
│  Medicamentos:                                  │
│    ✓ Vitamina D [X]                            │
│    ✓ Ômega-3 [X]                               │
│                                                  │
│  [Cancelar]  [Salvar]  [Deletar]               │
└─────────────────────────────────────────────────┘
```

**Mudanças Possíveis:**
- ✅ Alterar hora → Alarme é automaticamente reagendado
- ✅ Alterar nome
- ✅ Adicionar/remover medicamentos
- ✅ Deletar (remove tudo)

### 2.5 Receber Notificação (Background)

```
[Hora 08:00 chega, app fechado]
         ↓
[Sistema Android dispara AlarmManager]
         ↓
[Telefone vibra, som toca]
         ↓
┌──────────────────────────────────────┐
│ 🔔 Meus Remedinhos                   │
│                                       │
│ 08:00 • Café da Manhã               │
│ Remédios:                            │
│ Vitamina D                           │
│ Ômega-3                              │
│                                       │
│ [Swipe para descartar]  [Tap abrir]  │
└──────────────────────────────────────┘
```

**Características da Notificação:**
- 📢 **Som** + 📳 **Vibração** ativa
- 📝 **Conteúdo:** Hora + Nome do horário + Lista de medicamentos
- ⏰ **Timing:** Dispara exatamente na hora programada (ou minutos aproximados se device em Doze Mode)
- 🔄 **Repetição:** Diariamente (enquanto evento estiver ativo)
- 🎯 **Tap:** Abre app no Dashboard principal

### 2.6 Visualizar Widget (Tela Inicial)

```
[Tela Inicial do Telefone]

┌────────────────────────────────┐
│  Meus Remedinhos       [Meus Remedinhos]
│  ─────────────────────────────  │
│                                  │
│  🕐 08:00 Café da Manhã         │
│     Vitamina D, Ômega-3         │
│     ✓ (strikethrough de texto)  │
│                                  │
│  🍽️ 12:00 Almoço                │
│     Metiformina                 │
│     (texto normal, não tomado)  │
│                                  │
│  🌙 21:00 Antes de Dormir       │
│     Melatonina                  │
│     (texto normal)              │
│                                  │
└────────────────────────────────┘
```

**Características do Widget:**
- ✅ Mostra todos os eventos do dia
- ✅ Indica visualmente quais foram tomados (riscado)
- ✅ Atualiza automaticamente quando DB muda (1-2 segundos)
- ✅ Tap no evento → Abre app
- ✅ Sem ações diretas (não é interativo; click abre app)

---

## 3. Funcionalidades Detalhadas

### 3.1 Marcar Como Tomado (Check-In)

| Ação | Visual Antes | Visual Depois |
|------|-------------|--------------|
| **Usuário toca checkbox OFF** | Texto normal, checkbox vazio | Texto riscado, checkbox marcado |
| **Usuário toca checkbox ON** | Texto riscado, checkbox marcado | Texto normal, checkbox vazio |
| **Reset Diário** | Todos checkboxes em ON ao fim do dia anterior | Todos checkboxes em OFF ao abrir app próximo dia |

**Caso de Uso:** Idosa toma medicação ao acordar e marca no app para não esquecer à noite.

### 3.2 Ativar/Desativar Evento

**Cenário:** Paciente que precisa de medicação só de segunda a sexta.

| Ação | Resultado |
|------|-----------|
| **Evento Ativo** | Notificação dispara todos os dias @ hora programada |
| **Evento Desativado** | Notificação não dispara; medicação omitida; dados preservados |

**Nota:** Esta versão não suporta regras recorrentes (ex: apenas seg-sex). Workaround: usuário desativa manualmente no fim de semana.

### 3.3 Editar Horário Mantendo Medicamentos

**Cenário:** "Preciso tomar o mesmo remédio, mas às 09:00 em vez de 08:00"

✅ **Fluxo Suportado:**
1. Toca evento
2. Muda hora de 08:00 → 09:00
3. Salva → Alarme automaticamente reagendado
4. Medicações (Vitamina D, Ômega-3) ficam intactas

### 3.4 Remover Medicação de um Horário

**Cenário:** "Parei de tomar Ômega-3, mas continuo tomando Vitamina D"

✅ **Fluxo:**
1. Toca evento
2. Localiza "Ômega-3" na lista
3. Toca "X" no chip
4. Salva → Ômega-3 removido, Vitamina D mantida

### 3.5 Reset Diário Automático

**O que acontece:**
- Cada dia à meia-noite (ou quando app abre no dia seguinte), todos os checkboxes retornam a `not taken`
- Medicações e horários mantêm-se iguais
- Dados antigos não são deletados (serão exibidos em relatório futuro)

**Exemplo:**
```
Dia 1 @ 23:59
├─ ✓ 08:00 Café (marcado como tomado)
├─ ✓ 12:00 Almoço (marcado como tomado)
└─ ☐ 21:00 Antes de Dormir (não tomado)

Dia 2 @ 00:00 (ou quando app abre)
├─ ☐ 08:00 Café (RESET para não tomado)
├─ ☐ 12:00 Almoço (RESET para não tomado)
└─ ☐ 21:00 Antes de Dormir (já estava)
```

---

## 4. Requisitos & Limitações

### 4.1 Requisitos do Sistema

| Item | Requisito |
|------|-----------|
| **Plataforma** | Android apenas (iOS não suportado nesta versão) |
| **Versão Android** | Android 14+ (API 34+) |
| **RAM** | Mínimo 2 GB |
| **Armazenamento** | ~50 MB de espaço livre |
| **Conexão** | Não requerida (funciona 100% offline) |
| **Permissões** | Notificações, Agendamento de Alarmes |

### 4.2 Limitações Conhecidas

| Limitação | Status | Solução/Workaround |
|-----------|--------|-------------------|
| **Sem sincronização entre dispositivos** | Por design | Cada telefone tem sua própria cópia de dados |
| **Sem backup automático na nuvem** | Por design (privacidade) | Android permite backup manual via Google One |
| **Sem regras recorrentes (seg-sex)** | ⏳ Roadmap Phase 5 | Desativar manualmente nos fins de semana |
| **Sem medicações "conforme necessário"** | ⏳ Roadmap Phase 5 | Criar evento manual temporário |
| **Sem histórico de conclusão antes da v1.0** | ⏳ Roadmap Phase 4 | Será implementado após lançamento |
| **Widget não tem ações diretas (check inline)** | Por design (complexidade) | Abrir app para marcar como tomado |
| **Suporte apenas a português e inglês** | ⏳ Roadmap Phase 2 | Outro idioma pode ser adicionado após teste |

---

## 5. Casos de Uso & Cenários

### Cenário 1: Idosa com Hipertensão

**Personagem:** Maria, 72 anos, pouca experiência com celular

**Objetivo:** Tomar corretamente 3 medicamentos em horários diferentes

**Jornada:**
1. Filho instala app
2. Onboarding automático com defaults
3. Maria personaliza:
   - 08:00: Losartana (controle pressão)
   - 12:00: Omeprazol (estômago)
   - 20:00: Atorvastatina (colesterol)
4. Todos os dias:
   - 8:00 AM: Notificação dispara
   - Maria marca como "✓ Tomado"
   - Repete para 12:00 e 20:00
5. Semanalmente, filho checa relatório no app

**Sucesso:** Maria não esquece medicações; filho tem visibilidade sem exigir relatórios contínuos.

### Cenário 2: Paciente Crônico Pós-cirúrgico

**Personagem:** João, 45 anos, cirurgia recente com múltiplos medicamentos

**Objetivo:** Gerenciar 6-8 medicações em 4 horários diferentes, com possível ajuste semanal

**Jornada:**
1. João instala e configura manualmente
2. Adiciona eventos:
   - 07:00: Amoxicilina (antibiótico pós-cirurgia)
   - 12:30: Dipirona (dor), Omeprazol (proteção)
   - 18:00: Amoxicilina (2ª dose)
   - 21:00: Loratadina (alergia)
3. Durante semana, marca diariamente
4. Após consulta (semana 2), médico diz para parar Amoxicilina
5. João:
   - Edita eventos 07:00 e 18:00
   - Remove medicação "Amoxicilina"
   - Alarmes são automaticamente re-agendados
6. Monitor de progresso via widget

**Sucesso:** Ajustes são rápidos; João não perde doses; histórico de mudanças registrado.

### Cenário 3: Cuidador de Paciente Débil

**Personagem:** Ana, filha, responsável pelas medicações do pai internado

**Objetivo:** Garantir que pai tome remédios no horário, mesmo dormindo fora

**Jornada:**
1. Ana foi autorizada a acessar dispositivo do pai
2. Ela configura medicações
3. Diariamente (ou quando visita):
   - Recebe notificação @8, @12, @20
   - Verifica widget da tela inicial
   - Marca como "tomado" após administrar
   - Se esquecer, pode marcar depois (sem limite)
4. Pai dorme tranquilo; Ana gestiona medicações remotamente

**Sucesso:** Sem medicações perdidas; responsabilidade compartida; pai sente-se seguro.

---

## 6. Métricas de Sucesso & KPIs

### Engajamento

| Métrica | Target | Rationale |
|---------|--------|-----------|
| **Daily Active Users (DAU)** | >80% | Medicação é diária; esperado uso consistente |
| **Median Session Length** | 2-5 min | Rápido: verificar, marcar, fechar |
| **Feature Usage > Onboarding Days** | 90%+ | Sucesso se usuário continua usando |

### Confiabilidade

| Métrica | Target | Rationale |
|---------|--------|-----------|
| **Alarm Delivery Rate** | 99%+ | Crítico: medicação depende disso |
| **Mean Time to Open App** | <2 sec | Performance esperada |
| **Crash Rate** | <0.1% | Estabilidade crítica para saúde |

### Satisfação

| Métrica | Target | Rationale |
|---------|--------|-----------|
| **NPS (Net Promoter Score)** | >50 | Healthcare apps: alta expectativa |
| **Avg App Rating (Play Store)** | >4.5 ⭐ | Indica qualidade percebida |
| **Retention (30-day)** | >70% | Mantém-se usando após 1 mês |

---

## 7. Roadmap & Evolução

### Fase 1: MVP (Em Andamento ✅)
- [x] Dashboard com listagem de eventos
- [x] Criar/editar/deletar eventos
- [x] Alarmes diários confiáveis
- [x] Notificações locais
- [x] Widget básico na tela inicial
- [x] Reset diário de status
- [ ] Detalhes de medicamento (quantidade, mg, ml, ...)

**Target:** Lançamento Q3 2026

### Fase 2: Estabilidade & UX Polish (Q4 2026)
- [ ] Interface simplificada para idosos
- [ ] Temas: claro/escuro
- [ ] Widget com tamanhos adaptativos
- [ ] Suporte a mais idiomas

### Fase 3: Analytics & History (Q1 2027)
- [ ] Relatório de aderência (90 dias)
- [ ] Histórico de medicações
- [ ] Visualização em gráficos
- [ ] Exportação em PDF

### Fase 4: Advanced Features (Q2-Q3 2027)
- [ ] Regras recorrentes (seg-sex, etc.)
- [ ] Medicações "conforme necessário"
- [ ] Integração com wearables
- [ ] Compartilhamento com cuidadores (read-only)

### Fase 5: Ecosystem (Q4 2027+)
- [ ] Multi-dispositivo com sincronização criptografada
- [ ] Backend cloud (opcional, criptografado)
- [ ] Integração com prontuário eletrônico
- [ ] Comunidade e suporte

---

## 8. Testes & Qualidade

### Cenários de Teste Críticos

| Cenário                       | Critério de Aceitação                                    |
|-------------------------------|----------------------------------------------------------|
| **Adicionar evento 08:00**    | Evento aparece no dashboard; alarme agendado             |
| **Marcar como tomado**        | Checkbox muda visualmente; estado persiste ao fechar app |
| **Editar hora 08:00 → 09:00** | Novo alarme @ 09:00; medicações mantêm                   |
| ~~**Desativar evento**~~      | ~~Notificação não dispara; evento oculto (optional)~~    |
| **Widget atualiza**           | <2 seg após DB mudar                                     |
| **Reset diário**              | Todos checkboxes limpos quando abre app dia seguinte     |
| **Sem internet**              | Todas funções funcionam offline                          |
| **Notificação dispara**       | Som + vibração + push notification @ hora certa          |

### Teste em Dispositivos Reais

**Recomendado:**
- Samsung Galaxy A12 (entrada, comum entre idosos)
- Motorola G10 (entrada, confiável)
- Pixel 9 (topo, referência Google)

**Testes:**
- Deixar app em background 24h
- Desligar/ligar telefone
- Colocar em Doze Mode
- Limpar cache; reiniciar OS

---

## 9. Suporte & Documentação do Usuário

### FAQ (Perguntas Frequentes)

**P: Como faço para adicionar uma medicação?**  
R: Tap no "+" da tela inicial → Preencha horário e nome → Toque "Criar". Pronto!

**P: O aplicativo funciona sem internet?**  
R: Sim! 100% funciona offline. Seus dados ficam no seu telefone.

**P: Posso sincronizar com outro celular?**  
R: Não nesta versão. Cada telefone tem sua própria cópia. Planejamos multi-dispositivo depois.

**P: O que acontece se perder meu telefone?**  
R: Seus dados estarão perdidos (a menos que você tenha feito backup do Android). Recomendamos anotar as medicações.

**P: Posso desativar uma medicação sem deletar?**  
~~R: Sim! Toggle OFF no evento. Ela fica guardada e você pode ativar novamente.~~
R: Ainda não, esta funcionalidade não passou nos testes de usabilidade e está sendo replanejada.

**P: Como funciona o widget?**  
R: Ele mostra uma prévia dos seus medicamentos do dia. Tap abre o app.

### Tutoriais em Vídeo (Planejado)

1. **"Primeiros passos em 60 segundos"** — Onboarding rápido
2. **"Como adicionar medicação"** — Passo a passo
3. **"Configurar widget na tela inicial"** — Setup do widget

---

## 10. Comunicação & Marketing

### Mensagens-Chave

> **"Nunca mais esqueça medicação. Simples, privado, confiável."**

### Público Secundário (Além Usuários)

**Cuidadores/Familiares:**
- "Monitore medicações amadas à distância com o widget"
- "Sem custos, sem assinatura, sem anúncios"

**Profissionais de Saúde:**
- "Recomende a pacientes para aderência"
- "Dados privados; respeitamos LGPD"

---

## 11. Conformidade & Dados

### Privacidade (LGPD - Lei Geral de Proteção de Dados)

✅ **Conformidade:**
- Dados armazenados localmente (no dispositivo)
- Sem transmissão para servidores
- Sem coleta de dados pessoais
- Usuário é dono de seus dados
- Deletar app = deletar dados

### Segurança

✅ **Práticas:**
- API 34+ com recursos modernos de segurança
- Permissões minimizadas
- Sem terceiros com acesso
- Código open-source (auditável)

### Conformidade com Saúde

⚠️ **Nota:** Este é um aplicativo de **"notificações"** (lembrança), não de diagnóstico ou tratamento. Não pretende ser uma solução médica regulada (ex: FDA, ANVISA). Usuários devem consultar médico para prescrição.

---

## 12. Feedback & Iteração

### Como Coletar Feedback

1. **In-App Rating Prompt** (após 7 dias uso)
   - Pergunta simples: "Como você está achando?"
   - 1-5 stars

2. **Survey (Opcional)** — Via link em Settings
   - O que gostou?
   - O que poderia melhorar?

3. **GitHub Issues** — Para tech-savvy users
   - Feature requests
   - Bug reports

### Ciclo de Iteração

```
Feedback → Analysis → Priorização → Development → Testing → Release
   ↑                                                            │
   └────────────────────────────────────────────────────────────┘
```

**Frequência:** Sprint bi-semanal; release mensal (ou conforme demanda crítica)

---

## 13. Conclusão & Visão Futura

### Status Atual
Meus Remedinhos é um **MVP funcional** para Android que resolve o problema crítico de lembretes de medicação confiáveis, offline e simples.

### Visão de Futuro
Evoluir para um **ecossistema de lembretes** que:
- 👥 Conecta pacientes, cuidadores e profissionais
- 📊 Oferece insights sobre aderência à medicação
- 🌍 É acessível globalmente (multi-idioma)
- 🔐 Respeita privacidade total
- 💪 Capacita pessoas a tomar controle de seus lembretes

### Próximos Passos
1. **Lançar MVP** com público beta (Q3 2026)
2. **Coletar feedback** de usuários reais
3. **Priorizar fase 2** baseado em dados
4. **Expandir equipe** conforme demanda

---

## 14. Recursos & Contatos

### Documentação Técnica
- [`DEVELOPER_GUIDE.md`](DEVELOPER_GUIDE.md) — Setup e debugging
- [`FEATURES.md`](FEATURES.md) — Feature inventory
- [`PROJECT_OVERVIEW.md`](PROJECT_OVERVIEW.md) — Visão geral

### Referência React Native
- [`../RN/`](../RN/) — Implementação anterior (PWA → RN)
- [`../RN/.ai/PRD.md`](../../RN/.ai/PRD.md) — PRD original

### Suporte
- **GitHub:** [issues](https://github.com/codexico/med-tracker)  
- **Email:** support@meusremedinhos.local (futura)
- **Forum:** (planejado)


