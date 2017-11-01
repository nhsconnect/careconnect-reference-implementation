Feature: Patient Search Tests


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

    Scenario: 4.1.9 Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=ne1918-05-20
         Then the method response code should be 422

    Scenario: 4.1.9a Search Test
         Given FHIR STU3 Server
         When I Get Patient?birthdate=lt1919-07-13
         Then the method response code should be 200
         And have 3 Patient's returned
         And contains Ids
             | PatientId |
             | 1038 |
             | 1050 |
             | 1082 |

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
         When I Get Patient?birthdate=ap1917-06-04
         Then the method response code should be 422

    Scenario: 4.1.16 Search Test
         Given FHIR STU3 Server
         When I Get Patient?email=Judy.Welch@again.co.uk
         Then the method response code should be 200
          And have 1 Patient's returned
          And contains Ids
              | PatientId |
              | 1099 |

    Scenario: 4.1.17 Search Test
         Given FHIR STU3 Server
         When I Get Patient?email=Judy.welch@again.co.uk
         Then the method response code should be 200
          And have 1 Patient's returned
          And contains Ids
              | PatientId |
              | 1099 |

    Scenario: 4.1.18 Search Test
         Given FHIR STU3 Server
         When I Get Patient?email=Judy
         Then the method response code should be 200
         And have 2 Patient's returned
         And contains Ids
             | PatientId |
             | 1099 |
             | 1055 |

    Scenario: 4.1.19 Search Test
         Given FHIR STU3 Server
         When I Get Patient?email:exists=false
         Then the method response code should be 200
         And have 1 Patient's returned
         And contains Ids
             | PatientId |
             | 1001 |

    Scenario: 4.1.20 Search Test
         Given FHIR STU3 Server
         When I Get Patient?family=
         Then the method response code should be 200
         And have 1 Patient's returned
         And contains Ids
             | PatientId |
             | 1001 |

    Scenario: 4.1.21 Search Test
             Given FHIR STU3 Server
             When I Get Patient?family=Wilson
             Then the method response code should be 200
             And have 0 Patient's returned


   Scenario: 4.1.22 Search Test
            Given FHIR STU3 Server
            When I Get Patient?family=munoz
            Then the method response code should be 200
            And have 1 Patient's returned
            And contains Ids
                | PatientId |
                | 1001 |

   Scenario: 4.1.23 Search Test Antić
            Given FHIR STU3 Server
            When I Get Patient?family=Antic
            Then the method response code should be 200
            And have 1 Patient's returned
            And contains Ids
                | PatientId |
                | 1009 |

   Scenario: 4.1.24 Search Test
            Given FHIR STU3 Server
            When I Get Patient?family=Jessop
            Then the method response code should be 200
            And have 1 Patient's returned
            And contains Ids
                | PatientId |
                | 1012 |

   Scenario: 4.1.25 Search Test
            Given FHIR STU3 Server
            When I Get Patient?family=Mo
            Then the method response code should be 200
            And have 2 Patient's returned
            And contains Ids
                | PatientId |
                | 1032 |
                | 1084 |