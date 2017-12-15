Feature: MedicationStatement Integration Test
  As a client FHIR system

        Scenario: MedicationStatement Patient
            Given I have one MedicationRequest resource loaded
            When I get MedicationStatement ID = 1
            Then I should get a MedicationRequest resource



