Feature: Phase 1 Exemplar Test
  As a client FHIR application
  I want to search for Patients


  Scenario: Patient Search by familyName
    Given Patient Search by familyName kanfeld
    Then the result should be a valid FHIR Bundle

Scenario: Patient GET Patient 1
    Given I Patient GET Patient 1
    Then the result should be a valid FHIR Patient



