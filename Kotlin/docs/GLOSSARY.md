# Glossário do Projeto: Meus Remedinhos

Este documento define os termos e conceitos fundamentais do aplicativo **Meus Remedinhos**, servindo como base de conhecimento comum para IA, Desenvolvedores, Product Managers e Usuários.

## Termos Gerais

*   **Meus Remedinhos**: O nome oficial do aplicativo.
*   **App**: Refere-se ao ecossistema completo do aplicativo Android (interface, lógica de background e banco de dados).
*   **Onboarding**: O fluxo inicial de boas-vindas exibido na primeira vez que o usuário abre o app, apresentando a proposta de valor.

## Entidades de Dados

*   **Evento (ou Horário)**: A unidade principal de organização. Um evento possui um título (ex: "Café da Manhã"), um horário específico e uma lista de medicamentos associados.
*   **Medicamento**: Um item individual cadastrado dentro de um **Evento**. Representa o remédio ou suplemento que deve ser tomado naquele horário.
*   **Status do Evento**:
    *   **Pendente**: O estado inicial de um evento a cada dia. Indica que o usuário ainda não confirmou a ingestão dos medicamentos.
    *   **Tomado**: O estado após o usuário marcar o checkbox no **Card**. O status é visualmente indicado por um estilo "desbotado" (cinza) no título.
*   **Reset Diário**: A lógica automática que limpa todos os status de "Tomado" para "Pendente" no início de cada novo dia (00:00).

## Componentes de Interface (UI)

*   **Dashboard**: A tela principal do aplicativo onde todos os **Eventos** são listados cronologicamente.
*   **Card**: O componente visual que representa um **Evento** na lista do Dashboard.
    *   **Card Compacto**: O estado padrão do Card, mostrando apenas o título, horário, emojis e uma prévia dos medicamentos.
    *   **Card Expandido**: O estado de edição do Card. Ativado ao clicar no ícone de editar, permitindo alterar o nome, horário e gerenciar a lista de medicamentos.
*   **Widget**: O componente que o usuário adiciona à tela inicial (Home Screen) do celular. Ele mostra os próximos eventos sem a necessidade de abrir o app.
*   **Dialog de Criação**: A janela flutuante usada para adicionar um novo **Evento** personalizado à lista.

## Funcionalidades e Background

*   **Alarme / Lembrete**: A notificação do sistema disparada no horário configurado em cada **Evento**.
*   **Deep-link**: O link "mágico" que permite que o usuário, ao clicar em um item dentro do **Widget**, abra o **App** diretamente naquele **Evento** específico, destacando-o visualmente.
*   **Destaque (Highlight)**: O efeito visual (cor de fundo temporária) aplicado a um **Card** quando o app é aberto via **Deep-link**, ajudando o usuário a localizar o item rapidamente.
*   **Seeding (Semeio)**: O processo automático de criar os eventos padrão (Acordar, Almoço, etc.) na primeira vez que o banco de dados é criado.
