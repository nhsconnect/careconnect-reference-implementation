INSERT INTO Patient(`PATIENT_ID`,`family_name`,`gender`,`given_name`,`prefix`, `GP_ID`, `PRACTICE_ID`)
VALUES(1,'Kanfeld','FEMALE','Bernie','Miss',1,1);

INSERT INTO PatientIdentifier
(`PATIENT_IDENTIFIER_ID`,`value`,`SYSTEM_ID`,`PATIENT_ID`)
VALUES (1,'9876543210', 1, 1);
INSERT INTO PatientIdentifier(`PATIENT_IDENTIFIER_ID`,`value`,`SYSTEM_ID`,`PATIENT_ID`)
VALUES(2,'ABC8650149', 1001, 1);


INSERT INTO PatientTelecom(`PATIENT_TELECOM_ID`,`value`,`telecomUse`,`SYSTEM_ID`,`PATIENT_ID`)
VALUES (1,'0115 9737320','work', 6, 1);
 
INSERT INTO PatientAddress (`PATIENT_ADDRESS_ID`,`ADDRESS_ID`,`PATIENT_ID`)
VALUES(1,5,1);