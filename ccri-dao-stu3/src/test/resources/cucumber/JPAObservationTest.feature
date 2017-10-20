Feature: Observation Integration Test
  As a client FHIR system


         Scenario: Observation Post
               Given Observation resource file
               Then save the Observation

         Scenario: Observation Search Smoking Status Category
            Given I have two sample resources loaded
            When I search Observations on SNOMED category 228272008
            Then I should get a Bundle of Observations with 1 resource

         Scenario: Observation Search BMI Code
             Given I have two sample resources loaded
             When I search Observations on SNOMED code 301331008
             Then I should get a Bundle of Observations with 1 resource

        Scenario: Observation Search Patient
            Given I have two sample resources loaded
            When I search on Patient ID = 1
            Then I should get a Bundle of Observations with more then 2 resources

        Scenario: Observation Search Patient
            Given I have two sample resources loaded
            When I search on dates less than 1999-08-02
            Then I should get a Bundle of Observations with 1 resource
