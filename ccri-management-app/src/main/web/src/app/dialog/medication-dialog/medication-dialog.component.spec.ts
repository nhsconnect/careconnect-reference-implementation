import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MedicationDialogComponent } from './medication-dialog.component';

describe('MedicationDialogComponent', () => {
  let component: MedicationDialogComponent;
  let fixture: ComponentFixture<MedicationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MedicationDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MedicationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
