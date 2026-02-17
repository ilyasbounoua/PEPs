import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http'; 
import { provideHttpClientTesting } from '@angular/common/http/testing'; 
import { SoundAddComponent } from './sound-add';

describe('SoundAddComponent', () => {
  let component: SoundAddComponent;
  let fixture: ComponentFixture<SoundAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SoundAddComponent],
      providers: [
        provideHttpClient(), // 3. On fournit le HttpClient
        provideHttpClientTesting() // 4. On fournit l'outil de test associé
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SoundAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});