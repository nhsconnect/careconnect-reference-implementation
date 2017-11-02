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
            | 4.6.4     | Observation?code=Dummy1234                    | 200      | 0           |                |
            | 4.6.5     | Observation?date=2017-10-16                   | 200      | 63           |                |
            | 4.6.6     | Observation?date=16-sep-2017                  | 400      | 0           |                |
            | 4.6.7     | Observation?date=2012                         | 200      | 3           |                |
            | 4.6.8     | Observation?date=2012-11                      | 200      | 1           |                |
            | 4.6.9     | Observation?patient=1004                      | 200      | 0           |                |
            | 4.6.10    | Observation?patient=1050                      | 200      | 1           |                |
            | 4.6.11    | Observation?patient=1050&category=observation | 200      | 0           |                |
            | 4.6.12    | Observation?patient=1050&category=vital-signs | 200      | 1           |                |
