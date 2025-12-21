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

        // Default Events
        wakeUp: 'Ao acordar',
        breakfast: 'Café da manhã',
        morning: 'Manhã',
        lunch: 'Almoço',
        afternoon: 'Tarde',
        dinner: 'Janta',
        sleep: 'Antes de dormir',
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

        // Default Events
        wakeUp: 'Wake up',
        breakfast: 'Breakfast',
        morning: 'Morning',
        lunch: 'Lunch',
        afternoon: 'Afternoon',
        dinner: 'Dinner',
        sleep: 'Before sleep',
    }
});

i18n.locale = getLocales()[0].languageTag ?? 'pt-BR';
i18n.enableFallback = true;
i18n.defaultLocale = 'pt-BR';

export default i18n;
