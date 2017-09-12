INSERT INTO Practitioner(`PRACTITIONER_ID`,`family_name`,`gender`,`given_name`,`prefix`)
VALUES(1,'Bhatia','MALE','AA','Dr.');
INSERT INTO Practitioner(`PRACTITIONER_ID`,`family_name`,`gender`,`given_name`,`prefix`)
VALUES (2,'Swamp','FEMALE','Karen','Dr.');
INSERT INTO Practitioner(`PRACTITIONER_ID`,`family_name`,`gender`,`given_name`,`prefix`)
VALUES(3,'Amber','FEMALE','Ripley','Dr.');

INSERT INTO PractitionerIdentifier(`PRACTITIONER_IDENTIFIER_ID`,`value`,`SYSTEM_ID`,`PRACTITIONER_ID`)
VALUES (1,'G8133438', 5, 1);
INSERT INTO PractitionerIdentifier(`PRACTITIONER_IDENTIFIER_ID`,`value`,`SYSTEM_ID`,`PRACTITIONER_ID`)
VALUES (2,'G8650149', 5, 2);
INSERT INTO PractitionerIdentifier(`PRACTITIONER_IDENTIFIER_ID`,`value`,`SYSTEM_ID`,`PRACTITIONER_ID`)
VALUES(3, 'PT1357',5,3);


INSERT INTO PractitionerTelecom(`PRACTITIONER_TELECOM_ID`,`value`,`telecomUse`,`system`,`PRACTITIONER_ID`)
VALUES (1,'0115 9737320',1, 0, 1);
INSERT INTO PractitionerTelecom(`PRACTITIONER_TELECOM_ID`,`value`,`telecomUse`,`system`,`PRACTITIONER_ID`)
VALUES (2,'0115 9737320',1, 0, 2);
INSERT INTO PractitionerTelecom(`PRACTITIONER_TELECOM_ID`,`value`,`telecomUse`,`system`,`PRACTITIONER_ID`)
VALUES(3,'0115 9876543',1, 0, 3);
 
INSERT INTO PractitionerAddress (`PRACTITIONER_ADDRESS_ID`,`ADDRESS_ID`,`PRACTITIONER_ID`)
VALUES(1,2,1);
INSERT INTO PractitionerAddress (`PRACTITIONER_ADDRESS_ID`,`ADDRESS_ID`,`PRACTITIONER_ID`)
VALUES(2,2,2);
INSERT INTO PractitionerAddress (`PRACTITIONER_ADDRESS_ID`,`ADDRESS_ID`,`PRACTITIONER_ID`)
VALUES(3,4,3);

INSERT INTO `PractitionerRole` (`PRACTITIONER_ROLE_ID`,`managingOrganisation`,`PRACTITIONER_ID`,`role`) VALUES (1,1,1,NULL);
INSERT INTO `PractitionerRole` (`PRACTITIONER_ROLE_ID`,`managingOrganisation`,`PRACTITIONER_ID`,`role`) VALUES (2,1,2,NULL);
INSERT INTO `PractitionerRole` (`PRACTITIONER_ROLE_ID`,`managingOrganisation`,`PRACTITIONER_ID`,`role`) VALUES (3,2,3,NULL);