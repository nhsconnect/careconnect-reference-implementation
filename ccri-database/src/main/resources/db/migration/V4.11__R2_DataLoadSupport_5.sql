

INSERT INTO CodeSystem (CODESYSTEM_ID,CODE_SYSTEM_URI,CODESYSTEM_NAME)
VALUES(938,'http://hl7.org/fhir/immunization-origin','Immunization Origin Codes');

INSERT INTO Concept (CODE,DISPLAY,CODESYSTEM_ID)
VALUES('provider','Other Provider',938);
INSERT INTO Concept (CODE,DISPLAY,CODESYSTEM_ID)
VALUES('record','Written Record',938);
INSERT INTO Concept (CODE,DISPLAY,CODESYSTEM_ID)
VALUES('recall','Parent/Guardian/Patient Recall',938);
INSERT INTO Concept (CODE,DISPLAY,CODESYSTEM_ID)
VALUES('school','School Record',938);
