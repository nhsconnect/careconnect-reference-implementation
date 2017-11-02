Feature: Observation Tests (version 0.11) .N-4-6


    Scenario Outline: <reference> Observation Test
        Given FHIR STU3 Server
        When I Get <query>
        Then the method response code should be <response>
        And have <resultCount> Observation's returned
        And Observation Ids = <observationIds>
        And resource is valid

        Examples:
            | reference | query                                         | response | resultCount | observationIds |
            | 4.6.1     | Observation?category=laboratory               | 200      | 233         |                |
            | 4.6.2     | Observation?category=imaging                  | 200      | 0           |                |
            | 4.6.3     | Observation?code=165581004                    | 200      | 4           |                |
