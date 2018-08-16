Feature: QuestionnaireResponse Integration Test
  As a client FHIR system

        Scenario: QuestionnaireResponse Patient
            Given I have one QuestionnaireResponse resource loaded
            When I search QuestionnaireResponse on Patient ID = 1
            Then I should get a Bundle of QuestionnaireResponse 1 resource



