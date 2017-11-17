

INSERT INTO `codesystem`(`CODESYSTEM_ID`,`CODE_SYSTEM_URI`,`name`)
VALUES(937,'http://hl7.org/fhir/ValueSet/encounter-participant-type','ParticipantType');

INSERT INTO `concept` (`CODE`,`DISPLAY`,`CODESYSTEM_ID`)
VALUES('PPRF','primary performer',937);
INSERT INTO `concept` (`CODE`,`DISPLAY`,`CODESYSTEM_ID`)
VALUES('ADM','admitter',937);
INSERT INTO `concept` (`CODE`,`DISPLAY`,`CODESYSTEM_ID`)
VALUES('ATND','attender',937);
INSERT INTO `concept` (`CODE`,`DISPLAY`,`CODESYSTEM_ID`)
VALUES('CALLBCK','callback contact',937);
INSERT INTO `concept` (`CODE`,`DISPLAY`,`CODESYSTEM_ID`)
VALUES('CON','consultant',937);
INSERT INTO `concept` (`CODE`,`DISPLAY`,`CODESYSTEM_ID`)
VALUES('DIS','discharger',937);
INSERT INTO `concept` (`CODE`,`DISPLAY`,`CODESYSTEM_ID`)
VALUES('ESC','escort',937);
INSERT INTO `concept` (`CODE`,`DISPLAY`,`CODESYSTEM_ID`)
VALUES('REF','referrer',937);
INSERT INTO `concept` (`CODE`,`DISPLAY`,`CODESYSTEM_ID`)
VALUES('SPRF','secondary performer',937);
INSERT INTO `concept` (`CODE`,`DISPLAY`,`CODESYSTEM_ID`)
VALUES('PART','Participant',937);
INSERT INTO `concept` (`CODE`,`DISPLAY`,`CODESYSTEM_ID`)
VALUES('translator','translator',937);
INSERT INTO `concept` (`CODE`,`DISPLAY`,`CODESYSTEM_ID`)
VALUES('emergency','emergency',937);