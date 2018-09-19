
LOCK TABLE practitionertelecom IN EXCLUSIVE MODE;
SELECT setval('practitionertelecom_practitioner_telecom_id_seq', COALESCE((SELECT MAX(practitioner_telecom_id)+1 FROM practitionertelecom), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

INSERT INTO PractitionerTelecom(CONTACT_VALUE,TELECOM_USE,SYSTEM_ID,PRACTITIONER_ID)
VALUES ('abhatia@nhs.skynet',1, 2, 1);
INSERT INTO PractitionerTelecom(CONTACT_VALUE,TELECOM_USE,SYSTEM_ID,PRACTITIONER_ID)
VALUES ('karen.swamp@nhs.skynet',1, 2, 2);
INSERT INTO PractitionerTelecom(CONTACT_VALUE,TELECOM_USE,SYSTEM_ID,PRACTITIONER_ID)
VALUES('ripley.amber@nhs.skynet',1, 2, 3);

