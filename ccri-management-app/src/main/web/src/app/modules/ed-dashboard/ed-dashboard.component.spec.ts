import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EdDashboardComponent } from './ed-dashboard.component';

describe('EdDashboardComponent', () => {
  let component: EdDashboardComponent;
  let fixture: ComponentFixture<EdDashboardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EdDashboardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EdDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
