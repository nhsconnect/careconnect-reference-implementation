Feature: Immunisation Integration Test
  As a client FHIR system

        Scenario: Immunisation Patient
            Given I have one Immunisation resource loaded
            When I search Immunisation on Patient ID = 2
            Then I should get a Bundle of Immunisation 1 resource
            And the results should be a list of CareConnect Immunizations

        Scenario: Immunisation Patient Conditional
            Given I have one Immunisation resource loaded
            When I update this Immunisation
            Then I search Immunisation on Patient ID = 2
            Then I should get a Bundle of Immunisation 0 resource
            Then I search Immunisation on Patient ID = 3
            Then I should get a Bundle of Immunisation 1 resource
            And the results should be a list of CareConnect Immunizations

