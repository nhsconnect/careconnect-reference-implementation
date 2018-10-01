import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PractitionerDialogComponent } from './encounter-dialog.component';

describe('PractitionerRoleDialogComponent', () => {
  let component: PractitionerDialogComponent;
  let fixture: ComponentFixture<PractitionerDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PractitionerDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PractitionerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
