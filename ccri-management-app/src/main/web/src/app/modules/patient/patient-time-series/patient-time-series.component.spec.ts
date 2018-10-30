import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientTimeSeriesComponent } from './patient-time-series.component';

describe('PatientTimeSeriesComponent', () => {
  let component: PatientTimeSeriesComponent;
  let fixture: ComponentFixture<PatientTimeSeriesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PatientTimeSeriesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientTimeSeriesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
