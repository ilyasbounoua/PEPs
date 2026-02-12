/**
 * @author BOUNOUA Ilyas, VAZEILLE Clément, Anas EL HOUDI
 * @description This file contains the TypeScript interfaces used throughout the application to define data structures.
 */
export interface StatCard {
  totalInteractions: number;
  activeModules: number;
  lastInteraction: string;
}

export interface Interaction {
  id: number;
  date: string;
  module: string;
  type: string;
}

export interface Module {
  id: number;
  name: string;
  location: string;
  status: 'Actif' | 'Inactif';
  ip: string;
  config: ModuleConfig;
}

export interface ModuleConfig {
  volume: number;
  mode: 'Manuel' | 'Automatique';
  actif: boolean;
  son: boolean;
}

export interface DailyData {
  time: string;
  count: number;
}

export interface Sound {
  id: number;
  name: string;
  type: string;
  extension: string;
  fileName: string;
}

export interface NewSound {
  name: string;
  type: string;
  file: File | null;
}

export type SoundFilter = 'all' | 'Vocal' | 'Ambiance' | 'Naturel' | 'Autre';
export type PageType = 'dashboard' | 'interactions' | 'modules' | 'module-detail' | 'add-module' | 'sounds' | 'users' | 'account' | 'audit-logs' | 'archive';
export type PermissionType = 'viewer' | 'editor';

/* ===================== */
/* Archive Period Interface */
/* ===================== */

/**
 * Represents a 3-month archive period for interaction logs.
 * Used by the Archive section to display available periods for export.
 * @author Anas EL HOUDI
 */
export interface ArchivePeriod {
  periodId: string;       // e.g., "2025-07"
  periodLabel: string;    // e.g., "Juillet 2025 - Septembre 2025"
  startDate: string;
  endDate: string;
  interactionCount: number;
}

/**
 * Represents a 3-month archive period for audit logs.
 * Used by the Archive section to display available audit log periods for export.
 * @author Anas EL HOUDI
 */
export interface AuditArchivePeriod {
  periodId: string;       // e.g., "2025-07"
  periodLabel: string;    // e.g., "Juillet 2025 - Septembre 2025"
  startDate: string;
  endDate: string;
  interactionCount: number;  // Note: backend uses same field name for count
}

/* ===================== */
/* Interfaces Utilisateurs (Système multi-profils) */
/* ===================== */

/**
 * Représente un utilisateur retourné par l'API.
 * Le password_hash n'est jamais exposé pour des raisons de sécurité.
 */
export interface UserDTO {
  id: number;
  login: string;
  role: 'admin' | 'dauphin' | 'aras' | string;
  permission: PermissionType;
  enabled: boolean;
}

/**
 * Données pour créer un nouvel utilisateur.
 */
export interface CreateUserDTO {
  login: string;
  password: string;
  role: 'admin' | 'dauphin' | 'aras' | string;
  permission: PermissionType;
}

/**
 * Données pour modifier un utilisateur existant.
 * Tous les champs sont optionnels.
 */
export interface UpdateUserDTO {
  login?: string;
  password?: string;
  role?: 'admin' | 'dauphin' | 'aras' | string;
  permission?: PermissionType;
}

/* ===================== */
/* Interface Journal d'Audit */
/* ===================== */

/**
 * Représente une entrée du journal d'audit.
 * Trace les actions CREATE, UPDATE, DELETE effectuées via le frontend.
 */
export interface AuditLog {
  id: number;
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  entityType: 'module' | 'sound' | 'user';
  entityId: number | null;
  entityName: string | null;
  entityRole: string | null;
  userLogin: string;
  timestamp: string;
  oldValue: string | null;
  newValue: string | null;
  details: string | null;
}


