INSERT INTO Patient (`PATIENT_ID`,`RES_DELETED`,`RES_CREATED`,`RES_MESSAGE_REF`,`RES_UPDATED`,`date_of_birth`,`family_name`,`gender`,`given_name`,`prefix`,`registration_end`,`registration_start`,`NHSverification`,`ethnic`,`GP_ID`,`marital`,`PRACTICE_ID`) VALUES (1,NULL,NULL,NULL,NULL,'1998-03-19','Kanfeld','FEMALE','Bernie','Miss',NULL,NULL,79,11,1,7,1);

INSERT INTO PatientIdentifier
(`PATIENT_IDENTIFIER_ID`,`value`,`SYSTEM_ID`,`PATIENT_ID`)
VALUES (1,'9876543210', 1, 1);
INSERT INTO PatientIdentifier(`PATIENT_IDENTIFIER_ID`,`value`,`SYSTEM_ID`,`PATIENT_ID`)
VALUES(2,'ABC8650149', 1001, 1);


INSERT INTO PatientTelecom(`PATIENT_TELECOM_ID`,`value`,`telecomUse`,`SYSTEM_ID`,`PATIENT_ID`)
VALUES (1,'0115 9737320','work', 6, 1);
 
INSERT INTO PatientAddress (`PATIENT_ADDRESS_ID`,`ADDRESS_ID`,`PATIENT_ID`)
VALUES(1,5,1);