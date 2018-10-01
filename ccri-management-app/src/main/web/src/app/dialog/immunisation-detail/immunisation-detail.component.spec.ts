import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ImmunisationDetailComponent } from './immunisation-detail.component';

describe('ImmunisationDetailComponent', () => {
  let component: ImmunisationDetailComponent;
  let fixture: ComponentFixture<ImmunisationDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ImmunisationDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ImmunisationDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
