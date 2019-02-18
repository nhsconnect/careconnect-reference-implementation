
-- ValueSet



SELECT setval('valueset_valueset_id_seq', COALESCE((SELECT MAX(valueset_id)+1 FROM valueset), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

SELECT setval('valuesetinclude_valueset_include_id_seq', COALESCE((SELECT MAX(valueset_include_id)+1 FROM valuesetinclude), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

SELECT setval('valuesetincludefilter_valueset_include_filter_id_seq', COALESCE((SELECT MAX(valueset_include_filter_id)+1 FROM valuesetincludefilter), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

SELECT setval('valuesetincludeconcept_valueset_include_concept_id_seq', COALESCE((SELECT MAX(valueset_include_concept_id)+1 FROM valuesetincludeconcept), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

