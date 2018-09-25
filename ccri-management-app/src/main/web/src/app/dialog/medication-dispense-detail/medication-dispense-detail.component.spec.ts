import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MedicationDispenseDetailComponent } from './medication-dispense-detail.component';

describe('MedicationDispenseDetailComponent', () => {
  let component: MedicationDispenseDetailComponent;
  let fixture: ComponentFixture<MedicationDispenseDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MedicationDispenseDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MedicationDispenseDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
