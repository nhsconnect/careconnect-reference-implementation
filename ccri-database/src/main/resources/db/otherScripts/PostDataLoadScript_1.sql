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

