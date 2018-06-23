update Concept as c
 set DISPLAY = d.term
from ConceptDesignation as d where c.CONCEPT_ID = d.CONCEPT_ID
and designationUse=0;