import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MedicationDispenseComponent } from './medication-dispense.component';

describe('MedicationDispenseComponent', () => {
  let component: MedicationDispenseComponent;
  let fixture: ComponentFixture<MedicationDispenseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MedicationDispenseComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MedicationDispenseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
