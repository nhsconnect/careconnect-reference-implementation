Feature: Organization Tests (version 0.11) .N-4-3


    Scenario Outline: <reference> Organization Test
        Given FHIR STU3 Server
        When I Get <query>
        Then the method response code should be <response>
        And have <resultCount> Organization's returned
        And Organization Ids = <organizationIds>
        And resource is valid

        Examples:
            | reference | query                                         | response | resultCount | organizationIds       |
            | 4.3.1     | Organization?identifier=RGD                   | 200      | 1           | 63                    |
            | 4.3.2     | Organization?identifier=Dummy123              | 200      | 0           |                       |
            | 4.3.3     | Organization?name=Leeds                       | 200      | 16          |                       |
            | 4.3.4     | Organization?name=Leeds City Medical Practice | 200      | 1           | 1568                  |
            | 4.3.5     | Organization?name=Dummy123                    | 200      | 0           |                       |

