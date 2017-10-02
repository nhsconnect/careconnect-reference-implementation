Feature: Observation Integration Test
  As a client FHIR system


         Scenario: Observation Post
                   Given Observation resource file
                   Then save the Observation


        Scenario: Observation Blood Pressue Post
                   Given Observation a Blood Pressure import
                   Then save the Observation


