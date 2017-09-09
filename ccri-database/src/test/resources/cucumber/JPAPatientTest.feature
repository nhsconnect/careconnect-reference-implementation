Feature: Patient


  Scenario: Patient Search by NHS Number
    Given I add a Patient with NHS Number 9000000157
    Then the result should be a valid FHIR Bundle
    And the results should be valid CareConnect Profiles

