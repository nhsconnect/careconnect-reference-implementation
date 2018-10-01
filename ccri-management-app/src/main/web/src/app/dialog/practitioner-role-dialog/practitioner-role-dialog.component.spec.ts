import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PractitionerRoleDialogComponent } from './practitioner-role-dialog.component';

describe('PractitionerRoleDialogComponent', () => {
  let component: PractitionerRoleDialogComponent;
  let fixture: ComponentFixture<PractitionerRoleDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PractitionerRoleDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PractitionerRoleDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
