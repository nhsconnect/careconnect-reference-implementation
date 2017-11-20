Feature: Condition Integration Test
  As a client FHIR system

        Scenario: Condition Patient
            Given I have one Condition resource loaded
            When I search Condition on Patient ID = 1
            Then I should get a Bundle of Condition 2 resource
            And the results should be a list of CareConnect Conditions

        Scenario: Condition Patient Conditional
            Given I have one Condition resource loaded
            When I update this Condition
            Then I search Condition on Patient ID = 1
            Then I should get a Bundle of Condition 1 resource
            And the results should be a list of CareConnect Conditions
