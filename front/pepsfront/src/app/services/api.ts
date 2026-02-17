/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file contains the ApiService, which handles all HTTP requests to the backend.
 *
 * Multi-profile system:
 * - Uses AuthService to get the logged-in user's role
 * - Passes role to endpoints to filter data by profile
 */
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { StatCard, Interaction, Module, DailyData, Sound, UserDTO, CreateUserDTO, UpdateUserDTO, AuditLog, ArchivePeriod, AuditArchivePeriod } from '../models/interfaces';
import { AuthService } from './auth';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  // private readonly BASE_URL = 'https://peps-backend.onrender.com';
  // Fix for local development (NetBeans/Tomcat)
  // Fix for local development (NetBeans/Tomcat)
  private readonly BASE_URL = (environment as any).apiUrl || 'http://localhost:8080/PEPs_back';

  /**
   * Helper to get headers with user login for audit logging.
   */
  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders();
    const login = this.authService.currentLogin();
    if (login) {
      headers = headers.set('X-User-Login', login);
    }
    return headers;
  }

  // Dashboard
  /**
   * Gets dashboard stats.
   * - Regular users: stats for their own role
   * - Admin: stats for ALL data (no role) or filtered by selected role
   * @author Anas EL HOUDI
   */
  getDashboardStats(filterRole?: string, start?: string, end?: string): Observable<StatCard> {
    const userRole = this.authService.currentUserRole();
    const isAdmin = this.authService.isAdmin();

    // Admin can filter by role, regular users always use their own role
    let targetRole = isAdmin ? filterRole : userRole;

    let params = `?role=${targetRole || ''}`; // If targetRole is undefined/empty, send empty string (backend handles this)
    // Actually backend handles "role" param being present. If empty string passed, it might be weird.
    // Let's stick to existing logic where we construct query string.

    let queryParams: string[] = [];
    if (targetRole) queryParams.push(`role=${targetRole}`);
    if (start) queryParams.push(`startDate=${start}`);
    if (end) queryParams.push(`endDate=${end}`);

    const queryString = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';

    return this.http.get<StatCard>(`${this.BASE_URL}/dashboard${queryString}`);
  }

  getDailyStats(filterRole?: string, start?: string, end?: string): Observable<DailyData[]> {
    const userRole = this.authService.currentUserRole();
    const isAdmin = this.authService.isAdmin();

    let targetRole = isAdmin ? filterRole : userRole;

    let queryParams: string[] = [];
    if (targetRole) queryParams.push(`role=${targetRole}`);
    if (start) queryParams.push(`startDate=${start}`);
    if (end) queryParams.push(`endDate=${end}`);

    const queryString = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';

    return this.http.get<DailyData[]>(`${this.BASE_URL}/daily-stats${queryString}`);
  }

  // Interactions
  getInteractions(filterRole?: string): Observable<Interaction[]> {
    const userRole = this.authService.currentUserRole();
    const isAdmin = this.authService.isAdmin();

    let targetRole = isAdmin ? filterRole : userRole;
    const params = targetRole ? `?role=${targetRole}` : '';

    return this.http.get<any[]>(`${this.BASE_URL}/interactions${params}`).pipe(
      map(data => data.map(i => ({
        id: i.id,
        date: new Date(i.date).toISOString().replace('T', ' ').substring(0, 19),
        module: i.module,
        type: i.type
      })))
    );
  }

  // Modules
  getModules(filterRole?: string): Observable<Module[]> {
    const userRole = this.authService.currentUserRole();
    const isAdmin = this.authService.isAdmin();

    let targetRole = isAdmin ? filterRole : userRole;
    const params = targetRole ? `?role=${targetRole}` : '';

    return this.http.get<Module[]>(`${this.BASE_URL}/modules${params}`);
  }

  createModule(module: Omit<Module, 'id'>, overrideRole?: string): Observable<Module> {
    const userRole = this.authService.currentUserRole();
    const isAdmin = this.authService.isAdmin();
    // overrideRole takes precedence (admin assigning to specific role)
    // Otherwise: non-admin users use their own role, admin without override gets undefined
    const ownerRole = overrideRole !== undefined ? overrideRole : (isAdmin ? undefined : userRole);
    const params = ownerRole ? `?role=${ownerRole}` : '';
    return this.http.post<Module>(`${this.BASE_URL}/modules${params}`, module, { headers: this.getHeaders() });
  }

  updateModule(id: number, module: Module): Observable<Module> {
    return this.http.put<Module>(`${this.BASE_URL}/modules/${id}`, module, { headers: this.getHeaders() });
  }

  deleteModule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/modules/${id}`, { headers: this.getHeaders() });
  }

  // Sounds
  getSounds(filterRole?: string): Observable<Sound[]> {
    const userRole = this.authService.currentUserRole();
    const isAdmin = this.authService.isAdmin();

    let targetRole = isAdmin ? filterRole : userRole;
    const params = targetRole ? `?role=${targetRole}` : '';
    const url = `${this.BASE_URL}/sounds${params}`;
    console.log('[API] getSounds - filterRole:', filterRole, 'targetRole:', targetRole, 'URL:', url);

    return this.http.get<Sound[]>(url);
  }

  uploadSound(formData: FormData, overrideRole?: string): Observable<Sound> {
    const userRole = this.authService.currentUserRole();
    const isAdmin = this.authService.isAdmin();
    // overrideRole takes precedence (admin assigning to specific role)
    // Otherwise: non-admin users use their own role, admin without override gets undefined
    const ownerRole = overrideRole !== undefined ? overrideRole : (isAdmin ? undefined : userRole);
    const params = ownerRole ? `?role=${ownerRole}` : '';
    return this.http.post<Sound>(`${this.BASE_URL}/sounds${params}`, formData, { headers: this.getHeaders() });
  }

  updateSound(id: number, data: { name: string, type: string }): Observable<Sound> {
    return this.http.put<Sound>(`${this.BASE_URL}/sounds/${id}`, data, { headers: this.getHeaders() });
  }

  deleteSound(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/sounds/${id}`, { headers: this.getHeaders() });
  }

  getSoundFileUrl(id: number): string {
    return `${this.BASE_URL}/sounds/${id}/file`;
  }

  /* ===================== */
  /* Gestion Utilisateurs (Admin uniquement) */
  /* ===================== */

  /**
   * Liste tous les utilisateurs.
   * Accessible uniquement aux administrateurs.
   */
  getUsers(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(`${this.BASE_URL}/users`);
  }

  /**
   * Récupère les rôles distincts depuis la base de données.
   * Pour le filtre par profil.
   */
  getRoles(): Observable<string[]> {
    return this.getUsers().pipe(
      map(users => {
        const roles = users
          .map(u => u.role)
          .filter(r => r !== 'admin');
        return [...new Set(roles)];
      })
    );
  }

  /**
   * Récupère un utilisateur par son ID.
   */
  getUserById(id: number): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.BASE_URL}/users/${id}`);
  }

  /**
   * Crée un nouvel utilisateur.
   * @param data Login, password et role du nouvel utilisateur
   */
  createUser(data: CreateUserDTO): Observable<UserDTO> {
    return this.http.post<UserDTO>(`${this.BASE_URL}/users`, data, { headers: this.getHeaders() });
  }

  /**
   * Modifie un utilisateur existant.
   * @param id ID de l'utilisateur
   * @param data Champs à modifier (tous optionnels)
   */
  updateUser(id: number, data: UpdateUserDTO): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${this.BASE_URL}/users/${id}`, data, { headers: this.getHeaders() });
  }

  /**
   * Supprime un utilisateur.
   */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/users/${id}`, { headers: this.getHeaders() });
  }

  /* ===================== */
  /* User Password Change */
  /* ===================== */

  /**
   * Allows the user to change their own password.
   * Requires current password for validation.
   * @author Anas EL HOUDI
   */
  changePassword(userId: number, currentPassword: string, newPassword: string): Observable<any> {
    return this.http.put<any>(`${this.BASE_URL}/users/${userId}/password`, {
      currentPassword,
      newPassword
    }, { headers: this.getHeaders() });
  }

  /* ===================== */
  /* Audit Logs */
  /* ===================== */

  /**
   * Gets all audit logs (admin only).
   * Returns logs sorted by timestamp descending (most recent first).
   * @author Anas EL HOUDI
   */
  getAuditLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.BASE_URL}/audit-logs`);
  }

  /**
   * Gets audit logs filtered by entity type.
   */
  getAuditLogsByEntity(entityType: string): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.BASE_URL}/audit-logs/by-entity/${entityType}`);
  }

  /**
   * Gets audit logs for a specific user.
   */
  getAuditLogsByUser(userLogin: string): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.BASE_URL}/audit-logs/by-user/${userLogin}`);
  }

  /* ===================== */
  /* Archive Management (Admin only) */
  /* ===================== */

  /**
   * Gets available archive periods (quarters older than 3 months).
   * @author Anas EL HOUDI
   */
  getArchivePeriods(): Observable<ArchivePeriod[]> {
    return this.http.get<ArchivePeriod[]>(`${this.BASE_URL}/archive/periods`);
  }

  /**
   * Exports and deletes interactions for a specific period.
   * Returns a Blob for file download.
   * @author Anas EL HOUDI
   */
  exportAndDeletePeriod(periodId: string): Observable<Blob> {
    return this.http.post(`${this.BASE_URL}/archive/export?periodId=${periodId}`, null, {
      responseType: 'blob'
    });
  }

  /**
   * Exports and deletes ALL archive periods.
   * Returns a Blob for file download.
   * @author Anas EL HOUDI
   */
  exportAndDeleteAllPeriods(): Observable<Blob> {
    return this.http.post(`${this.BASE_URL}/archive/export-all`, null, {
      responseType: 'blob'
    });
  }

  /* ===================== */
  /* Audit Log Archive Management (Admin only) */
  /* ===================== */

  /**
   * Gets available audit log archive periods (quarters older than 3 months).
   * @author Anas EL HOUDI
   */
  getAuditArchivePeriods(): Observable<AuditArchivePeriod[]> {
    return this.http.get<AuditArchivePeriod[]>(`${this.BASE_URL}/archive/audit-periods`);
  }

  /**
   * Exports and deletes audit logs for a specific period.
   * Returns a Blob for file download.
   * @author Anas EL HOUDI
   */
  exportAndDeleteAuditPeriod(periodId: string): Observable<Blob> {
    return this.http.post(`${this.BASE_URL}/archive/audit-export?periodId=${periodId}`, null, {
      responseType: 'blob'
    });
  }

  /**
   * Exports and deletes ALL audit log archive periods.
   * Returns a Blob for file download.
   * @author Anas EL HOUDI
   */
  exportAndDeleteAllAuditPeriods(): Observable<Blob> {
    return this.http.post(`${this.BASE_URL}/archive/audit-export-all`, null, {
      responseType: 'blob'
    });
  }
}
