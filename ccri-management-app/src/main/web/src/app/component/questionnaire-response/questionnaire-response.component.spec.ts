import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionnaireResponseComponent } from './questionnaire-response.component';

describe('QuestionnaireResponseComponent', () => {
  let component: QuestionnaireResponseComponent;
  let fixture: ComponentFixture<QuestionnaireResponseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QuestionnaireResponseComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QuestionnaireResponseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
