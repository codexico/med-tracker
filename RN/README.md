# Meus Remedinhos (MedTracker) 💊

Aplicativo mobile para gerenciamento de medicamentos e rotinas de saúde, focado em simplicidade e notificações locais confiáveis.

## 📱 Funcionalidades

- **Notificações Locais Confiáveis**: Lembretes diários que funcionam mesmo offline.
- **Gerenciamento de Rotina**: Organize seus horários (Café, Almoço, Jantar, etc).
- **Lista de Medicamentos**: Associe múltiplos remédios a cada horário.
- **Offline First**: Seus dados ficam no seu dispositivo. Privacidade total.
- **Interface Simples**: Texto grande, alto contraste e fácil de usar.

## 🛠 Tecnologias

- **React Native** + **Expo**
- **TypeScript**
- **SQLite** (Expo SQLite) para persistência de dados.
- **Expo Notifications** para agendamento local.
- **Expo Router** para navegação.

## 🚀 Como Rodar

1. Instale as dependências:
```bash
npm install
```

2. Inicie o projeto:
```bash
npm start
```

3. Escolha a plataforma:
- Pressione `a` para Android (Emulador ou Dispositivo via USB).
- Pressione `i` para iOS (Simulador).

## 📂 Estrutura do Projeto

- `/app`: Telas e rotas (Expo Router).
- `/components`: Componentes reutilizáveis (MedicationList, etc).
- `/constants`: Estilos, Temas e Configurações estáticas.
- `/services`: Lógica de Banco de Dados e Notificações.
- `/assets`: Imagens e fontes.
- `/.ai`: Documentação do projeto (PRD, Requisitos, Funcionalidades).

## 🤝 Contribuição

Projeto Open Source sob licença MIT. Sinta-se à vontade para abrir Issues ou Pull Requests.

