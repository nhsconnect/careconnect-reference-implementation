update
tempDescription AS d 
set CONCEPT_ID = null;

update
tempDescription AS d 
set CONCEPT_ID = c.CONCEPT_ID
FROM Concept AS c WHERE d.conceptId = c.CODE
AND c.CODESYSTEM_ID = 9 ;


update
tempDescription as d
set CONCEPT_DESIGNATION_ID = c.CONCEPT_DESIGNATION_ID
FROM ConceptDesignation c WHERE d.id = c.designationId;

insert into ConceptDesignation(designationId,term,CONCEPT_ID,designationUse,active)
SELECT
	distinct d.id,d.term,d.CONCEPT_ID,
	case d.typeId
	   when '900000000000003001' then 0
	   when '900000000000013009' then 1
	   when '900000000000550004' then 2
	end 
	,true
FROM
	tempDescription d
where d.active = '1' and CONCEPT_DESIGNATION_ID is null;