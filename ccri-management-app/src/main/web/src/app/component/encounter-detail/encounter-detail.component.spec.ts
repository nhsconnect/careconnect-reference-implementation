import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EncounterDetailComponent } from './encounter-detail.component';

describe('EncounterDetailComponent', () => {
  let component: EncounterDetailComponent;
  let fixture: ComponentFixture<EncounterDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EncounterDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EncounterDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
