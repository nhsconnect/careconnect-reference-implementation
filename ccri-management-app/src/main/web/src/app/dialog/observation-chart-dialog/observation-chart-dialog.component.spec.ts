import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ObservationChartDialogComponent } from './observation-chart-dialog.component';

describe('ObservationChartDialogComponent', () => {
  let component: ObservationChartDialogComponent;
  let fixture: ComponentFixture<ObservationChartDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ObservationChartDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ObservationChartDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
