import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SmartAppsComponent } from './smart-apps.component';

describe('SmartAppsComponent', () => {
  let component: SmartAppsComponent;
  let fixture: ComponentFixture<SmartAppsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SmartAppsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SmartAppsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
