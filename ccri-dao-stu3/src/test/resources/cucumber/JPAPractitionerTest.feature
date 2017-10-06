Feature: Practitioner Integration Test
  As a client FHIR system
  I want to search for a Practitioners

        Scenario: Practitioner Search by SDS Code
            Given I search for Practitioners by SDSId G8133438
            Then the result should be a practitioner list with 1 entry
            And they shall all be FHIR Practitioner resources
            And the results should be a list of CareConnect Practitioners

        Scenario: Practitioner Search by SDS Code
            Given I search for Practitioners by SDSId S8133438
            Then the result should be a practitioner list with 0 entry

        Scenario: Practitioner Search by Name
            Given I search for Practitioners by name Bhatia
            Then the result should be a practitioner list with 1 entry
            And they shall all be FHIR Practitioner resources
            And the results should be a list of CareConnect Practitioners

       Scenario: Practitioner Search by Name Mixed case
                Given I search for Practitioners by name BHATIA
                Then the result should be a practitioner list with 1 entry
                And they shall all be FHIR Practitioner resources
                And the results should be a list of CareConnect Practitioners

        Scenario: Practitioner Search by Name
            Given I search for Practitioners by name xanadu
            Then the result should be a practitioner list with 0 entry



