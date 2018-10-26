import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientImmunisationComponent } from './patient-immunisation.component';

describe('PatientImmunisationComponent', () => {
  let component: PatientImmunisationComponent;
  let fixture: ComponentFixture<PatientImmunisationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PatientImmunisationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientImmunisationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
