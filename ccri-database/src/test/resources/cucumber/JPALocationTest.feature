Feature: Location Integration Test
  As a client FHIR system
  I want to search for a Location by SDS site Code

  Scenario: Location Search by SDS Code
        Given I have these Locations on the RI:
            | SDSCode  | Surname | Initials | Title | Sex  | Phone        | Street        | Town       | City       | PostCode | PracticeCode | RoleCode | RoleName                     |
            | G8133438 | Bhatia  | AA       | Dr.   | MALE | 0115 9737320 | Regent Street | Long Eaton | Nottingham | NG10 1QQ | C81010       | R0260    | General Medical Practitioner |
        When I search for Location <SDSCode>

        Then the search shall be logged in the Audit Trail


