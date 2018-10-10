import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BinaryComponent } from './binary.component';

describe('BinaryComponent', () => {
  let component: BinaryComponent;
  let fixture: ComponentFixture<BinaryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BinaryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BinaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
