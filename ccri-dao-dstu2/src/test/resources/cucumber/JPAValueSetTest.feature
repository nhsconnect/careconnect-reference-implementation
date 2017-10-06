Feature: ValueSet


Scenario: ValueSet Read
        Given I add a ValueSet with an Id of CareConnect-EthnicCategory-1
        Then the result should be a FHIR ValueSet

Scenario: ValueSet Read
        Given I add a ValueSet with an Id of CareConnect-AllergySeverity-1
        Then the result should be a FHIR ValueSet