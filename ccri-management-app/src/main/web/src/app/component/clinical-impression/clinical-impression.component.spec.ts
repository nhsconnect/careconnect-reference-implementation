import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ClinicalImpressionComponent } from './clinical-impression.component';

describe('ClinicalImpressionComponent', () => {
  let component: ClinicalImpressionComponent;
  let fixture: ComponentFixture<ClinicalImpressionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ClinicalImpressionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ClinicalImpressionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
