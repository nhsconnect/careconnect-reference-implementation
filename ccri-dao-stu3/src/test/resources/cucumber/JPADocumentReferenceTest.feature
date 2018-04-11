Feature: DocumentReference Integration Test
  As a client FHIR system

        Scenario: DocumentReference Patient
            Given I have one DocumentReference resource loaded
            When I get DocumentReference ID = 1
            Then I should get a DocumentReference resource



