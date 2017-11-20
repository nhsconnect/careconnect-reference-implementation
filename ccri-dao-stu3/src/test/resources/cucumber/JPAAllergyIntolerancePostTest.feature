Feature: AllergyIntolerance Integration Test
  As a client FHIR system

        Scenario: AllergyIntolerance Patient
            Given I have one AllergyIntolerance resource loaded
            When I search AllergyIntolerance on Patient ID = 1
            Then I should get a Bundle of AllergyIntolerance 2 resource

        Scenario: AllergyIntolerance Patient Conditional
            Given I have one AllergyIntolerance resource loaded
            When I update this AllergyIntolerance
            Then I search AllergyIntolerance on Patient ID = 1
            Then I should get a Bundle of AllergyIntolerance 1 resource

