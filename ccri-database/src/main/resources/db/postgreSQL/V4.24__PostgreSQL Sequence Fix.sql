-- Terminology

LOCK TABLE concept IN EXCLUSIVE MODE;
SELECT setval('concept_concept_id_seq', COALESCE((SELECT MAX(concept_id)+1 FROM concept), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');
COMMIT;

-- Address

LOCK TABLE address IN EXCLUSIVE MODE;
SELECT setval('address_address_id_seq', COALESCE((SELECT MAX(address_id)+1 FROM address), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');


-- AllergyIntolerance 

LOCK TABLE allergyintolerance IN EXCLUSIVE MODE;
SELECT setval('allergyintolerance_allergy_id_seq', COALESCE((SELECT MAX(allergy_id)+1 FROM allergyintolerance), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE allergyintolerance IN EXCLUSIVE MODE;
SELECT setval('allergyintolerancecategory_allergy_category_id_seq', COALESCE((SELECT MAX(allergy_category_id)+1 FROM allergyintolerancecategory), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE allergyintoleranceidentifier IN EXCLUSIVE MODE;
SELECT setval('allergyintoleranceidentifier_allergy_identifier_id_seq', COALESCE((SELECT MAX(allergy_identifier_id)+1 FROM allergyintoleranceidentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE allergyintolerancemanifestation IN EXCLUSIVE MODE;
SELECT setval('allergyintolerancemanifestation_allergy_manifestation_id_seq', COALESCE((SELECT MAX(allergy_manifestation_id)+1 FROM allergyintolerancemanifestation), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

-- Patient 

LOCK TABLE patient IN EXCLUSIVE MODE;
SELECT setval('patient_patient_id_seq', COALESCE((SELECT MAX(patient_id)+1 FROM patient), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE patientaddress IN EXCLUSIVE MODE;
SELECT setval('patientaddress_patient_address_id_seq', COALESCE((SELECT MAX(patient_address_id)+1 FROM patientaddress), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE patientidentifier IN EXCLUSIVE MODE;
SELECT setval('patientidentifier_patient_identifier_id_seq', COALESCE((SELECT MAX(patient_identifier_id)+1 FROM patientidentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE patientname IN EXCLUSIVE MODE;
SELECT setval('patientname_patient_name_id_seq', COALESCE((SELECT MAX(patient_name_id)+1 FROM patientname), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE patienttelecom IN EXCLUSIVE MODE;
SELECT setval('patienttelecom_patient_telecom_id_seq', COALESCE((SELECT MAX(patient_telecom_id)+1 FROM patienttelecom), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

-- Encounter 

LOCK TABLE encounter IN EXCLUSIVE MODE;
SELECT setval('encounter_encounter_id_seq', COALESCE((SELECT MAX(encounter_id)+1 FROM encounter), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE encounteridentifier IN EXCLUSIVE MODE;
SELECT setval('encounteridentifier_encounter_identifier_id_seq', COALESCE((SELECT MAX(encounter_identifier_id)+1 FROM encounteridentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE encounterdiagnosis IN EXCLUSIVE MODE;
SELECT setval('encounterdiagnosis_encounter_reason_id_seq', COALESCE((SELECT MAX(encounter_reason_id)+1 FROM encounterdiagnosis), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE encounterepisodeofcare IN EXCLUSIVE MODE;
SELECT setval('encounterepisodeofcare_encounter_episode_id_seq', COALESCE((SELECT MAX(encounter_episode_id)+1 FROM encounterepisodeofcare), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

-- Practitioner

LOCK TABLE practitioner IN EXCLUSIVE MODE;
SELECT setval('practitioner_practitioner_id_seq', COALESCE((SELECT MAX(practitioner_id)+1 FROM practitioner), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE practitionertelecom IN EXCLUSIVE MODE;
SELECT setval('practitionertelecom_practitioner_telecom_id_seq', COALESCE((SELECT MAX(practitioner_telecom_id)+1 FROM practitionertelecom), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE practitioneridentifier IN EXCLUSIVE MODE;
SELECT setval('practitioneridentifier_practitioner_identifier_id_seq', COALESCE((SELECT MAX(practitioner_identifier_id)+1 FROM practitioneridentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE practitionername IN EXCLUSIVE MODE;
SELECT setval('practitionername_practitioner_name_id_seq', COALESCE((SELECT MAX(practitioner_name_id)+1 FROM practitionername), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE practitioneraddress IN EXCLUSIVE MODE;
SELECT setval('practitioneraddress_practitioner_address_id_seq', COALESCE((SELECT MAX(practitioner_address_id)+1 FROM practitioneraddress), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE practitionerrole IN EXCLUSIVE MODE;
SELECT setval('practitionerrole_practitioner_role_id_seq', COALESCE((SELECT MAX(practitioner_role_id)+1 FROM practitionerrole), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE practitionerroleidentifier IN EXCLUSIVE MODE;
SELECT setval('practitionerroleidentifier_practitioner_role_identifier_id_seq', COALESCE((SELECT MAX(practitioner_role_identifier_id)+1 FROM practitionerroleidentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE practitionerspecialty IN EXCLUSIVE MODE;
SELECT setval('practitionerspecialty_practitioner_specialty_id_seq', COALESCE((SELECT MAX(practitioner_specialty_id)+1 FROM practitionerspecialty), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE practitionertelecom IN EXCLUSIVE MODE;
SELECT setval('practitionertelecom_practitioner_telecom_id_seq', COALESCE((SELECT MAX(practitioner_telecom_id)+1 FROM practitionertelecom), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

-- Organisation

LOCK TABLE organisation IN EXCLUSIVE MODE;
SELECT setval('organisation_organisation_id_seq', COALESCE((SELECT MAX(organisation_id)+1 FROM organisation), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE organisationidentifier IN EXCLUSIVE MODE;
SELECT setval('organisationidentifier_organisation_identifier_id_seq', COALESCE((SELECT MAX(organisation_identifier_id)+1 FROM organisationidentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE organisationaddress IN EXCLUSIVE MODE;
SELECT setval('organisationaddress_organisation_address_id_seq', COALESCE((SELECT MAX(organisation_address_id)+1 FROM organisationaddress), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE organisationtelecom IN EXCLUSIVE MODE;
SELECT setval('organisationtelecom_organisation_telecom_id_seq', COALESCE((SELECT MAX(organisation_telecom_id)+1 FROM organisationtelecom), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

-- Location 

LOCK TABLE location IN EXCLUSIVE MODE;
SELECT setval('location_location_id_seq', COALESCE((SELECT MAX(location_id)+1 FROM location), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE locationidentifier IN EXCLUSIVE MODE;
SELECT setval('locationidentifier_location_identifier_id_seq', COALESCE((SELECT MAX(location_identifier_id)+1 FROM locationidentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE locationaddress IN EXCLUSIVE MODE;
SELECT setval('locationaddress_location_address_id_seq', COALESCE((SELECT MAX(location_address_id)+1 FROM locationaddress), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE locationtelecom IN EXCLUSIVE MODE;
SELECT setval('locationtelecom_location_telecom_id_seq', COALESCE((SELECT MAX(location_telecom_id)+1 FROM locationtelecom), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');