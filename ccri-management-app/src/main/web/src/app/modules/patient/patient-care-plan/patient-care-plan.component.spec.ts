import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientCarePlanComponent } from './patient-care-plan.component';

describe('PatientCarePlanComponent', () => {
  let component: PatientCarePlanComponent;
  let fixture: ComponentFixture<PatientCarePlanComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PatientCarePlanComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientCarePlanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
