/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file contains the ApiService, which handles all HTTP requests to the backend.
 * 
 * Multi-profile system:
 * - Uses AuthService to get the logged-in user's role
 * - Passes role to endpoints to filter data by profile
 */
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { StatCard, Interaction, Module, DailyData, Sound, UserDTO, CreateUserDTO, UpdateUserDTO } from '../models/interfaces';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  // private readonly BASE_URL = 'https://peps-backend.onrender.com';
  // Fix for local development (NetBeans/Tomcat)
  private readonly BASE_URL = 'http://localhost:8080/PEPs_back';

  // Dashboard
  /**
   * Gets dashboard stats.
   * - Regular users: stats for their own role
   * - Admin: stats for ALL data (no role) or filtered by selected role
   * @author Anas EL HOUDI
   */
  getDashboardStats(filterRole?: string): Observable<StatCard> {
    const userRole = this.authService.currentUserRole();
    const isAdmin = this.authService.isAdmin();

    // Admin can filter by role, regular users always use their own role
    let targetRole = isAdmin ? filterRole : userRole;
    const params = targetRole ? `?role=${targetRole}` : '';

    return this.http.get<StatCard>(`${this.BASE_URL}/dashboard${params}`);
  }

  getDailyStats(filterRole?: string): Observable<DailyData[]> {
    const userRole = this.authService.currentUserRole();
    const isAdmin = this.authService.isAdmin();

    let targetRole = isAdmin ? filterRole : userRole;
    const params = targetRole ? `?role=${targetRole}` : '';

    return this.http.get<DailyData[]>(`${this.BASE_URL}/daily-stats${params}`);
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

  createModule(module: Omit<Module, 'id'>): Observable<Module> {
    return this.http.post<Module>(`${this.BASE_URL}/modules`, module);
  }

  updateModule(id: number, module: Module): Observable<Module> {
    return this.http.put<Module>(`${this.BASE_URL}/modules/${id}`, module);
  }

  deleteModule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/modules/${id}`);
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

  uploadSound(formData: FormData): Observable<Sound> {
    return this.http.post<Sound>(`${this.BASE_URL}/sounds`, formData);
  }

  updateSound(id: number, data: { name: string, type: string }): Observable<Sound> {
    return this.http.put<Sound>(`${this.BASE_URL}/sounds/${id}`, data);
  }

  deleteSound(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/sounds/${id}`);
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
    return this.http.post<UserDTO>(`${this.BASE_URL}/users`, data);
  }

  /**
   * Modifie un utilisateur existant.
   * @param id ID de l'utilisateur
   * @param data Champs à modifier (tous optionnels)
   */
  updateUser(id: number, data: UpdateUserDTO): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${this.BASE_URL}/users/${id}`, data);
  }

  /**
   * Supprime un utilisateur.
   */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/users/${id}`);
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
    });
  }
}
