Feature: Observation Integration Test
  As a client FHIR system



        Scenario: Observation Search Patient
            Given I have two sample resources loaded
            When I search on Patient ID = 1
            Then I should get a Bundle of Observations with more then 2 resources

