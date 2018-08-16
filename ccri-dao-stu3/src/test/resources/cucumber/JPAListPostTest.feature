Feature: List Integration Test
  As a client FHIR system

        Scenario: List Patient
            Given I have one List resource loaded
            When I search List on Patient ID = 1
            Then I should get a Bundle of List 1 resource



