Feature: PractitionerRole Tests (version 0.11) .N-4-5


    Scenario Outline: <reference> PractitionerRole Test
        Given FHIR STU3 Server
        When I Get <query>
        Then the method response code should be <response>
        And have <resultCount> PractitionerRole's returned
        And PractitionerRole Ids = <practitionerRoleIds>
        And resource is valid

        Examples:
            | reference | query                                         | response | resultCount | practitionerRoleIds |
            | 4.5.1     | PractitionerRole?practitioner=790             | 200      | 1           |                     |
            | 4.5.2     | PractitionerRole?practitioner=Dummy123        | 422      | 0           |                     |
            | 4.5.3     | PractitionerRole?organization=8797            | 200      | 1           |                     |
            | 4.5.4     | PractitionerRole?organization=9222            | 200      | 5           |                     |
            | 4.5.5     | PractitionerRole?address-postalcode=Dummy123  | 400      | 0           |                     |

