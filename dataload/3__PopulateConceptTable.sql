insert into Concept (CODE, CODESYSTEM_ID, active)
SELECT
	distinct c.id, 9, true
FROM
	tempConcept c
	where active = '1' and id not in (select code from Concept where CODESYSTEM_ID=9);