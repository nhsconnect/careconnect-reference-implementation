import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ExplorerMainComponent } from './explorer-main.component';

describe('ExplorerMainComponent', () => {
  let component: ExplorerMainComponent;
  let fixture: ComponentFixture<ExplorerMainComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ExplorerMainComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExplorerMainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
