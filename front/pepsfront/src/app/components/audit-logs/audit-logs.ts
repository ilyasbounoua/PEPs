import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApiService } from '../../services/api';
import { AuditLog } from '../../models/interfaces';
import { I18nService } from '../../services/i18n';

/**
 * Composant pour afficher le journal d'audit (admin only).
 * Affiche les actions CREATE, UPDATE, DELETE avec des couleurs distinctes.
 * 
 * @author Anas EL HOUDI
 */
@Component({
    selector: 'app-audit-logs',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatTableModule,
        MatCardModule,
        MatIconModule,
        MatButtonModule,
        MatSelectModule,
        MatFormFieldModule,
        MatTooltipModule,
        MatProgressSpinnerModule
    ],
    templateUrl: './audit-logs.html',
    styleUrls: ['./audit-logs.css']
})
export class AuditLogsComponent implements OnInit {
    private api = inject(ApiService);
    readonly i18n = inject(I18nService);

    logs = signal<AuditLog[]>([]);
    loading = signal(false);
    error = signal<string | null>(null);
    filterEntity = signal<string>('all');
    expandedLogId = signal<number | null>(null);

    displayedColumns = ['action', 'entity', 'userLogin', 'timestamp', 'details'];

    ngOnInit() {
        this.loadLogs();
    }

    loadLogs() {
        this.loading.set(true);
        this.error.set(null);

        const entityFilter = this.filterEntity();
        const request = entityFilter === 'all'
            ? this.api.getAuditLogs()
            : this.api.getAuditLogsByEntity(entityFilter);

        request.subscribe({
            next: (data) => {
                this.logs.set(data);
                this.loading.set(false);
            },
            error: (err) => {
                console.error('[AuditLogs] Error loading logs:', err);
                this.error.set(this.i18n.t('auditLogs.loadError'));
                this.loading.set(false);
            }
        });
    }

    onFilterChange() {
        this.loadLogs();
    }

    toggleExpand(logId: number) {
        if (this.expandedLogId() === logId) {
            this.expandedLogId.set(null);
        } else {
            this.expandedLogId.set(logId);
        }
    }

    isExpanded(logId: number): boolean {
        return this.expandedLogId() === logId;
    }

    getActionIcon(action: string): string {
        switch (action) {
            case 'CREATE': return 'add_circle';
            case 'UPDATE': return 'edit';
            case 'DELETE': return 'delete';
            default: return 'info';
        }
    }

    getActionClass(action: string): string {
        switch (action) {
            case 'CREATE': return 'action-create';
            case 'UPDATE': return 'action-update';
            case 'DELETE': return 'action-delete';
            default: return '';
        }
    }

    formatTimestamp(timestamp: any): string {
        if (!timestamp) return '';

        let date: Date;

        // Si c'est un tableau [y, m, d, h, m, s] (format Java 8 par défaut parfois)
        if (Array.isArray(timestamp)) {
            date = new Date(timestamp[0], timestamp[1] - 1, timestamp[2], timestamp[3], timestamp[4], timestamp[5] || 0);
        } else {
            // Si c'est un nombre (timestamp epoch) ou une string
            // Assurer que si c'est une string purement numérique, on la convertit en nombre
            if (typeof timestamp === 'string' && /^\d+$/.test(timestamp)) {
                timestamp = parseInt(timestamp, 10);
            }
            date = new Date(timestamp);
        }

        if (isNaN(date.getTime())) {
            console.warn('Invalid date received:', timestamp);
            return this.i18n.t('auditLogs.invalidDate');
        }

        return date.toLocaleDateString('fr-FR') + ' ' + date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    }

    formatEntityInfo(log: AuditLog): string {
        let info = log.entityType;
        if (log.entityName) {
            info += ` "${log.entityName}"`;
        }
        if (log.entityId) {
            info += ` #${log.entityId}`;
        }
        if (log.entityRole) {
            info += ` (${log.entityRole})`;
        }
        return info;
    }

    parseJson(value: string | null): any {
        if (!value) return null;
        try {
            return JSON.parse(value);
        } catch {
            return value;
        }
    }
}
