import * as SQLite from 'expo-sqlite';

import { MedEvent } from '@/types';
import { createInitialEvents } from '@/constants/Events';


let db: SQLite.SQLiteDatabase;

const getDb = async () => {
    if (!db) {
        db = await SQLite.openDatabaseAsync('medtracker.db');
    }
    return db;
};

// This function will be called by SQLiteProvider's onInit
export const initializeDatabase = async (database: SQLite.SQLiteDatabase) => {
    // 1. Create Tables
    await database.execAsync(`
        PRAGMA journal_mode = WAL;
        CREATE TABLE IF NOT EXISTS events (
            id TEXT PRIMARY KEY NOT NULL,
            label TEXT NOT NULL,
            time TEXT NOT NULL,
            icon TEXT NOT NULL,
            enabled INTEGER NOT NULL,
            completedToday INTEGER NOT NULL,
            lastCompletedDate TEXT,
            medications TEXT,
            notificationId TEXT
        );
        CREATE TABLE IF NOT EXISTS settings (
            key TEXT PRIMARY KEY NOT NULL,
            value TEXT
        );
    `);

    // 2. Check and Seed Data
    const result = await database.getFirstAsync<{ count: number }>('SELECT COUNT(*) as count FROM events');
    const count = result?.count ?? 0;
    console.log('Database count:', count);
    if (count === 0) {
        console.log('Seeding initial events...');
        const initialEvents = createInitialEvents();
        for (const event of initialEvents) {
            await database.runAsync(
                `INSERT OR REPLACE INTO events (id, label, time, icon, enabled, completedToday, lastCompletedDate, medications, notificationId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                event.id,
                event.label,
                event.time,
                event.icon,
                1, // enabled   
                0, // completedToday
                null, // lastCompletedDate   
                JSON.stringify([]), // medications
                null // notificationId
            );
        }
    } else {
        console.log('Database already initialized with events.');
    }
};

export const saveEvent = async (event: MedEvent) => {
    console.log('Saving event:', event.label);
    const database = await getDb();
    await database.runAsync(
        `INSERT OR REPLACE INTO events (id, label, time, icon, enabled, completedToday, lastCompletedDate, medications, notificationId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
        event.id,
        event.label,
        event.time,
        event.icon,
        event.enabled ? 1 : 0,
        event.completedToday ? 1 : 0,
        event.lastCompletedDate || null,
        JSON.stringify(event.medications || []),
        event.notificationId || null
    );
};

export const getEvents = async (): Promise<MedEvent[]> => {
    const database = await getDb();
    const rows = await database.getAllAsync<any>('SELECT * FROM events ORDER BY time ASC');
    const today = new Date().toISOString().split('T')[0];

    // Check for resets needed
    const updates: Promise<any>[] = [];
    const events = rows.map(row => {
        let completedToday = !!row.completedToday;
        // If it was completed but on a different day, reset it
        if (completedToday && row.lastCompletedDate !== today) {
            completedToday = false;
            // distinct update promise
            updates.push(database.runAsync('UPDATE events SET completedToday = 0 WHERE id = ?', row.id));
        }

        return {
            id: row.id,
            label: row.label,
            time: row.time,
            icon: row.icon,
            enabled: !!row.enabled,
            completedToday: completedToday,
            lastCompletedDate: row.lastCompletedDate,
            medications: JSON.parse(row.medications || '[]'),
            notificationId: row.notificationId,
        };
    });

    await Promise.all(updates);

    return events;
};

export const toggleEventCompletion = async (id: string, completed: boolean) => {
    const database = await getDb();
    const today = new Date().toISOString().split('T')[0];
    await database.runAsync(
        'UPDATE events SET completedToday = ?, lastCompletedDate = ? WHERE id = ?',
        completed ? 1 : 0,
        completed ? today : null, // If uncompleting, we can clear the date or leave it. Clearing seems safer for logic.
        id
    );
};

export const deleteEvent = async (id: string) => {
    const database = await getDb();
    await database.runAsync('DELETE FROM events WHERE id = ?', id);
};

// Settings helpers
export const saveSetting = async (key: string, value: string) => {
    const database = await getDb();
    await database.runAsync(
        'INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)',
        key,
        value
    );
};

export const getSetting = async (key: string): Promise<string | null> => {
    const database = await getDb();
    const result = await database.getFirstAsync<{ value: string }>('SELECT value FROM settings WHERE key = ?', key);
    return result ? result.value : null;
};
