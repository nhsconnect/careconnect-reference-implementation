Feature: Patient Search Tests


    Scenario: 4.1.1 Search Test
        Given FHIR STU3 Server
        When I Get Patient?address-postalcode=LS
        Then the method response code should be 200
        And have 99 Patient's returned