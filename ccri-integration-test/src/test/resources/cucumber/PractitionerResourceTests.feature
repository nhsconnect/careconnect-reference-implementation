Feature: Practitioner Tests (version 0.11) .N-4-4


    Scenario Outline: <reference> Practitioner Test
        Given FHIR STU3 Server
        When I Get <query>
        Then the method response code should be <response>
        And have <resultCount> Practitioner's returned
        And Practitioner Ids = <practitionerIds>
        And resource is valid

        Examples:
            | reference | query                                   | response | resultCount | practitionerIds       |
            | 4.4.1     | Practitioner?identifier=G2011402        | 200      | 1           |                       |
            | 4.4.2     | Practitioner?identifier=Dummy123        | 200      | 0           |                       |
            | 4.4.3     | Practitioner?address-postalcode=LS      | 200      | 1411        |                       |
            | 4.4.4     | Practitioner?address-postalcode=LS9 6AU | 200      | 10          |                       |
            | 4.4.5     | Practitioner?address-postalcode=Dum     | 200      | 0           |                       |

