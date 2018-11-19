import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReferralRequestComponent } from './referral-request.component';

describe('ReferralRequestComponent', () => {
  let component: ReferralRequestComponent;
  let fixture: ComponentFixture<ReferralRequestComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReferralRequestComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReferralRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
