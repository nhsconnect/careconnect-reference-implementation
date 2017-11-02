Feature: Http Test .N-1
The conformance statement will be used to validate the structure definition of the Patient resource. Additional content is expected as the conformance resources will include resources that are not expected to be accessed directly over the API (such as OperationDefinition, StructureDefinition and ValueSet).


    Scenario: Conformance Retrieval XML
        Given FHIR STU3 Server
        When I retrieve the ConformanceStatement format=xml
        Then the response code should be 200
        And the Header:Content-Type=application/fhir+xml

     Scenario: Conformance Retrieval JSON
        Given FHIR STU3 Server
        When I retrieve the ConformanceStatement format=json
        Then the response code should be 200
        And the Header:Content-Type=application/fhir+json




