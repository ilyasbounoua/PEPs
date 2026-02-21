/**
 * I18nService — Internationalization service for PEP'S
 * 
 * Hybrid approach:
 * - Before login: language is loaded from localStorage (default: 'fr')
 * - After login: language is loaded from the user's DB preference
 * - On language change: saved to both localStorage AND database
 *
 * @author Anas EL HOUDI
 */
import { Injectable, signal, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

import frTranslations from '../../assets/i18n/fr.json';
import enTranslations from '../../assets/i18n/en.json';

/** Supported languages */
export type Lang = 'fr' | 'en';

/** Storage key for localStorage */
const LANG_KEY = 'peps_lang';

@Injectable({
    providedIn: 'root',
})
export class I18nService {

    private readonly http = inject(HttpClient);
    private platformId = inject(PLATFORM_ID);

    /** All translations loaded in memory */
    private translations: Record<Lang, any> = {
        fr: frTranslations,
        en: enTranslations
    };

    /** Current language signal */
    currentLang = signal<Lang>(this.getInitialLang());

    /**
     * Translate a key using dot notation.
     * Example: t('login.title') → 'PEP'S Login'
     */
    t(key: string): string {
        const lang = this.currentLang();
        const dict = this.translations[lang];
        const value = this.resolveKey(dict, key);
        if (value === undefined) {
            console.warn(`[I18n] Missing translation: ${key} (${lang})`);
            return key;
        }
        return value;
    }

    /**
     * Set language (updates signal + saves to localStorage).
     * Does NOT save to database — call saveToDatabase() separately.
     */
    setLang(lang: Lang): void {
        this.currentLang.set(lang);
        this.saveToLocalStorage(lang);
    }

    /**
     * Called after login: applies the user's DB-stored language preference.
     * Also updates localStorage so the login page remembers the last language.
     */
    loadFromUser(preferredLang: string | undefined): void {
        const lang: Lang = (preferredLang === 'en') ? 'en' : 'fr';
        this.currentLang.set(lang);
        this.saveToLocalStorage(lang);
    }

    /**
     * Save language preference to the database.
     * Called when the user manually toggles the language while logged in.
     */
    saveToDatabase(userId: number, lang: Lang): void {
        const baseUrl = (environment as any).apiUrl || 'http://localhost:8080/PEPs_back';
        this.http.put(`${baseUrl}/users/${userId}/language`, { lang }).subscribe({
            next: () => console.log(`[I18n] Language saved to DB: ${lang}`),
            error: (err) => console.error('[I18n] Error saving language to DB:', err)
        });
    }

    /**
     * Toggle between French and English.
     * Returns the new language.
     */
    toggle(): Lang {
        const newLang: Lang = this.currentLang() === 'fr' ? 'en' : 'fr';
        this.setLang(newLang);
        return newLang;
    }

    // --- Private helpers ---

    private getInitialLang(): Lang {
        // 1. Check localStorage (user's explicit choice persists)
        if (typeof localStorage !== 'undefined') {
            try {
                const stored = localStorage.getItem(LANG_KEY);
                if (stored === 'en' || stored === 'fr') {
                    return stored;
                }
            } catch (e) {
                // SSR or storage error
            }
        }

        // 2. Detect browser language as fallback
        if (typeof navigator !== 'undefined' && navigator.language) {
            const browserLang = navigator.language.toLowerCase();
            if (browserLang.startsWith('en')) {
                return 'en';
            }
        }

        // 3. Default to French
        return 'fr';
    }

    private saveToLocalStorage(lang: Lang): void {
        if (typeof localStorage !== 'undefined') {
            try {
                localStorage.setItem(LANG_KEY, lang);
            } catch (e) {
                // Ignore
            }
        }
    }

    /**
     * Resolve a dot-separated key in a nested object.
     * e.g., resolveKey(obj, 'login.title') → obj.login.title
     */
    private resolveKey(obj: any, key: string): string | undefined {
        return key.split('.').reduce((acc, part) => {
            if (acc && typeof acc === 'object') {
                return acc[part];
            }
            return undefined;
        }, obj);
    }
}
