Feature: RiskAssessment Integration Test
  As a client FHIR system

        Scenario: RiskAssessment Patient
            Given I have one RiskAssessment resource loaded
            When I search RiskAssessment on Patient ID = 1
            Then I should get a Bundle of RiskAssessment 1 resource



