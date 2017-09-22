Feature: Location Integration Test
  As a client FHIR system
  I want to search for a Locations

        Scenario: Location Search by SDS Code
            Given I search for Locations by SDSCode RTG08
            Then the result should be a Location list with 1 entry
            And they shall all be FHIR Location resources
            And the results should be a list of CareConnect Locations

        Scenario: Location Search by SDS Code
            Given I search for Locations by SDSCode 12RTG081010
            Then the result should be a Location list with 0 entry

        Scenario: Location Search by Name
            Given I search for Locations by name Long
            Then the result should be a Location list with 1 entry
            And they shall all be FHIR Location resources
            And the results should be a list of CareConnect Locations

       Scenario: Location Search by Name Mixed case
                Given I search for Locations by name LONG
                Then the result should be a Location list with 1 entry
                And they shall all be FHIR Location resources
                And the results should be a list of CareConnect Locations

        Scenario: Location Search by Name
            Given I search for Locations by name xanadu
            Then the result should be a Location list with 0 entry

         Scenario: Location Load
                   Given Location resource file
                   Then save the location




