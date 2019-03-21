
-- Concept Map

SELECT setval('conceptmap_concept_map_id_seq', COALESCE((SELECT MAX(concept_map_id)+1 FROM conceptmap), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

SELECT setval('conceptmapelement_concept_map_element_id_seq', COALESCE((SELECT MAX(concept_map_element_id)+1 FROM conceptmapelement), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

SELECT setval('conceptmapgroup_concept_map_group_id_seq', COALESCE((SELECT MAX(concept_map_group_id)+1 FROM conceptmapgroup), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

SELECT setval('conceptmapidentifier_concept_map_identifier_id_seq', COALESCE((SELECT MAX(concept_map_identifier_id)+1 FROM conceptmapidentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

SELECT setval('conceptmaptarget_concept_map_target_id_seq', COALESCE((SELECT MAX(concept_map_target_id)+1 FROM conceptmaptarget), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

SELECT setval('conceptmaptelecom_concept_map_telecom_id_seq', COALESCE((SELECT MAX(concept_map_telecom_id)+1 FROM conceptmaptelecom), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');
