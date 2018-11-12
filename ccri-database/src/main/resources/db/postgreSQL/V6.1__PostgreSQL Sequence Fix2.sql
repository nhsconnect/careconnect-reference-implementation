
-- EpisodeOfCare


SELECT setval('episodeofcare_episode_id_seq', COALESCE((SELECT MAX(episode_id)+1 FROM episodeofcare), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');


SELECT setval('episodeofcareidentifier_episode_identifier_id_seq', COALESCE((SELECT MAX(episode_identifier_id)+1 FROM episodeofcareidentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');


SELECT setval('episodeofcarediagnosis_episode_diagnosis_id_seq', COALESCE((SELECT MAX(episode_diagnosis_id)+1 FROM episodeofcarediagnosis), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');


SELECT setval('episodeofcarereferral_episode_referral_id_seq', COALESCE((SELECT MAX(episode_referral_id)+1 FROM episodeofcarereferral), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

