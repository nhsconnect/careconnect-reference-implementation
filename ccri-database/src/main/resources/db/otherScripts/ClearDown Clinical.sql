use careconnect;

SET SQL_SAFE_UPDATES = 0;


delete from ImmunizationIdentifier where IMMUNISATION_ID > 1;
delete from Immunisation where IMMUNISATION_ID > 1;


delete from `EncounterIdentifier` WHERE ENCOUNTER_ID > 3;
delete from `Encounter` WHERE ENCOUNTER_ID > 3;

delete from `ProcedureIdentifier` WHERE PROCEDURE_ID > 2;
delete from `ProcedurePerformer` WHERE PROCEDURE_ID > 2;
delete from `Procedure_` WHERE PROCEDURE_ID > 2;

delete from ConditionCategory WHERE CONDITION_ID > 2;
delete from ConditionIdentifier WHERE CONDITION_ID > 2;

delete from Condition_ WHERE CONDITION_ID > 2;


delete from MedicationRequestDosage where PRESCRIBING_ID>1;
delete from MedicationRequestIdentifier where PRESCRIBING_ID>1;
delete from MedicationRequest where PRESCRIBING_ID>1;







