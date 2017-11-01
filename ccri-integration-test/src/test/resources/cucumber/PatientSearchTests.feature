Feature: N.4. Patient Search Tests (version 0.11)


    Scenario: 4.1.1 Search Test
        Given FHIR STU3 Server
        When I Get Patient?address-postalcode=LS
        Then the method response code should be 200
        And have 99 Patient's returned

    Scenario: 4.1.2 Search Test
        Given FHIR STU3 Server
        When I Get Patient?address-postalcode=LS15 8BD
        Then the method response code should be 200
        And have 1 Patient's returned
        And Patient Id = 1003
        And resource is valid

    Scenario: 4.1.3 Search Test
        Given FHIR STU3 Server
        When I Get Patient?address-postalcode=B689QP
        Then the method response code should be 200
        And have 0 Patient's returned

    Scenario: 4.1.4 Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=1935-09-20
         Then the method response code should be 200
         And have 1 Patient's returned
         And Patient Id = 1001

    Scenario: 4.1.5 Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=1935-09-21
         Then the method response code should be 200
         And have 0 Patient's returned

    Scenario: 4.1.6 Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=1935
         Then the method response code should be 200
         And have 1 Patient's returned

     Scenario: 4.1.7 Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=1917-06
         Then the method response code should be 200
         And have 1 Patient's returned
         And Patient Id = 1038

     Scenario: 4.1.7a Search Test - Invalid parameter
         Given FHIR STU3 Server
         When I Get Patient?birthdate=191
         Then the method response code should be 500

    Scenario: 4.1.8 Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=eq1918-05-20
         Then the method response code should be 200
         And have 1 Patient's returned
         And Patient Id = 1050

    Scenario: 4.1.9 birthdate=ne1918-05-20
         Given FHIR STU3 Server
         When I Get Patient?birthdate=ne1918-05-20
         Then the method response code should be 422



    Scenario: 4.1.10 Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=le1919-07-13
         Then the method response code should be 200
         And have 4 Patient's returned
         And contains Ids
             | PatientId |
             | 1038 |
             | 1050 |
             | 1078 |
             | 1082 |

    Scenario: 4.1.11 Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=gt2015-05-11
         Then the method response code should be 200
         And have 3 Patient's returned
         And contains Ids
             | PatientId |
             | 1046 |
             | 1053 |
             | 1068 |

    Scenario: 4.1.12 Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=ge2015-05-11
         Then the method response code should be 200
         And have 4 Patient's returned
         And contains Ids
             | PatientId |
             | 1045 |
             | 1046 |
             | 1053 |
             | 1068 |

    Scenario: 4.1.13 Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=sa2015-05-11
         Then the method response code should be 422

    Scenario: 4.1.14 Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=eb1919-07-13
         Then the method response code should be 422

    Scenario: 4.1.15 Search Test
         Given FHIR STU3 Server
         When I Get Patient?email=Judy.Welch@again.co.uk
         Then the method response code should be 200
          And have 1 Patient's returned
          And contains Ids
              | PatientId |
              | 1099 |


    Scenario: 4.1.16 Search Test
         Given FHIR STU3 Server
         When I Get Patient?email=Judy
         Then the method response code should be 200
         And have 2 Patient's returned
         And contains Ids
             | PatientId |
             | 1099 |
             | 1055 |


    Scenario: 4.1.17 Search Test
         Given FHIR STU3 Server
         When I Get Patient?family=Munoz
         Then the method response code should be 200
         And have 1 Patient's returned
         And contains Ids
             | PatientId |
             | 1001 |

    Scenario: 4.1.18 Search Test
             Given FHIR STU3 Server
             When I Get Patient?family=Wilson
             Then the method response code should be 200
             And have 0 Patient's returned


   Scenario: 4.1.19 Search Test
            Given FHIR STU3 Server
            When I Get Patient?family=munoz
            Then the method response code should be 200
            And have 1 Patient's returned
            And contains Ids
                | PatientId |
                | 1001 |

   Scenario: 4.1.20 family=Antic (Antić)
            Given FHIR STU3 Server
            When I Get Patient?family=Antic
            Then the method response code should be 200
            And have 1 Patient's returned
            And contains Ids
                | PatientId |
                | 1009 |

   Scenario: 4.1.20a family=Antić
               Given FHIR STU3 Server
               When I Get Patient?family=Antić
               Then the method response code should be 200
               And have 1 Patient's returned
               And contains Ids
                   | PatientId |
                   | 1009 |

   Scenario: 4.1.21 Search Test
            Given FHIR STU3 Server
            When I Get Patient?family=Jessop
            Then the method response code should be 200
            And have 1 Patient's returned
            And contains Ids
                | PatientId |
                | 1012 |

   Scenario: 4.1.22 Search Test
            Given FHIR STU3 Server
            When I Get Patient?family=Mo
            Then the method response code should be 200
            And have 2 Patient's returned
            And contains Ids
                | PatientId |
                | 1032 |
                | 1084 |

   Scenario: 4.1.23 Search Test
           Given FHIR STU3 Server
           When I Get Patient?gender=MALE
           Then the method response code should be 200
           And have 52 Patient's returned

   Scenario: 4.1.24 Search Test
          Given FHIR STU3 Server
          When I Get Patient?gender=FEMALE
          Then the method response code should be 200
          And have 44 Patient's returned

   Scenario: 4.1.25 Search Test
          Given FHIR STU3 Server
          When I Get Patient?gender=OTHER
          Then the method response code should be 200
          And have 7 Patient's returned

   Scenario: 4.1.26 Search Test
          Given FHIR STU3 Server
          When I Get Patient?gender=male
          Then the method response code should be 200
          And have 52 Patient's returned

   Scenario: 4.1.27 Search Test
             Given FHIR STU3 Server
             When I Get Patient?given=Kendra
             Then the method response code should be 200
             And have 1 Patient's returned
             And contains Ids
                 | PatientId |
                 | 1002 |

   Scenario: 4.1.28 Search Test
            Given FHIR STU3 Server
            When I Get Patient?given=John
            Then the method response code should be 200
            And have 0 Patient's returned

   Scenario: 4.1.29 Search Test
            Given FHIR STU3 Server
            When I Get Patient?given=kendra
            Then the method response code should be 200
            And have 1 Patient's returned
            And contains Ids
                 | PatientId |
                 | 1002 |

   Scenario: 4.1.30 given=Lendina (Lëndina)
           Given FHIR STU3 Server
           When I Get Patient?given=Lendina
           Then the method response code should be 200
           And have 1 Patient's returned
           And contains Ids
                | PatientId |
                | 1043 |

   Scenario: 4.1.30a given=Lëndina
              Given FHIR STU3 Server
              When I Get Patient?given=Lëndina
              Then the method response code should be 200
              And have 1 Patient's returned
              And contains Ids
                   | PatientId |
                   | 1043 |

   Scenario: 4.1.31 given=Jet
              Given FHIR STU3 Server
              When I Get Patient?given=Jet
              Then the method response code should be 200
              And have 1 Patient's returned
              And contains Ids
                   | PatientId |
                   | 1090 |

   Scenario: 4.1.32 given=Vic
              Given FHIR STU3 Server
              When I Get Patient?given=Vic
              Then the method response code should be 200
              And have 2 Patient's returned
              And contains Ids
                   | PatientId |
                   | 1001 |
                   | 1058 |

   Scenario: 4.1.33 Search Test
                 Given FHIR STU3 Server
                 When I Get Patient?identifier=9002090358
                 Then the method response code should be 200
                 And have 1 Patient's returned
                 And contains Ids
                      | PatientId |
                      | 1011 |

   Scenario: 4.1.34 Search Test
                Given FHIR STU3 Server
                When I Get Patient?identifier=8678768768
                Then the method response code should be 200
                And have 0 Patient's returned

   Scenario: 4.1.35 Search Test
               Given FHIR STU3 Server
               When I Get Patient?identifier=https://fhir.nhs.uk/Id/nhs-number|9002090358
               Then the method response code should be 200
               And have 1 Patient's returned
               And contains Ids
                    | PatientId |
                    | 1011 |

   Scenario: 4.1.36 Search Test
                  Given FHIR STU3 Server
                  When I Get Patient?identifier=https://fhir.leedsth.nhs.uk/Id/pas-number|9002090358
                  Then the method response code should be 200
                  And have 0 Patient's returned

   Scenario: 4.1.37 Search Test
                     Given FHIR STU3 Server
                     When I Get Patient?identifier=https://dummy.co.uk/invalidaddress|9847680167
                     Then the method response code should be 200
                     And have 0 Patient's returned

   Scenario: 4.1.38 Search Test
                  Given FHIR STU3 Server
                  When I Get Patient?identifier=LOCAL1001
                  Then the method response code should be 200
                  And have 1 Patient's returned
                  And contains Ids
                       | PatientId |
                       | 1001 |

   Scenario: 4.1.39 Search Test
                    Given FHIR STU3 Server
                    When I Get Patient?identifier=local1001
                    Then the method response code should be 200
                    And have 1 Patient's returned
                    And contains Ids
                         | PatientId |
                         | 1001 |

   Scenario: 4.1.40 Search Test
              Given FHIR STU3 Server
              When I Get Patient?name=Dan
              Then the method response code should be 200
              And have 2 Patient's returned
              And contains Ids
                   | PatientId |
                   | 1037 |
                   | 1040 |

   Scenario: 4.1.41 Search Test
             Given FHIR STU3 Server
             When I Get Patient?name=John
             Then the method response code should be 200
             And have 0 Patient's returned

   Scenario: 4.1.42 Search Test
             Given FHIR STU3 Server
             When I Get Patient?name=kendra
             Then the method response code should be 200
             And have 1 Patient's returned
             And contains Ids
                  | PatientId |
                  | 1002 |

   Scenario: 4.1.43 Search Test
            Given FHIR STU3 Server
            When I Get Patient?name=Jet
            Then the method response code should be 200
            And have 1 Patient's returned
            And contains Ids
                 | PatientId |
                 | 1090 |

   Scenario: 4.1.44 Search Test
           Given FHIR STU3 Server
           When I Get Patient?phone=(0113) 574 3265
           Then the method response code should be 200
           And have 1 Patient's returned
           And contains Ids
                | PatientId |
                | 1002 |

   Scenario: 4.1.45 Search Test
            Given FHIR STU3 Server
            When I Get Patient?phone=01135743265
            Then the method response code should be 200
            And have 0 Patient's returned

   Scenario: 4.1.46 name=munoz&gender=male
            Given FHIR STU3 Server
            When I Get Patient?name=munoz&gender=male
            Then the method response code should be 200
            And have 0 Patient's returned

   Scenario: 4.1.47 name=munoz&gender=female
           Given FHIR STU3 Server
           When I Get Patient?name=munoz&gender=female
           Then the method response code should be 200
           And have 1 Patient's returned
           And contains Ids
               | PatientId |
               | 1001 |

    Scenario: 4.1.48 name=dan&gender=male
           Given FHIR STU3 Server
           When I Get Patient?name=dan&gender=male
           Then the method response code should be 200
           And have 1 Patient's returned
           And contains Ids
               | PatientId |
               | 1040 |

     Scenario: 4.1.49 name=dan&gender=female
           Given FHIR STU3 Server
           When I Get Patient?name=dan&gender=female
           Then the method response code should be 200
           And have 1 Patient's returned
           And contains Ids
               | PatientId |
               | 1037 |

     Scenario: 4.1.50 name=dan&birthdate=1926-08-25
            Given FHIR STU3 Server
            When I Get Patient?name=dan&birthdate=1926-08-25
            Then the method response code should be 200
            And have 1 Patient's returned
            And contains Ids
                | PatientId |
                | 1037 |

     Scenario: 4.1.51 name=dan&birthdate=1926-08
         Given FHIR STU3 Server
         When I Get Patient?name=dan&birthdate=1926-08
         Then the method response code should be 200
         And have 1 Patient's returned
         And contains Ids
             | PatientId |
             | 1037 |

    Scenario: 4.1.52 name=dan&birthdate=1926-08
        Given FHIR STU3 Server
        When I Get Patient?name=dan&birthdate=1926-08
        Then the method response code should be 200
        And have 1 Patient's returned
        And contains Ids
            | PatientId |
            | 1037 |

    Scenario: 4.1.53 name=dan&birthdate=1926
        Given FHIR STU3 Server
        When I Get Patient?name=dan&birthdate=1926
        Then the method response code should be 200
        And have 1 Patient's returned
        And contains Ids
            | PatientId |
            | 1037 |

    Scenario: 4.1.54 name=dan&birthdate=eq1926-08-25
            Given FHIR STU3 Server
            When I Get Patient?name=dan&birthdate=eq1926-08-25
            Then the method response code should be 200
            And have 1 Patient's returned
            And contains Ids
                | PatientId |
                | 1037 |

    Scenario: 4.1.55 name=dan&birthdate=ne1926-08-25
            Given FHIR STU3 Server
            When I Get Patient?name=dan&birthdate=ne1926-08-25
            Then the method response code should be 422


    Scenario: 4.1.56 name=dan&birthdate=lt1973-08-25
            Given FHIR STU3 Server
            When I Get Patient?name=dan&birthdate=lt1973-08-25
            Then the method response code should be 200
            And have 1 Patient's returned
            And contains Ids
                | PatientId |
                | 1037 |

   Scenario: 4.1.57 name=dan&birthdate=le1973-08-25
           Given FHIR STU3 Server
           When I Get Patient?name=dan&birthdate=le1973-08-25
           Then the method response code should be 200
           And have 2 Patient's returned
           And contains Ids
               | PatientId |
               | 1037 |
               | 1040 |


   Scenario: 4.1.58 name=dan&birthdate=gt1926-08-25
              Given FHIR STU3 Server
              When I Get Patient?name=dan&birthdate=gt1926-08-25
              Then the method response code should be 200
              And have 1 Patient's returned
              And contains Ids
                  | PatientId |
                  | 1040 |

   Scenario: 4.1.59 name=dan&birthdate=ge1926-08-25
              Given FHIR STU3 Server
              When I Get Patient?name=dan&birthdate=ge1926-08-25
              Then the method response code should be 200
              And have 2 Patient's returned
              And contains Ids
                  | PatientId |
                  | 1037 |
                  | 1040 |


   Scenario: 4.1.60 name=dan&birthdate=sa1926-08-25
              Given FHIR STU3 Server
              When I Get Patient?name=dan&birthdate=sa1926-08-25
              Then the method response code should be 422

   Scenario: 4.1.61 name=dan&birthdate=eb1973-08-25
              Given FHIR STU3 Server
              When I Get Patient?name=dan&birthdate=eb1973-08-25
              Then the method response code should be 422

   Scenario: 4.1.62 family=mo&gender=female
             Given FHIR STU3 Server
             When I Get Patient?family=mo&gender=female
             Then the method response code should be 200
             And have 2 Patient's returned
             And contains Ids
                  | PatientId |
                  | 1032 |
                  | 1084 |

    Scenario: 4.1.63 family=mo&gender=male
                 Given FHIR STU3 Server
                 When I Get Patient?family=mo&gender=male
                 Then the method response code should be 200
                 And have 0 Patient's returned

   Scenario: 4.1.64 given=vic&gender=female
                Given FHIR STU3 Server
                When I Get Patient?given=vic&gender=female
                Then the method response code should be 200
                And have 2 Patient's returned
                And contains Ids
                     | PatientId |
                     | 1001 |
                     | 1058 |

   Scenario: 4.1.65 given=vic&gender=male
        Given FHIR STU3 Server
        When I Get Patient?given=vic&gender=male
        Then the method response code should be 200
        And have 0 Patient's returned

    Scenario: 4.1.66 Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=lt1919-07-13
         Then the method response code should be 200
         And have 3 Patient's returned
         And contains Ids
             | PatientId |
             | 1038 |
             | 1050 |
             | 1082 |