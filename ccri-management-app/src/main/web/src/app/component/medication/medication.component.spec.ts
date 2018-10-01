import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MedicationComponent } from './medication.component';

describe('MedicationComponent', () => {
  let component: MedicationComponent;
  let fixture: ComponentFixture<MedicationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MedicationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MedicationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
