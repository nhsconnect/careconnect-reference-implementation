import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionnaireResponseItemComponent } from './questionnaire-response-item.component';

describe('QuestionnaireResponseItemComponent', () => {
  let component: QuestionnaireResponseItemComponent;
  let fixture: ComponentFixture<QuestionnaireResponseItemComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ QuestionnaireResponseItemComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QuestionnaireResponseItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
