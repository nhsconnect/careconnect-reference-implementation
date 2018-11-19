import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientReferralRequestComponent } from './patient-referral-request.component';

describe('PatientReferralRequestComponent', () => {
  let component: PatientReferralRequestComponent;
  let fixture: ComponentFixture<PatientReferralRequestComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PatientReferralRequestComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PatientReferralRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
