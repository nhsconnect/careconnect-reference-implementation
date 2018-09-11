Feature: Phase 1 Exemplar Test
  As a client FHIR application
  I want to search for Patients


    Scenario: Patient Search by familyName
        Given Patient Search by familyName kanfeld
        Then the result should be a valid FHIR Bundle
        And the results should be valid CareConnect Profiles

    Scenario: Organisation Search by name
        Given Organisation Search by name moir
        Then the result should be a valid FHIR Bundle
        And the results should be valid CareConnect Profiles

     Scenario: Practitioner Search by name
        Given Practitioner Search by name Bhatia
        Then the result should be a valid FHIR Bundle
        And the results should be valid CareConnect Profiles

     Scenario: Location Search by name
        Given Location Search by name Long
        Then the result should be a valid FHIR Bundle
        And the results should be valid CareConnect Profiles



