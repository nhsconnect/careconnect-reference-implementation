Feature: Phase 1 Exemplar Test
  As a receptionist
  I want to search for a Patient by NHS Number


  Scenario: Patient Search by NHS Number
    Given I search for a Patient by NHS Number 9000000157
    Then the result should be a valid FHIR Bundle
    And the results should be valid CareConnect Profiles


   Scenario: Organization Search by ODS Code
      Given I search for a Organisation by ODS Code R1A17
      Then the result should be a valid FHIR Bundle
      And the results should be valid CareConnect Profiles

    Scenario: Practitioner Search by GMP/GMC Code
         Given I search for a Practitioner by SDS User Id G13579135
         Then the result should be a valid FHIR Bundle
         And the results should be valid CareConnect Profiles