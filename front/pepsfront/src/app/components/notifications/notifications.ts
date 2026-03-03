import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { DatePipe } from '@angular/common';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';
import { I18nService } from '../../services/i18n';
import { NotificationService } from '../../services/notification.service';
import { Notification } from '../../models/interfaces';

@Component({
    selector: 'app-notifications',
    standalone: true,
    imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatDividerModule, DatePipe],
    templateUrl: './notifications.html',
    styleUrls: ['./notifications.css']
})
export class NotificationsComponent implements OnInit {
    public api = inject(ApiService);
    public auth = inject(AuthService);
    public i18n = inject(I18nService);
    private notifService = inject(NotificationService);

    notifications = signal<Notification[]>([]);
    isLoading = signal(false);

    ngOnInit() {
        this.loadNotifications();
    }

    /**
     * Translates a structured notification message using the current language.
     */
    translateMessage(message: string): string {
        return this.notifService.translateMessage(message);
    }

    /**
     * Extracts the profile name from a structured notification message.
     * Returns null if not available (e.g. old format messages).
     */
    extractProfile(message: string): string | null {
        return this.notifService.extractProfile(message);
    }

    loadNotifications() {
        this.isLoading.set(true);
        this.api.getNotifications().subscribe({
            next: (data) => {
                this.notifications.set(data);
                this.isLoading.set(false);
                // Mark unread as read
                data.filter(n => !n.isRead).forEach(n => {
                    this.api.markNotificationAsRead(n.id).subscribe();
                });
            },
            error: (err) => {
                console.error("Error loading notifications:", err);
                this.isLoading.set(false);
            }
        });
    }

    delete(id: number) {
        const title = this.i18n.t('notifications.deleteTitle');
        const message = this.i18n.t('notifications.deleteMessage');

        if (window.confirm(`${title}\n\n${message}`)) {
            this.api.deleteNotification(id).subscribe({
                next: () => {
                    this.notifications.update(notifs => notifs.filter(n => n.id !== id));
                },
                error: (err) => console.error(err)
            });
        }
    }

    deleteAll() {
        const title = this.i18n.t('notifications.deleteAllTitle');
        const message = this.i18n.t('notifications.deleteAllMessage');

        if (window.confirm(`${title}\n\n${message}`)) {
            this.api.deleteAllNotifications().subscribe({
                next: () => {
                    this.notifications.set([]);
                },
                error: (err) => console.error(err)
            });
        }
    }
}
