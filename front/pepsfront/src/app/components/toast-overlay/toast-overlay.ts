import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { NotificationService, ToastItem } from '../../services/notification.service';

@Component({
    selector: 'app-toast-overlay',
    standalone: true,
    imports: [CommonModule, MatIconModule, MatButtonModule],
    template: `
        <div class="toast-container">
            @for (toast of notifService.toasts(); track toast.id) {
                <div class="toast-item" [class.toast-enter]="true">
                    <span class="toast-message">{{ toast.message }}</span>
                    <div class="toast-actions">
                        @if (toast.actionLabel) {
                            <button class="toast-action-btn" (click)="onAction(toast)">{{ toast.actionLabel }}</button>
                        }
                        <button class="toast-close-btn" (click)="dismiss(toast)">✕</button>
                    </div>
                </div>
            }
        </div>
    `,
    styles: [`
        .toast-container {
            position: fixed;
            bottom: 16px;
            right: 16px;
            z-index: 10000;
            display: flex;
            flex-direction: column-reverse;
            gap: 8px;
            max-width: 420px;
        }

        .toast-item {
            background: #323232;
            color: #fff;
            padding: 12px 16px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.3);
            animation: slideIn 0.3s ease-out;
            min-width: 300px;
        }

        @keyframes slideIn {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }

        .toast-message {
            flex: 1;
            font-size: 14px;
            line-height: 1.4;
        }

        .toast-actions {
            display: flex;
            align-items: center;
            gap: 8px;
            flex-shrink: 0;
        }

        .toast-action-btn {
            background: none;
            border: none;
            color: #4fc3f7;
            font-weight: 600;
            font-size: 13px;
            cursor: pointer;
            padding: 4px 8px;
            border-radius: 4px;
            white-space: nowrap;
        }

        .toast-action-btn:hover {
            background: rgba(255,255,255,0.1);
        }

        .toast-close-btn {
            background: none;
            border: none;
            color: rgba(255,255,255,0.7);
            font-size: 16px;
            cursor: pointer;
            padding: 4px 6px;
            border-radius: 4px;
            line-height: 1;
        }

        .toast-close-btn:hover {
            color: #fff;
            background: rgba(255,255,255,0.15);
        }
    `]
})
export class ToastOverlayComponent {
    notifService = inject(NotificationService);

    dismiss(toast: ToastItem) {
        this.notifService.dismissToast(toast.id);
    }

    onAction(toast: ToastItem) {
        if (toast.onAction) {
            toast.onAction();
        }
        this.notifService.dismissToast(toast.id);
    }
}
