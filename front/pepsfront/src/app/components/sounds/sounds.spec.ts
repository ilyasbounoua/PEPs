import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Sounds } from './sounds';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';
import { AudioService } from '../../services/audio';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Sound } from '../../models/interfaces';
import { signal } from '@angular/core';

class MockApiService {
  getSounds(role?: string) {
    const sounds: Sound[] = [
      { id: 1, name: 'Sound 1', type: 'Vocal', extension: 'mp3', fileName: 'sound1.mp3' },
      { id: 2, name: 'Sound 2', type: 'Ambiance', extension: 'wav', fileName: 'sound2.wav' },
    ];
    return of(sounds);
  }
  getRoles() {
    return of(['dauphin', 'aras']);
  }
  updateSound(id: number, data: { name: string, type: string }) {
    const updatedSound: Sound = { id, ...data, extension: 'mp3', fileName: 'sound1.mp3' };
    return of(updatedSound);
  }
  deleteSound(id: number) {
    return of(undefined);
  }
  getSoundFileUrl(id: number) {
    return `url/${id}`;
  }
}

class MockAudioService {
  currentlyPlayingId = signal(null);
  playSound(url: string, id: number) {}
}

describe('Sounds', () => {
  let component: Sounds;
  let fixture: ComponentFixture<Sounds>;
  let apiService: ApiService;
  let audioService: AudioService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sounds],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        { provide: AuthService, useValue: { isAdmin: () => false, canEdit: () => true } },
        { provide: AudioService, useClass: MockAudioService },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Sounds);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
    audioService = TestBed.inject(AudioService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load sounds on init', () => {
    spyOn(apiService, 'getSounds').and.callThrough();
    component.ngOnInit();
    expect(apiService.getSounds).toHaveBeenCalled();
    expect(component.sounds().length).toBe(2);
  });

  it('should filter sounds', () => {
    component.setSoundFilter('Vocal');
    expect(component.filteredSounds().length).toBe(1);
    component.setSoundFilter('all');
    expect(component.filteredSounds().length).toBe(2);
  });

  it('should switch view mode', () => {
    component.openAddPage();
    expect(component.viewMode()).toBe('add');
    component.closeAddPage(false);
    expect(component.viewMode()).toBe('list');
  });

  it('should play sound', () => {
    spyOn(audioService, 'playSound');
    const sound = component.sounds()[0];
    component.playSound(sound);
    expect(audioService.playSound).toHaveBeenCalled();
  });

  it('should delete sound', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(apiService, 'deleteSound').and.callThrough();
    const sound = component.sounds()[0];
    component.deleteSound(sound);
    expect(apiService.deleteSound).toHaveBeenCalledWith(sound.id);
  });

  it('should edit sound', () => {
    spyOn(apiService, 'updateSound').and.callThrough();
    const sound = component.sounds()[0];
    component.startEditSound(sound);
    expect(component.editingSoundId()).toBe(sound.id);

    component.updateEditSoundName('New Name');
    expect(component.editSoundData().name).toBe('New Name');
    
    component.saveEditSound(sound.id);
    expect(apiService.updateSound).toHaveBeenCalled();
    expect(component.editingSoundId()).toBe(null);
  });
});
