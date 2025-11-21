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
export type PageType = 'dashboard' | 'interactions' | 'modules' | 'module-detail' | 'add-module' | 'sounds';
