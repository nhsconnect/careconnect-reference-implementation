Feature: Encounter Integration Test
  As a client FHIR system

        Scenario: Encounter Patient
            Given I have one Encounter resource loaded
            When I search Encounter on Patient ID = 1
            Then I should get a Bundle of Encounter 1 resource

