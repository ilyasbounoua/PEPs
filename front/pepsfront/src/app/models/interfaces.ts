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
export type PageType = 'dashboard' | 'interactions' | 'modules' | 'module-detail' | 'add-module' | 'sounds' | 'users' | 'account';

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
  role: 'admin' | 'dauphin' | 'aras';
  enabled: boolean;
}

/**
 * Données pour créer un nouvel utilisateur.
 */
export interface CreateUserDTO {
  login: string;
  password: string;
  role: 'admin' | 'dauphin' | 'aras';
}

/**
 * Données pour modifier un utilisateur existant.
 * Tous les champs sont optionnels.
 */
export interface UpdateUserDTO {
  login?: string;
  password?: string;
  role?: 'admin' | 'dauphin' | 'aras';
}

