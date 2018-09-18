CREATE TABLE if NOT EXISTS tempConcept (
  idConcept bigserial,
  id varchar(45)  DEFAULT NULL,
  effectiveTime date DEFAULT NULL,
  active varchar(45)  DEFAULT NULL,
  moduleId varchar(45)  DEFAULT NULL,
  definitionStatusId varchar(45)  DEFAULT NULL,
  PRIMARY KEY (idConcept)
)  ;

CREATE TABLE IF NOT EXISTS tempDescription (
  idDescription bigserial,
  id varchar(45)  DEFAULT NULL,
  effectiveTime date DEFAULT NULL,
  active varchar(45)  DEFAULT NULL,
  moduleId varchar(45)  DEFAULT NULL,
  conceptId varchar(45)  DEFAULT NULL,
  languageCode varchar(45)  DEFAULT NULL,
  typeId varchar(45)  DEFAULT NULL,
  term varchar(1024)  DEFAULT NULL,
  caseSignificanceId varchar(45) DEFAULT NULL,
  CONCEPT_DESIGNATION_ID bigint,
  designationUse int,
  CONCEPT_ID bigint,
  PRIMARY KEY (idDescription)
) ;

CREATE TABLE if NOT EXISTS tempRelationship (
  idRelationship bigserial,
  id varchar(45) DEFAULT NULL,
  effectiveTime date DEFAULT NULL,
  active varchar(45)  DEFAULT NULL,
  moduleId varchar(45)  DEFAULT NULL,
  sourceId varchar(45)  DEFAULT NULL,
  destinationId varchar(45) DEFAULT NULL,
  relationshipGroup varchar(45)  DEFAULT NULL,
  typeId varchar(45)  DEFAULT NULL,
  characteristicTypeId varchar(45)  DEFAULT NULL,
  modifierId varchar(45)  DEFAULT NULL,
  PRIMARY KEY (idRelationship)
) ;


CREATE TABLE if NOT EXISTS tempSimple (
  idSimple bigserial,
  id varchar(45)  DEFAULT NULL,
  effectiveTime date DEFAULT NULL,
  active varchar(45)  DEFAULT NULL,
  moduleId varchar(45)  DEFAULT NULL,
  refsetId varchar(45)  DEFAULT NULL,
  referencedComponentId varchar(45) DEFAULT NULL,
  PRIMARY KEY (idSimple)
) ;

