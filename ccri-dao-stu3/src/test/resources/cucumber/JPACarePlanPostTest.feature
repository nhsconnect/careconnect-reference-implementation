Feature: CarePlan Integration Test
  As a client FHIR system

        Scenario: CarePlan Patient
            Given I have one CarePlan resource loaded
            When I search CarePlan on Patient ID = 1
            Then I should get a Bundle of CarePlan 1 resource


