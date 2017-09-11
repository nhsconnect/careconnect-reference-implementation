Feature: Practitioner Integration Test
  As a client FHIR system
  I want to search for a Practioner by SDS Code (GMP or GMC)

  Scenario: Practitioner Search by GMP Code
        Given I have these Practitioners on the RI:
            | SDSCode  | Surname | Initials | Title | Sex  | Phone        | Street        | Town       | City       | PostCode | PracticeCode | RoleCode | RoleName                     |
            | G8133438 | Bhatia  | AA       | Dr.   | MALE | 0115 9737320 | Regent Street | Long Eaton | Nottingham | NG10 1QQ | C81010       | R0260    | General Medical Practitioner |
        When I search for Practitioner <SDSCode>
        Then the search shall be logged in the Audit Trail


