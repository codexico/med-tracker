import * as SQLite from 'expo-sqlite';
import { MedEvent } from '@/types';

// Using openDatabaseSync for synchronous-like simplified API where possible, 
// but async implementation is standard for Expo SQLite now.
// Note: expo-sqlite next/modern API might require openDatabaseAsync or similiar.
// Checking docs, openDatabaseSync is valid in recent versions for synchronous access 
// if WAL is enabled, or use useSQLiteContext. 
// For simplicity in non-component files, we'll use the imperative API.

let db: SQLite.SQLiteDatabase;

const getDb = async () => {
    if (!db) {
        db = await SQLite.openDatabaseAsync('medtracker.db');
    }
    return db;
};

export const initDatabase = async () => {
    const database = await getDb();
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

    // Migration: Add notificationId column if it doesn't exist (primitive migration)
    try {
        await database.execAsync('ALTER TABLE events ADD COLUMN notificationId TEXT;');
    } catch (e) {
        // Column likely exists, ignore
    }
};

export const saveEvent = async (event: MedEvent) => {
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
    const rows = await database.getAllAsync<any>('SELECT * FROM events');
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
