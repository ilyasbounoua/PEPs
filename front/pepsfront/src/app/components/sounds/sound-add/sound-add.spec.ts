import { ComponentFixture, TestBed } from '@angular/core/testing';

// 1. On importe le bon nom de classe
import { SoundAddComponent } from './sound-add'; 

describe('SoundAddComponent', () => {
  // 2. On met à jour les types ici
  let component: SoundAddComponent;
  let fixture: ComponentFixture<SoundAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      // 3. On met à jour l'import du module standalone
      imports: [SoundAddComponent] 
    })
    .compileComponents();

    // 4. On crée le composant avec la bonne classe
    fixture = TestBed.createComponent(SoundAddComponent); 
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});