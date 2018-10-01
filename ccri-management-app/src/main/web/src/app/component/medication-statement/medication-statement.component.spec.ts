import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MedicationStatementComponent } from './medication-statement.component';

describe('MedicationStatementComponent', () => {
  let component: MedicationStatementComponent;
  let fixture: ComponentFixture<MedicationStatementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MedicationStatementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MedicationStatementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
