INSERT INTO `MedicationRequest`(`PRESCRIPTION_ID`,`authoredDate`,`intent`,`priority`,`status`,`substitutionAllowed`,`writtenDate`,`CATEGORY_CONCEPT`,`ENCOUNTER_ID`,`EPISODE_ID`,`MEDICATION_CODE_CONCEPT_ID`,`PATIENT_ID`,`REASON_CONCEPT`,`REASON_CONDITION`,`REASON_OBSERVATION`,`RECORDER_PRACTITIONER`)
VALUES(1,'2017-05-25 00:00:00',2,0,3,true,'2017-05-25 00:00:00',NULL,1,1,NULL,1,NULL,NULL,NULL,1);

INSERT INTO `MedicationRequestDosage` (`PRESCRIPTION_DOSAGE_ID`,`asNeededBoolean`,`doseQuantity`,`doseRangeHigh`,`doseRangeLow`,`otherText`,`patientInstruction`,`sequence`,`ADDITIONAL_INSTRUCTION_CONCEPT`,`ASNEEDED_CONCEPT`,`DOSE_UNITS_CONCEPT`,`METHOD_CONCEPT`,`PRESCRIPTION_ID`,`ROUTE_CONCEPT`,`SITE_CONCEPT`)
VALUES (1,false,null,null,null,'Please explain to Bernie how to use injector.','Three times a day',null,null,null,null,null,1,null,null);
