import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EdEncounterListComponent } from './ed-encounter-list.component';

describe('EdEncounterListComponent', () => {
  let component: EdEncounterListComponent;
  let fixture: ComponentFixture<EdEncounterListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EdEncounterListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EdEncounterListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
