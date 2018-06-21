INSERT INTO Patient (PATIENT_ID,RES_DELETED,RES_CREATED,RES_MESSAGE_REF,RES_UPDATED,date_of_birth,gender,registration_end,registration_start,NHSverification,ethnic,GP_ID,marital,PRACTICE_ID)
VALUES (2,NULL,NULL,NULL,NULL,'1970-10-16','MALE',NULL,NULL,79,11,1,5,1);
INSERT INTO Patient (PATIENT_ID,RES_DELETED,RES_CREATED,RES_MESSAGE_REF,RES_UPDATED,date_of_birth,gender,registration_end,registration_start,NHSverification,ethnic,GP_ID,marital,PRACTICE_ID)
VALUES (3,NULL,NULL,NULL,NULL,'1978-12-01','FEMALE',NULL,NULL,79,50,1,5,1);

INSERT INTO PatientName (PATIENT_ID,PATIENT_NAME_ID,family_name,given_name,prefix,nameUse)
VALUES (2,2,'Samson','Horatio','Mr',0);
INSERT INTO PatientName (PATIENT_ID,PATIENT_NAME_ID,family_name,given_name,prefix,nameUse)
VALUES (2,4,'Batman','Nelsons','',3);
INSERT INTO PatientName (PATIENT_ID,PATIENT_NAME_ID,family_name,given_name,prefix,nameUse)
VALUES (3,3,'Mills-Samson','Elsie','Mrs',0);


INSERT INTO PatientIdentifier
(PATIENT_IDENTIFIER_ID,ENT_VALUE,SYSTEM_ID,PATIENT_ID)
VALUES (3,'9876543211', 1, 2);
INSERT INTO PatientIdentifier(PATIENT_IDENTIFIER_ID,ENT_VALUE,SYSTEM_ID,PATIENT_ID)
VALUES(4,'ABC8650150', 1001, 2);
INSERT INTO PatientIdentifier
(PATIENT_IDENTIFIER_ID,ENT_VALUE,SYSTEM_ID,PATIENT_ID)
VALUES (5,'9876512345', 1, 3);
INSERT INTO PatientIdentifier(PATIENT_IDENTIFIER_ID,ENT_VALUE,SYSTEM_ID,PATIENT_ID)
VALUES(6,'ABC8650051', 1001, 3);

INSERT INTO PatientTelecom(PATIENT_TELECOM_ID,ENT_VALUE,telecomUse,system,PATIENT_ID)
VALUES (3,'0115 8497320',0, 0, 2);
INSERT INTO PatientTelecom (PATIENT_TELECOM_ID,system,telecomUse,ENT_VALUE,PATIENT_ID)
VALUES (4,2,0,'horatio.samson@chumhum.com',2);
INSERT INTO PatientTelecom(PATIENT_TELECOM_ID,ENT_VALUE,telecomUse,system,PATIENT_ID)
VALUES (5,'0771 8497320',0, 0, 3);
INSERT INTO PatientTelecom (PATIENT_TELECOM_ID,system,telecomUse,ENT_VALUE,PATIENT_ID)
VALUES (6,2,0,'elsie.mills@chumhum.com',3);

INSERT INTO Address (ADDRESS_ID,RES_DELETED,RES_CREATED,RES_MESSAGE_REF,RES_UPDATED,address_1,address_2,address_3,address_4,address_5,city,county,postcode)
VALUES (8,NULL,NULL,NULL,NULL,'The Forge','1 Curzon Street','Gotham','','','Nottingham','Nottinghamshire','NG11 0HQ');
 
INSERT INTO PatientAddress (PATIENT_ADDRESS_ID,ADDRESS_ID,PATIENT_ID,addressUse)
VALUES(3,8,2,1);
INSERT INTO PatientAddress (PATIENT_ADDRESS_ID,ADDRESS_ID,PATIENT_ID,addressUse)
VALUES(4,8,3,1);