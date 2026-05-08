import { getLocales } from 'expo-localization';
import { I18n } from 'i18n-js';

const i18n = new I18n({
    'pt-BR': {
        appName: 'Meus Remedinhos',
        config: 'Configuração',
        configSubtitle: 'Ajuste seus horários e medicamentos.',
        editTime: 'Editar Hora',
        addMedication: 'Adicionar Medicamento',
        newMedication: 'Novo Medicamento',
        medNamePlaceholder: 'Nome do medicamento',
        cancel: 'Cancelar',
        add: 'Adicionar',
        newTime: 'Novo Horário',
        timeNameLabel: 'Nome do Horário (ex: Lanche):',
        timeNamePlaceholder: 'Ex: Lanche da Tarde',
        timeLabel: 'Horário:',
        create: 'Criar',
        addNewTime: 'Adicionar Novo Horário',
        nameRequired: 'Nome é obrigatório!',
        nameRequiredHint: 'É bom colocar um nome para lembrar depois.',
        noEvents: 'Nenhum evento configurado.',
        noEventsToday: 'Sem eventos hoje',
        finishOnboarding: 'Concluir',
        welcome: 'Bem-vindo(a)!',
        initialScreenSubtitle: 'Vamos configurar sua rotina.',
        initialInstructionsStep1: '📅 Defina seus horários de referência (Acordar, Almoço, etc).',
        initialInstructionsStep2: '💊 Adicione os medicamentos em cada horário.',
        initialInstructionsStep3: '⚙️ Não se preocupe! Você pode alterar tudo depois nas configurações.',
        initialInstructionsNote: 'Toque nos horários abaixo para editar ou ativar/desativar.',

        // Default Events
        wakeUp: 'Ao acordar',
        breakfast: 'Café da manhã',
        morning: 'Manhã',
        lunch: 'Almoço',
        afternoon: 'Tarde',
        dinner: 'Janta',
        sleep: 'Antes de dormir',

        // About Tab
        about: 'Sobre',
        appDescription: 'O Meus Remedinhos é o seu assistente pessoal para nunca mais esquecer de tomar seus medicamentos. Cadastre seus horários, adicione seus remédios e deixe o resto com a gente.',
        widgetPromoTitle: 'Adicione nossos Widgets!',
        widgetPromoDesc: 'Acompanhe seus remédios diretamente da sua tela, sem precisar abrir o aplicativo.',
        widgetPromoStep1: '1. Pressione e segure em um espaço vazio na sua tela do celular.',
        widgetPromoStep2: '2. Toque em "Widgets".',
        widgetPromoStep3: '3. Encontre o "Meus Remedinhos", escolha o seu favorito e arraste-o para a tela.',
        githubLink: 'Código Fonte no GitHub',
        version: 'Versão',
    },
    'en-US': {
        appName: 'My Meds',
        config: 'Settings',
        configSubtitle: 'Adjust your schedules and medications.',
        editTime: 'Edit Time',
        addMedication: 'Add Medication',
        newMedication: 'New Medication',
        medNamePlaceholder: 'Medication name',
        cancel: 'Cancel',
        add: 'Add',
        newTime: 'New Schedule',
        timeNameLabel: 'Schedule Name (e.g. Snack):',
        timeNamePlaceholder: 'e.g. Afternoon Snack',
        timeLabel: 'Time:',
        create: 'Create',
        addNewTime: 'Add New Schedule',
        nameRequired: 'Name is required!',
        nameRequiredHint: 'Good to have a name to remember later.',
        noEvents: 'No events configured.',
        noEventsToday: 'No events today',
        finishOnboarding: 'Finish',
        welcome: 'Welcome!',
        initialScreenSubtitle: 'Let\'s set up your schedule.',
        initialInstructionsStep1: '📅 Define your reference times (Wake up, Lunch, etc).',
        initialInstructionsStep2: '💊 Add medications to each time slot.',
        initialInstructionsStep3: '⚙️ Don\'t worry! You can change everything later in settings.',
        initialInstructionsNote: 'Tap on the times below to edit or toggle them.',

        // Default Events
        wakeUp: 'Wake up',
        breakfast: 'Breakfast',
        morning: 'Morning',
        lunch: 'Lunch',
        afternoon: 'Afternoon',
        dinner: 'Dinner',
        sleep: 'Before sleep',

        // About Tab
        about: 'About',
        appDescription: 'My Meds is your personal assistant so you never forget to take your medications again. Set your schedules, add your meds, and leave the rest to us.',
        widgetPromoTitle: 'Add our Widgets!',
        widgetPromoDesc: 'Keep track of your medications directly from your home screen, without opening the app.',
        widgetPromoStep1: '1. Long press on an empty space on your home screen.',
        widgetPromoStep2: '2. Tap on "Widgets".',
        widgetPromoStep3: '3. Find "My Meds", choose your favorite, and drag it to your home screen.',
        githubLink: 'Source Code on GitHub',
        version: 'Version',
    }
});

i18n.locale = getLocales()[0].languageTag ?? 'pt-BR';
i18n.enableFallback = true;
i18n.defaultLocale = 'pt-BR';

export default i18n;
