import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientSearchItemComponent } from './patient.component';

describe('PatientSearchItemComponent', () => {
  let component: PatientSearchItemComponent;
  let fixture: ComponentFixture<PatientSearchItemComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PatientSearchItemComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientSearchItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
