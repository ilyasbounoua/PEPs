import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SoundAdd } from './sound-add';

describe('SoundAdd', () => {
  let component: SoundAdd;
  let fixture: ComponentFixture<SoundAdd>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SoundAdd]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SoundAdd);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
