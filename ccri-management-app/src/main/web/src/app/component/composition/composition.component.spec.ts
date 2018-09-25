import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FindDocumentItemComponent } from './composition.component';

describe('FindDocumentItemComponent', () => {
  let component: FindDocumentItemComponent;
  let fixture: ComponentFixture<FindDocumentItemComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FindDocumentItemComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FindDocumentItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
