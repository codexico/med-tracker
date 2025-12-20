# Aplicativo "Meus Remedinhos'


## Objetivo Principal: 

Dar uma notificação para o usuário quando ele deve tomar seu medicamento.

### Objetivo Secundário: 

Ter um app para dispositivos Android que ajude o usuário a se lembrar de tomar seus medicamentos através de Notificações locais.

### Público Alvo:    

Pessoas que usam medicamentos e precisam se lembrar de tomar seus medicamentos.

Os usuários do app podem ser idosos, pessoas com deficiência, pessoas que não se lembram de tomar seus medicamentos e pessoas com doenças crônicas ou mesmo pessoas saudáveis que querem se lembrar de tomar seus medicamentos.

Devido ao público diverso, o app deve ser simples de usar e ter uma interface intuitiva e agradável e deve prestar atenção na acessibilidade e usabilidade, não usar fontes pequenas e usar botões grandes e claros.

### Glossário:

- Evento: o conjunto de dados que define um horário de medicamento, com o nome, a hora, uma lista de medicamentos e se está ativo ou não.

- Medicamento: o nome do medicamento que o usuário deve tomar.

- Horário: a hora em que o medicamento deve ser tomado.

- Telas:
    - Tela Principal: tela que mostra a lista de Eventos
    - Tela de Configuração: tela que mostra a lista de Eventos e permite adicionar, editar e remover Eventos


### Funcionalidades:

- [ ]   Notificação de Evento na hora do medicamento
- [ ]   Mostar lista de Eventos contendo o horário e a lista de medicamentos de cada Evento
- [ ]   Adicionar novos Eventos
- [ ]   Editar Eventos
- [ ]   Remover Eventos
- [ ]   Adicionar Medicamento ao Evento
- [ ]   Remover Medicamento do Evento (não é necessário Editar Medicamento)
- [ ]   Usuário marcar o Evento como executado
- [ ]   Mostrar o Evento como executado
- [ ]   Mostrar o Evento como atrasado
- [ ]   Parabenizar o usuário quando ele tomar o medicamento e também ao término do dia

 
### UI

#### Tela Principal: 

Mostrar lista de Eventos do dia com o horário e a lista de medicamentos de cada Evento e status de Executado ou Atrasado.

#### Tela de Configuração: 

Mostrar lista de Eventos do dia com o horário e a lista de medicamentos de cada Evento e opções para adicionar, editar e remover Eventos.

### Requisitos Técnicos

O app deve ser feito em React Native, com o framework Expo.

Se for uma IA, siga as instruções do arquivo AGENTS.md.

O app deve ter um banco de dados local para armazenar os Eventos e Medicamentos.

O app deve ser totalmente local, sem necessidade de internet. O acesso à internet deve ser feito apenas para atualizar o app.

### Outros requisitos

O app deve ser gratuito e open source.

O app não deve armazenar dados do usuário, apenas os Eventos e Medicamentos.


