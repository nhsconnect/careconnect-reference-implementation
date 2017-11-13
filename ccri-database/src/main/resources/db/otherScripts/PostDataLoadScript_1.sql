SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '394802001' ;
update `EpisodeOfCare` set `TYPE_CONCEPT_ID`= @CONCEPT where EPISODE_ID = 4;


SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '281685003' ;
update  `Encounter` set `TYPE_CONCEPT_ID`= @CONCEPT where ENCOUNTER_ID = 1;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '308292007' ;
update  `Encounter` set `TYPE_CONCEPT_ID`= @CONCEPT where ENCOUNTER_ID = 2;

SET @CONCEPT = null;
SELECT CONCEPT_ID INTO @CONCEPT FROM careconnect.Concept where CODE = '25241000000106' ;
update  `Encounter` set `TYPE_CONCEPT_ID`= @CONCEPT where ENCOUNTER_ID = 3;
