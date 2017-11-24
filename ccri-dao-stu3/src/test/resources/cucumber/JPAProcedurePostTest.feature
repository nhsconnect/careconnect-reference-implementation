Feature: Procedure Integration Test
  As a client FHIR system

        Scenario: Procedure Patient
            Given I have one Procedure resource loaded
            When I search Procedure on Patient ID = 1
            Then I should get a Bundle of Procedure 2 resource

        Scenario: Procedure Patient Conditional
            Given I have one Procedure resource loaded
            When I update this Procedure
            Then I search Procedure on Patient ID = 1
            Then I should get a Bundle of Procedure 1 resource

