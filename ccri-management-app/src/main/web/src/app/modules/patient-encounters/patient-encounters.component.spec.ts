import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientEncountersComponent } from './patient-encounters.component';

describe('PatientEncountersComponent', () => {
  let component: PatientEncountersComponent;
  let fixture: ComponentFixture<PatientEncountersComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PatientEncountersComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientEncountersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
