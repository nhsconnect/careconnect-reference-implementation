use careconnect;

SET SQL_SAFE_UPDATES = 0;

delete from `ProcedureIdentifier` WHERE PROCEDURE_ID > 2;
delete from `ProcedurePerformer` WHERE PROCEDURE_ID > 2;
delete from `Procedure_` WHERE PROCEDURE_ID > 2;

delete from ConditionCategory WHERE CONDITION_ID > 2;
delete from ConditionIdentifier WHERE CONDITION_ID > 2;

delete from Condition_ WHERE CONDITION_ID > 2;

delete from AllergyIntoleranceManifestation;
delete from AllergyIntoleranceCategory;
delete from AllergyIntoleranceReaction;
DELETE FROM AllergyIntolerance;

delete from MedicationRequestDosage;
delete from MedicationRequestIdentifier;
delete from MedicationRequest;







