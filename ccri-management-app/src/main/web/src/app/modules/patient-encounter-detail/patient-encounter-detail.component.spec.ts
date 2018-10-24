import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientEncounterDetailComponent } from './patient-encounter-detail.component';

describe('PatientEncounterDetailComponent', () => {
  let component: PatientEncounterDetailComponent;
  let fixture: ComponentFixture<PatientEncounterDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PatientEncounterDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientEncounterDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
