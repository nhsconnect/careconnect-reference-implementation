import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CarePlanComponent } from './care-plan.component';

describe('CarePlanComponent', () => {
  let component: CarePlanComponent;
  let fixture: ComponentFixture<CarePlanComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CarePlanComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CarePlanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
