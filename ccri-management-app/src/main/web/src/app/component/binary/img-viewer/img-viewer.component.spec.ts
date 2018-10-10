import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ImgViewerComponent } from './img-viewer.component';

describe('ImgViewerComponent', () => {
  let component: ImgViewerComponent;
  let fixture: ComponentFixture<ImgViewerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ImgViewerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ImgViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
