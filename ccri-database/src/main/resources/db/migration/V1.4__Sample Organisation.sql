INSERT INTO Organisation(ORGANISATION_ID,name)
VALUES(1,'The Moir Medical Centre');

INSERT INTO Organisation(ORGANISATION_ID,name)
VALUES(2,'Derby Teaching Hospitals NHS Foundation Trust');

INSERT INTO OrganisationIdentifier(ORGANISATION_IDENTIFIER_ID,`value`,SYSTEM_ID,ORGANISATION_ID)
VALUES (1,'C81010', 3, 1);

INSERT INTO OrganisationIdentifier(ORGANISATION_IDENTIFIER_ID,`value`,SYSTEM_ID,ORGANISATION_ID)
VALUES(2,'RTG', 3, 2);

INSERT INTO OrganisationTelecom(`ORGANISATION_TELECOM_ID`,`value`,`telecomUse`,`system`,`ORGANISATION_ID`)
VALUES (1,'0115 9737320',1, 1, 1);

INSERT INTO OrganisationTelecom(`ORGANISATION_TELECOM_ID`,`value`,`telecomUse`,`system`,`ORGANISATION_ID`)
VALUES (2,'01332 340131',1, 1, 2);

INSERT INTO OrganisationAddress (`ORGANISATION_ADDRESS_ID`,`ADDRESS_ID`,`ORGANISATION_ID`,`addressUse`,`addressType`)
VALUES(1,2,1,1,2);

INSERT INTO OrganisationAddress (`ORGANISATION_ADDRESS_ID`,`ADDRESS_ID`,`ORGANISATION_ID`,`addressUse`,`addressType`)
VALUES(2,3,2,1,2);
