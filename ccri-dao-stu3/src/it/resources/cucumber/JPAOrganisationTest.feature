Feature: Organisation Integration Test
  As a client FHIR system
  I want to search for a Organisations

        Scenario: Organisation Search by SDS Code
            Given I search for Organisations by SDSCode C81010
            Then the result should be a organisation list with 1 entry
            And they shall all be FHIR Organization resources
            And the results should be a list of CareConnect Organisations

        Scenario: Organisation Search by SDS Code
            Given I search for Organisations by SDSCode 12345681010
            Then the result should be a organisation list with 0 entry

        Scenario: Organisation Search by Name
            Given I search for Organisations by name the
            Then the result should be a organisation list with 1 entry
            And they shall all be FHIR Organization resources
            And the results should be a list of CareConnect Organisations

       Scenario: Organisation Search by Name Mixed case
                Given I search for Organisations by name ThE
                Then the result should be a organisation list with 1 entry
                And they shall all be FHIR Organization resources
                And the results should be a list of CareConnect Organisations

        Scenario: Organisation Search by Name
            Given I search for Organisations by name xanadu
            Then the result should be a organisation list with 0 entry



