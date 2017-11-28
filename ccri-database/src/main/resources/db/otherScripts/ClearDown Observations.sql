truncate careconnect.ObservationCategory;

truncate careconnect.ObservationPerformer;

truncate careconnect.ObservationRange;

truncate careconnect.ObservationIdentifier;

delete from careconnect.Observation where PARENT_OBSERVATION_ID is not null;

SET SQL_SAFE_UPDATES = 0;


delete from careconnect.Observation ; 