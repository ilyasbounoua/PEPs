import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { SoundAddComponent } from './sound-add';
import { ApiService } from '../../../services/api';
import { AuthService } from '../../../services/auth';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Sound } from '../../../models/interfaces';

class MockApiService {
  uploadSound(formData: FormData, role?: string) {
    return of({ id: 1, name: 'New Sound', type: 'Vocal', extension: 'mp3', fileName: 'new.mp3' });
  }
  getRoles() {
    return of(['dauphin', 'aras']);
  }
}

describe('SoundAddComponent as a regular user', () => {
  let component: SoundAddComponent;
  let fixture: ComponentFixture<SoundAddComponent>;
  let apiService: ApiService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SoundAddComponent],
      providers: [
        { provide: ApiService, useClass: MockApiService },
        { provide: AuthService, useValue: { isAdmin: () => false } },
        provideNoopAnimations(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SoundAddComponent);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not load roles on init', () => {
    spyOn(apiService, 'getRoles').and.callThrough();
    component.ngOnInit();
    expect(apiService.getRoles).not.toHaveBeenCalled();
  });

  it('should update sound properties', () => {
    component.updateSoundName('New Sound');
    expect(component.newSound().name).toBe('New Sound');
    component.updateSoundType('Vocal');
    expect(component.newSound().type).toBe('Vocal');
  });

  it('should handle file selection', () => {
    const file = new File([''], 'test.mp3', { type: 'audio/mpeg' });
    const event = { target: { files: [file] } } as any;
    component.onFileSelected(event);
    expect(component.newSound().file).toBe(file);
    expect(component.uploadError()).toBe('');
  });

  it('should show error for invalid file format', () => {
    const file = new File([''], 'test.txt', { type: 'text/plain' });
    const event = { target: { files: [file] } } as any;
    component.onFileSelected(event);
    expect(component.newSound().file).toBe(null);
    expect(component.uploadError()).toBe('Format non supporté (mp3, wav, ogg, m4a)');
  });

  it('should upload sound and emit success', () => {
    spyOn(apiService, 'uploadSound').and.callThrough();
    spyOn(component.soundAdded, 'emit');
    component.updateSoundName('New Sound');
    component.updateSoundType('Vocal');
    component.newSound.update(s => ({ ...s, file: new File([''], 'test.mp3') }));
    component.uploadSound();
    expect(apiService.uploadSound).toHaveBeenCalled();
    expect(component.soundAdded.emit).toHaveBeenCalled();
  });

  it('should show error on upload without name', () => {
    component.updateSoundType('Vocal');
    component.newSound.update(s => ({ ...s, file: new File([''], 'test.mp3') }));
    component.uploadSound();
    expect(component.uploadError()).toBe('Nom obligatoire');
  });

  it('should emit cancel event', () => {
    spyOn(component.cancel, 'emit');
    component.onCancel();
    expect(component.cancel.emit).toHaveBeenCalled();
  });
});

describe('SoundAddComponent as an admin', () => {
    let component: SoundAddComponent;
    let fixture: ComponentFixture<SoundAddComponent>;
    let apiService: ApiService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SoundAddComponent],
            providers: [
                { provide: ApiService, useClass: MockApiService },
                { provide: AuthService, useValue: { isAdmin: () => true } },
                provideNoopAnimations(),
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(SoundAddComponent);
        component = fixture.componentInstance;
        apiService = TestBed.inject(ApiService);
        fixture.detectChanges();
    });

    it('should load roles on init', () => {
        spyOn(apiService, 'getRoles').and.callThrough();
        component.ngOnInit();
        expect(apiService.getRoles).toHaveBeenCalled();
        expect(component.profiles.length).toBe(2);
    });

    it('should show error on upload without profile', () => {
        component.updateSoundName('New Sound');
        component.updateSoundType('Vocal');
        component.newSound.update(s => ({ ...s, file: new File([''], 'test.mp3') }));
        component.uploadSound();
        expect(component.uploadError()).toBe('Profil cible obligatoire');
    });
});
