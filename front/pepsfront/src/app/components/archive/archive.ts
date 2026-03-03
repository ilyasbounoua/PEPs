/**
 * @author Anas EL HOUDI
 * @description Archive component for managing old interaction logs and audit logs.
 * Allows administrators to view, export, and delete archived data
 * grouped by 3-month periods.
 * 
 * Features:
 * - Display list of archive periods for interactions (older than 3 months)
 * - Display list of archive periods for audit logs (older than 3 months)
 * - Export single period as JSON for each type
 * - Export all periods as JSON for each type
 * - Confirmation dialog before export (data deletion warning)
 */
import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService } from '../../services/api';
import { ArchivePeriod, AuditArchivePeriod } from '../../models/interfaces';

@Component({
    selector: 'app-archive',
    imports: [
        CommonModule,
        MatCardModule,
        MatTableModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatTooltipModule
    ],
    templateUrl: './archive.html',
    styleUrl: './archive.css',
})
export class ArchiveComponent implements OnInit {
    private api = inject(ApiService);

    // Interaction archive
    periods = signal<ArchivePeriod[]>([]);
    isLoading = signal(false);
    isExporting = signal(false);
    displayedColumns: string[] = ['period', 'count', 'actions'];

    // Audit log archive
    auditPeriods = signal<AuditArchivePeriod[]>([]);
    isLoadingAudit = signal(false);
    isExportingAudit = signal(false);

    ngOnInit() {
        this.loadPeriods();
        this.loadAuditPeriods();
    }

    /* ===================== */
    /* INTERACTION ARCHIVE */
    /* ===================== */

    loadPeriods() {
        this.isLoading.set(true);
        this.api.getArchivePeriods().subscribe({
            next: (data) => {
                this.periods.set(data);
                this.isLoading.set(false);
            },
            error: (err) => {
                console.error('Error loading archive periods:', err);
                this.isLoading.set(false);
            }
        });
    }

    /**
     * Export a single period as JSON and delete from database.
     * Shows confirmation dialog before proceeding.
     */
    exportPeriod(period: ArchivePeriod) {
        const confirmMessage = `Êtes-vous sûr de vouloir exporter la période "${period.periodLabel}" ?\n\n` +
            `⚠️ ATTENTION: Les ${period.interactionCount} interactions de cette période seront ` +
            `définitivement supprimées de la base de données après l'export.\n\n` +
            `Cette action est irréversible.`;

        if (!confirm(confirmMessage)) {
            return;
        }

        this.isExporting.set(true);
        this.api.exportAndDeletePeriod(period.periodId).subscribe({
            next: (blob) => {
                this.downloadBlob(blob, `interactions_${period.periodId}.json`);
                this.loadPeriods();
                this.isExporting.set(false);
            },
            error: (err) => {
                console.error('Error exporting period:', err);
                alert('Erreur lors de l\'export. Veuillez réessayer.');
                this.isExporting.set(false);
            }
        });
    }

    /**
     * Export ALL periods as a single JSON file and delete from database.
     * Shows confirmation dialog before proceeding.
     */
    exportAll() {
        const totalCount = this.periods().reduce((sum, p) => sum + p.interactionCount, 0);
        const confirmMessage = `Êtes-vous sûr de vouloir exporter TOUTES les périodes d'interactions archivées ?\n\n` +
            `⚠️ ATTENTION: ${totalCount} interactions au total seront ` +
            `définitivement supprimées de la base de données après l'export.\n\n` +
            `Cette action est irréversible.`;

        if (!confirm(confirmMessage)) {
            return;
        }

        this.isExporting.set(true);
        this.api.exportAndDeleteAllPeriods().subscribe({
            next: (blob) => {
                const today = new Date().toISOString().split('T')[0];
                this.downloadBlob(blob, `interactions_archive_${today}.json`);
                this.loadPeriods();
                this.isExporting.set(false);
            },
            error: (err) => {
                console.error('Error exporting all periods:', err);
                alert('Erreur lors de l\'export. Veuillez réessayer.');
                this.isExporting.set(false);
            }
        });
    }

    /* ===================== */
    /* AUDIT LOG ARCHIVE */
    /* ===================== */

    loadAuditPeriods() {
        this.isLoadingAudit.set(true);
        this.api.getAuditArchivePeriods().subscribe({
            next: (data) => {
                this.auditPeriods.set(data);
                this.isLoadingAudit.set(false);
            },
            error: (err) => {
                console.error('Error loading audit archive periods:', err);
                this.isLoadingAudit.set(false);
            }
        });
    }

    /**
     * Export audit logs for a single period as JSON and delete from database.
     */
    exportAuditPeriod(period: AuditArchivePeriod) {
        const confirmMessage = `Êtes-vous sûr de vouloir exporter la période "${period.periodLabel}" du journal d'audit ?\n\n` +
            `⚠️ ATTENTION: Les ${period.interactionCount} entrées d'audit de cette période seront ` +
            `définitivement supprimées de la base de données après l'export.\n\n` +
            `Cette action est irréversible.`;

        if (!confirm(confirmMessage)) {
            return;
        }

        this.isExportingAudit.set(true);
        this.api.exportAndDeleteAuditPeriod(period.periodId).subscribe({
            next: (blob) => {
                this.downloadBlob(blob, `audit_logs_${period.periodId}.json`);
                this.loadAuditPeriods();
                this.isExportingAudit.set(false);
            },
            error: (err) => {
                console.error('Error exporting audit period:', err);
                alert('Erreur lors de l\'export. Veuillez réessayer.');
                this.isExportingAudit.set(false);
            }
        });
    }

    /**
     * Export ALL audit log periods as a single JSON file and delete from database.
     */
    exportAllAudit() {
        const totalCount = this.auditPeriods().reduce((sum, p) => sum + p.interactionCount, 0);
        const confirmMessage = `Êtes-vous sûr de vouloir exporter TOUTES les périodes d'audit archivées ?\n\n` +
            `⚠️ ATTENTION: ${totalCount} entrées d'audit au total seront ` +
            `définitivement supprimées de la base de données après l'export.\n\n` +
            `Cette action est irréversible.`;

        if (!confirm(confirmMessage)) {
            return;
        }

        this.isExportingAudit.set(true);
        this.api.exportAndDeleteAllAuditPeriods().subscribe({
            next: (blob) => {
                const today = new Date().toISOString().split('T')[0];
                this.downloadBlob(blob, `audit_logs_archive_${today}.json`);
                this.loadAuditPeriods();
                this.isExportingAudit.set(false);
            },
            error: (err) => {
                console.error('Error exporting all audit periods:', err);
                alert('Erreur lors de l\'export. Veuillez réessayer.');
                this.isExportingAudit.set(false);
            }
        });
    }

    /* ===================== */
    /* HELPER METHODS */
    /* ===================== */

    /**
     * Helper to download a blob as a file.
     */
    private downloadBlob(blob: Blob, filename: string) {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    }
}
