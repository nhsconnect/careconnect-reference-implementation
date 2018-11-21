import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MedicationAdministrationComponent } from './medication-administration.component';

describe('MedicationAdministrationComponent', () => {
  let component: MedicationAdministrationComponent;
  let fixture: ComponentFixture<MedicationAdministrationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MedicationAdministrationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MedicationAdministrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
