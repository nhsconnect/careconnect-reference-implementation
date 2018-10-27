import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientMedicationComponent } from './patient-medication.component';

describe('PatientMedicationComponent', () => {
  let component: PatientMedicationComponent;
  let fixture: ComponentFixture<PatientMedicationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PatientMedicationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientMedicationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
