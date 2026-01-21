/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file contains the ApiService, which handles all HTTP requests to the backend.
 * 
 * Multi-profile system:
 * - Uses AuthService to get the logged-in user's ID
 * - Passes ownerId to endpoints to filter data by user
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
   * - Regular users: stats for their own data (pass ownerId)
   * - Admin: stats for ALL data (no ownerId)
   * @author Anas EL HOUDI
   */
  getDashboardStats(filterOwnerId?: number): Observable<StatCard> {
    const userId = this.authService.currentUserId();
    const isAdmin = this.authService.isAdmin();

    let targetId = isAdmin ? filterOwnerId : userId;
    const params = targetId ? `?ownerId=${targetId}` : '';

    return this.http.get<StatCard>(`${this.BASE_URL}/dashboard${params}`);
  }

  getDailyStats(filterOwnerId?: number): Observable<DailyData[]> {
    const userId = this.authService.currentUserId();
    const isAdmin = this.authService.isAdmin();

    let targetId = isAdmin ? filterOwnerId : userId;
    const params = targetId ? `?ownerId=${targetId}` : '';

    return this.http.get<DailyData[]>(`${this.BASE_URL}/daily-stats${params}`);
  }

  // Interactions
  // Interactions
  getInteractions(filterOwnerId?: number): Observable<Interaction[]> {
    const userId = this.authService.currentUserId();
    const isAdmin = this.authService.isAdmin();

    // If admin provides a filter ID, use it. 
    // If admin provides no filter, use no param (get all).
    // If regular user, ALWAYS enforce their own ID.
    let targetId = isAdmin ? filterOwnerId : userId;

    // If admin and explicit null passed (meaning "All"), ensure param is empty
    const params = targetId ? `?ownerId=${targetId}` : '';

    return this.http.get<any[]>(`${this.BASE_URL}/interactions${params}`).pipe(
      map(data => data.map(i => ({
        id: i.id, // Backend DTO uses 'id'
        date: new Date(i.date).toISOString().replace('T', ' ').substring(0, 19), // Backend DTO uses 'date'
        module: i.module, // Backend DTO uses 'module'
        type: i.type // Backend DTO uses 'type'
      })))
    );
  }

  // Modules
  // Modules
  getModules(filterOwnerId?: number): Observable<Module[]> {
    const userId = this.authService.currentUserId();
    const isAdmin = this.authService.isAdmin();

    let targetId = isAdmin ? filterOwnerId : userId;
    const params = targetId ? `?ownerId=${targetId}` : '';

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
  // Sounds
  getSounds(filterOwnerId?: number): Observable<Sound[]> {
    const userId = this.authService.currentUserId();
    const isAdmin = this.authService.isAdmin();

    let targetId = isAdmin ? filterOwnerId : userId;
    const params = targetId ? `?ownerId=${targetId}` : '';

    return this.http.get<Sound[]>(`${this.BASE_URL}/sounds${params}`);
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

