import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewDocumentSectionComponent } from './view-document-section.component';

describe('ViewDocumentSectionComponent', () => {
  let component: ViewDocumentSectionComponent;
  let fixture: ComponentFixture<ViewDocumentSectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ViewDocumentSectionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewDocumentSectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
