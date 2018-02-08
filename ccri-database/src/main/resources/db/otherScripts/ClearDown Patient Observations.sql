SET SQL_SAFE_UPDATES = 0;

delete from ObservationCategory where OBSERVATION_ID in (select OBSERVATION_ID from Observation where  PATIENT_ID = 1186);
delete from ObservationIdentifier where OBSERVATION_ID in (select OBSERVATION_ID from Observation where PATIENT_ID = 1186);
delete from ObservationPerformer where OBSERVATION_ID in (select OBSERVATION_ID from Observation where PATIENT_ID = 1186);

delete from Observation where  PATIENT_ID = 1186 and PARENT_OBSERVATION_ID is not null;
delete from Observation where  PATIENT_ID = 1186 and PARENT_OBSERVATION_ID is null;

delete from EncounterIdentifier where ENCOUNTER_ID in (select ENCOUNTER_ID from Encounter where PATIENT_ID = 1186);
delete from Encounter where PATIENT_ID = 1186