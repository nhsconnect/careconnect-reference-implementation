insert into Concept (CODE, CODESYSTEM_ID, active)
SELECT
	distinct tc.id, 9, true
FROM
	tempConcept as tc
	where tc.active = '1' and not exists (select 1 from Concept as c where c.CODE=tc.id and CODESYSTEM_ID=9);