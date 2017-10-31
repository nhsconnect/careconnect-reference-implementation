Feature: Method Tests


    Scenario: 3.1.1 Method Test
        Given FHIR STU3 Server
        When I Delete Patient/1001
        Then the method response code should be 400

    Scenario: 3.1.2 Method Test
        Given FHIR STU3 Server
        When I Get Patient/1001
        Then the method response code should be 200

    Scenario: 3.1.3 Method Test
        Given FHIR STU3 Server
        When I Get Patient?_id=1001
        Then the method response code should be 400

    Scenario: 3.1.4 Method Test
        Given FHIR STU3 Server
        When I Get Patient?dummy
        Then the method response code should be 400

    Scenario: 3.1.5 Method Test
        Given FHIR STU3 Server
        When I Get Patient/2010
        Then the method response code should be 404

    Scenario: 3.1.7 Method Test
        Given FHIR STU3 Server
        When I Get Patient/1001/_history/1
        Then the method response code should be 400

    Scenario: 3.1.8 Method Test
       Given FHIR STU3 Server
       When I Head Patient/1001
       Then the method response code should be 400

    Scenario: 3.1.9 Method Test
       Given FHIR STU3 Server
       When I Patch Patient/2010
       | <Patient xmlns="http://hl7.org/fhir"><identifier><system value="http://acme.org/mrns"/><value value="12345"/></identifier><name><family value="Jameson"/><given value="J"/><given value="Jonah"/></name><gender value="male"/></Patient> |
       Then the method response code should be 400

    Scenario: 3.1.10 Method Test
       Given FHIR STU3 Server
       When I Post Patient/2010
       | <Patient xmlns="http://hl7.org/fhir"><identifier><system value="http://acme.org/mrns"/><value value="12345"/></identifier><name><family value="Jameson"/><given value="J"/><given value="Jonah"/></name><gender value="male"/></Patient> |
       Then the method response code should be 400

    Scenario: 3.1.11 Method Test
        Given FHIR STU3 Server
        When I Post Patient/_search?family=Munoz
        Then the method response code should be 200

    Scenario: 3.1.12 Method Test
       Given FHIR STU3 Server
       When I Put Patient/1001
       | <Patient xmlns="http://hl7.org/fhir"><identifier><system value="http://acme.org/mrns"/><value value="12345"/></identifier><name><family value="Jameson"/><given value="J"/><given value="Jonah"/></name><gender value="male"/></Patient> |
       Then the method response code should be 400