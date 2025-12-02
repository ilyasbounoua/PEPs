/**
 * @author BOUNOUA Ilyas and VAZEILLE Clément
 * @description This file contains the ApiService, which handles all HTTP requests to the backend.
 */
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { StatCard, Interaction, Module, DailyData, Sound } from '../models/interfaces';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly BASE_URL = 'https://peps-backend.onrender.com';

  // Dashboard
  getDashboardStats(): Observable<StatCard> {
    return this.http.get<StatCard>(`${this.BASE_URL}/dashboard`);
  }

  getDailyStats(): Observable<DailyData[]> {
    return this.http.get<DailyData[]>(`${this.BASE_URL}/daily-stats`);
  }

  // Interactions
  getInteractions(): Observable<Interaction[]> {
    return this.http.get<any[]>(`${this.BASE_URL}/interactions`).pipe(
      map(data => data.map(i => ({
        id: i.id,
        date: new Date(i.date).toISOString().replace('T', ' ').substring(0, 19),
        module: i.module,
        type: i.type
      })))
    );
  }

  // Modules
  getModules(): Observable<Module[]> {
    return this.http.get<Module[]>(`${this.BASE_URL}/modules`);
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
  getSounds(): Observable<Sound[]> {
    return this.http.get<Sound[]>(`${this.BASE_URL}/sounds`);
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
}
