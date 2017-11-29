Feature: MedicationRequest Integration Test
  As a client FHIR system

        Scenario: MedicationRequest Patient
            Given I have one MedicationRequest resource loaded
            When I search MedicationRequest on Patient ID = 2
            Then I should get a Bundle of MedicationRequest 1 resource
            And the results should be a list of CareConnect MedicationRequests

        Scenario: MedicationRequest Patient Conditional
            Given I have one MedicationRequest resource loaded
            When I update this MedicationRequest
            Then I search MedicationRequest on Patient ID = 2
            Then I should get a Bundle of MedicationRequest 0 resource
            Then I search MedicationRequest on Patient ID = 3
            Then I should get a Bundle of MedicationRequest 1 resource
            And the results should be a list of CareConnect MedicationRequests

