import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AmbulanceATMISTComponent } from './ambulance-atmist.component';

describe('AmbulanceATMISTComponent', () => {
  let component: AmbulanceATMISTComponent;
  let fixture: ComponentFixture<AmbulanceATMISTComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AmbulanceATMISTComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AmbulanceATMISTComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
