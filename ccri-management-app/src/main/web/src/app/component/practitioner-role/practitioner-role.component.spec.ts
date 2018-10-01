import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PractitionerRoleComponent } from './practitioner-role.component';

describe('PractitionerRoleComponent', () => {
  let component: PractitionerRoleComponent;
  let fixture: ComponentFixture<PractitionerRoleComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PractitionerRoleComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PractitionerRoleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
