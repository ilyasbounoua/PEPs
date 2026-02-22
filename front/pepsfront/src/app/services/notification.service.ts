import { Injectable, inject, signal } from '@angular/core';
import { ApiService } from './api';
import { AuthService } from './auth';
import { I18nService } from './i18n';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';

export interface ToastItem {
    id: number;
    message: string;
    actionLabel?: string;
    onAction?: () => void;
}

@Injectable({
    providedIn: 'root'
})
export class NotificationService {
    private readonly api = inject(ApiService);
    private readonly auth = inject(AuthService);
    private readonly i18n = inject(I18nService);
    private readonly platformId = inject(PLATFORM_ID);

    private pollingInterval: any;
    private highestSeenId: number = 0;
    private toastIdCounter = 0;

    // Track if welcome toast has been shown this session
    private welcomeToastShown = false;

    // Signal-based toast list for the overlay component
    toasts = signal<ToastItem[]>([]);

    // Callback to navigate to the notifications page (set by App component)
    public onNavigateToNotifications: (() => void) | null = null;

    constructor() {
        if (isPlatformBrowser(this.platformId)) {
            const stored = sessionStorage.getItem('welcome_toast_shown');
            if (stored === 'true') {
                this.welcomeToastShown = true;
            }
        }
    }

    /**
     * Called exactly once after a fresh login.
     */
    public showWelcomeToastIfNeeded() {
        if (!this.auth.isAuthenticated() || this.welcomeToastShown) {
            return;
        }

        this.api.getUnreadNotificationCount().subscribe({
            next: (res) => {
                if (res.count > 0) {
                    const message = this.i18n.t('notifications.welcomeToast')
                        .replace('{count}', String(res.count));

                    this.addToast(message, this.i18n.t('notifications.viewNotifs'), () => {
                        if (this.onNavigateToNotifications) {
                            this.onNavigateToNotifications();
                        }
                    });
                }

                this.welcomeToastShown = true;
                if (isPlatformBrowser(this.platformId)) {
                    sessionStorage.setItem('welcome_toast_shown', 'true');
                }

                this.initializeHighestId();
            },
            error: (err) => console.error("Error fetching unread count for welcome toast", err)
        });
    }

    /**
     * Starts polling for new notifications.
     */
    public startPolling() {
        if (this.pollingInterval) {
            return;
        }

        this.initializeHighestId();

        this.pollingInterval = setInterval(() => {
            if (!this.auth.isAuthenticated()) {
                this.stopPolling();
                return;
            }

            this.api.pollNewNotifications(this.highestSeenId).subscribe({
                next: (notifs) => {
                    if (notifs && notifs.length > 0) {
                        for (const n of notifs) {
                            if (n.id > this.highestSeenId) {
                                this.highestSeenId = n.id;
                            }
                            this.showRealTimeToast(n.message);
                        }
                    }
                },
                error: (err) => console.error("Error polling notifications", err)
            });
        }, 10000);
    }

    public stopPolling() {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
            this.pollingInterval = null;
        }
    }

    public resetWelcomeFlag() {
        this.welcomeToastShown = false;
        if (isPlatformBrowser(this.platformId)) {
            sessionStorage.removeItem('welcome_toast_shown');
        }
    }

    /**
     * Translates a structured notification message.
     * Format: "MODULE_OFFLINE|moduleName|moduleIp|ownerRole"
     * Returns { text, profile } for use in the component.
     */
    public translateMessage(message: string): string {
        if (message.startsWith('MODULE_OFFLINE|')) {
            const parts = message.split('|');
            if (parts.length >= 3) {
                return this.i18n.t('notifications.moduleOffline')
                    .replace('{name}', parts[1])
                    .replace('{ip}', parts[2]);
            }
        }
        return message;
    }

    /**
     * Extracts the profile (ownerRole) from a structured message.
     * Returns the profile name or null if not available.
     */
    public extractProfile(message: string): string | null {
        if (message.startsWith('MODULE_OFFLINE|')) {
            const parts = message.split('|');
            if (parts.length >= 4) {
                return parts[3];
            }
        }
        return null;
    }

    // --- Toast management ---

    public addToast(message: string, actionLabel?: string, onAction?: () => void) {
        const id = ++this.toastIdCounter;
        this.toasts.update(list => [...list, { id, message, actionLabel, onAction }]);
    }

    public dismissToast(id: number) {
        this.toasts.update(list => list.filter(t => t.id !== id));
    }

    public dismissAllToasts() {
        this.toasts.set([]);
    }

    // --- Private ---

    private initializeHighestId() {
        this.api.getUnreadNotifications().subscribe({
            next: (notifs) => {
                if (notifs && notifs.length > 0) {
                    const maxId = Math.max(...notifs.map(n => n.id));
                    if (maxId > this.highestSeenId) {
                        this.highestSeenId = maxId;
                    }
                }
            },
            error: (err) => console.error("Failed to initialize highest notification ID", err)
        });
    }

    private showRealTimeToast(rawMessage: string) {
        const translated = this.translateMessage(rawMessage);
        const actionLabel = this.i18n.t('notifications.details');

        this.addToast(translated, actionLabel, () => {
            if (this.onNavigateToNotifications) {
                this.onNavigateToNotifications();
            }
        });
    }
}
