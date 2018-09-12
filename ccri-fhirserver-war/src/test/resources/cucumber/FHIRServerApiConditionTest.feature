Feature: Phase 1 Exemplar Test
  As a client FHIR application
  I want to search for Patients




  Scenario: Condition Search by greater than date
    Given Condition Search by gt2017-10-18
    Then the result should be a valid FHIR Bundle
    And the results should be valid CareConnect Profiles

  Scenario: Condition Search by greater equal date
    Given Condition Search by ge2017-10-18
    Then the result should be a valid FHIR Bundle
    And the results should be valid CareConnect Profiles

