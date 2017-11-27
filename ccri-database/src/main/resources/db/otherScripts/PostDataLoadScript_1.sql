SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '394802001' ;
update `EpisodeOfCare` set `TYPE_CONCEPT_ID`= @CONCEPT where EPISODE_ID = 1;


SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '281685003' ;
update  `Encounter` set `TYPE_CONCEPT_ID`= @CONCEPT where ENCOUNTER_ID = 1;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '308292007' ;
update  `Encounter` set `TYPE_CONCEPT_ID`= @CONCEPT where ENCOUNTER_ID = 2;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '25241000000106' ;
update  `Encounter` set `TYPE_CONCEPT_ID`= @CONCEPT where ENCOUNTER_ID = 3;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '44054006';
update `Condition_` set `CODE_CONCEPT_ID`= @CONCEPT where CONDITION_ID = 2;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '22220005';
update `Condition_` set `CODE_CONCEPT_ID`= @CONCEPT where CONDITION_ID = 1; 

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '174184006';
update `Procedure_` set `CODE_ID`= @CONCEPT where PROCEDURE_ID = 1;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '923461000000103';
update `Procedure_` set `CODE_ID`= @CONCEPT where PROCEDURE_ID = 2;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '1660001' ;
update `AllergyIntolerance` set `CODE_CONCEPT_ID`= @CONCEPT where ALLERGY_ID = 1;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '226017009' ;
update `AllergyIntoleranceReaction` set `SUBSTANCE_CONCEPT_ID`= @CONCEPT where ALLERGY_REACTION_ID = 1;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '39579001' ;
update `AllergyIntoleranceManifestation` set `MANIFESTATION_CONCEPT_ID`= @CONCEPT where ALLERGY_MANIFESTATION_ID = 1;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '441321000000103' ;
update `AllergyIntolerance` set `CODE_CONCEPT_ID`= @CONCEPT where ALLERGY_ID = 2;
SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '414058001' ;
update `AllergyIntoleranceReaction` set `SUBSTANCE_CONCEPT_ID`= @CONCEPT where ALLERGY_REACTION_ID = 2;
SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '91175000' ;
update `AllergyIntoleranceManifestation` set `MANIFESTATION_CONCEPT_ID`= @CONCEPT where ALLERGY_MANIFESTATION_ID = 2;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '396429000' ;
update `Immunisation` set `MEDICATION_CODE_ID`= @CONCEPT where IMMUNISATION_ID = 1;

/* MEDICATIONS */

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '10097211000001102' ;
update `MedicationRequest` set `MEDICATION_CODE_CONCEPT_ID`= @CONCEPT where PRESCRIPTION_ID = 1;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '1521000175104' ;
update `MedicationRequestDosage` set `ADDITIONAL_INSTRUCTION_CONCEPT`= @CONCEPT where PRESCRIPTION_DOSAGE_ID = 1;





