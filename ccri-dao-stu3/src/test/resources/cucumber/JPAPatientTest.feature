Feature: Patient


Scenario: Patient Read
        Given I add a Patient with an Id of 1
        Then the result should be a FHIR Patient
        And the results should be a CareConnect Patient

  Scenario: Patient first name Search Found
        Given I search for a Patient with a family name of Kanfeld
        Then the result should be a list with 1 entry
        And they shall all be FHIR Patient resources
        And the results should be a list of CareConnect Patients

  Scenario: Patient first name mixed case Search Found
        Given I search for a Patient with a family name of KanFelD
        Then the result should be a list with 1 entry
        And they shall all be FHIR Patient resources
        And the results should be a list of CareConnect Patients

   Scenario: Patient first name Search NOT Found
         Given I search for a Patient with a family name of Schmidt
         Then the result should be a list with 0 entry

   Scenario: Patient first name Search Found
         Given I search for a Patient with a given name of Bernie
         Then the result should be a list with 1 entry
         And they shall all be FHIR Patient resources
         And the results should be a list of CareConnect Patients

   Scenario: Patient first name Search NOT Found
         Given I search for a Patient with a given name of Eric
         Then the result should be a list with 0 entry


    Scenario: Patient birthdate name Search Found '1998-03-19'
         Given I search for a Patient with a birthdate of '1998-03-19'
         Then the result should be a list with 1 entry
         And they shall all be FHIR Patient resources
         And the results should be a list of CareConnect Patients



     Scenario: Patient birthdate Search NOT Found
             Given I search for a Patient with a birthdate of '1918-02-17'
             Then the result should be a list with 0 entry


   Scenario: Patient gender Search Female
             Given I search for a Patient with a gender of FEMALE
             Then the result should be a list with several entries
             And they shall all be FHIR Patient resources
             And the results should be a list of CareConnect Patients

  Scenario: Patient gender Search Male
             Given I search for a Patient with a gender of MALE
             Then the result should be a list with several entries
             And they shall all be FHIR Patient resources
             And the results should be a list of CareConnect Patients


 Scenario: Patient identifier Search Found
             Given I search for a Patient with a NHSNumber of 9876543210
             Then the result should be a list with 1 entry
             And they shall all be FHIR Patient resources
             And the results should be a list of CareConnect Patients

 Scenario: Patient identifier Search Found
              Given I search for a Patient with a NHSNumber of 1234567890
              Then the result should be a list with 0 entry


  Scenario: Patient name (given supplied) Search Found
               Given I search for a Patient with a name of "Bernie"
               Then the result should be a list with 1 entry
               And they shall all be FHIR Patient resources
               And the results should be a list of CareConnect Patients

    Scenario: Patient name (family supplied) Search Found
                 Given I search for a Patient with a name of "Kanfeld"
                 Then the result should be a list with 1 entry
                 And they shall all be FHIR Patient resources
                 And the results should be a list of CareConnect Patients

   Scenario: Patient name Search NOT Found
                 Given I search for a Patient with a name of "Loki"
                 Then the result should be a list with 0 entry

   Scenario: Patient email Search Found
                 Given I search for a Patient with a email of "bernie.kanfeld@nhsdigital.nhs.uk"
                 Then the result should be a list with 1 entry

    Scenario: Patient POSTCODE Search Found
                  Given I search for a Patient with a address-postcode of "NG10 1ZZ"
                  Then the result should be a list with 1 entry

   Scenario: Patient POSTCODE Part Search Found
                     Given I search for a Patient with a address-postcode of "NG10"
                     Then the result should be a list with 1 entry

    Scenario: Patient PHONE Search Found
                  Given I search for a Patient with a phone of "0115 9737320"
                  Then the result should be a list with 1 entry

    Scenario: Patient email Search NOT Found
                 Given I search for a Patient with a email of "kevin.mayfield@airelogic.com"
                 Then the result should be a list with 0 entry

    Scenario: Patient POSTCODE Search NOT Found
                  Given I search for a Patient with a address-postcode of "LS15 8FR"
                  Then the result should be a list with 0 entry

    Scenario: Patient PHONE Search NOT Found
                  Given I search for a Patient with a phone of "0113 9737320"
                  Then the result should be a list with 0 entry

  Scenario:  Patient Conditional

    When I Conditional add a Patient
    Then I search Patient on Patient PPMID = 1101
    Then I should get a Bundle of Patient 1 resource