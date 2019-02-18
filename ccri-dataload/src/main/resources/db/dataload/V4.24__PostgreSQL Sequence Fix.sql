-- codeSystem

LOCK TABLE concept IN EXCLUSIVE MODE;
SELECT setval('concept_concept_id_seq', COALESCE((SELECT MAX(concept_id)+1 FROM concept), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');
COMMIT;

LOCK TABLE ConceptParentChildLink IN EXCLUSIVE MODE;
SELECT setval('conceptparentchildlink_concept_parent_child_id_seq', COALESCE((SELECT MAX(concept_parent_child_id)+1 FROM ConceptParentChildLink), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');
COMMIT;

LOCK TABLE system IN EXCLUSIVE MODE;
SELECT setval('system_system_id_seq', COALESCE((SELECT MAX(system_id)+1 FROM system), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');
COMMIT;

LOCK TABLE codesystem IN EXCLUSIVE MODE;
SELECT setval('codesystem_codesystem_id_seq', COALESCE((SELECT MAX(codesystem_id)+1 FROM codesystem), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');
COMMIT;

-- Address

LOCK TABLE address IN EXCLUSIVE MODE;
SELECT setval('address_address_id_seq', COALESCE((SELECT MAX(address_id)+1 FROM address), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

-- MedicationRequest 

LOCK TABLE medicationrequest IN EXCLUSIVE MODE;
SELECT setval('medicationrequest_prescription_id_seq', COALESCE((SELECT MAX(prescription_id)+1 FROM medicationrequest), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE medicationrequestidentifier IN EXCLUSIVE MODE;
SELECT setval('medicationrequestidentifier_prescription_identifier_id_seq', COALESCE((SELECT MAX(prescription_identifier_id)+1 FROM medicationrequestidentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE medicationrequestdosage IN EXCLUSIVE MODE;
SELECT setval('medicationrequestdosage_prescription_dosage_id_seq', COALESCE((SELECT MAX(prescription_dosage_id)+1 FROM medicationrequestdosage), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');
-- Procedure 

LOCK TABLE immunisation IN EXCLUSIVE MODE;
SELECT setval('immunisation_immunisation_id_seq', COALESCE((SELECT MAX(immunisation_id)+1 FROM immunisation), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE procedureidentifier IN EXCLUSIVE MODE;
SELECT setval('immunizationidentifier_immunisation_identifier_id_seq', COALESCE((SELECT MAX(immunisation_identifier_id)+1 FROM immunizationidentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');



-- Procedure 

LOCK TABLE procedure_ IN EXCLUSIVE MODE;
SELECT setval('procedure__procedure_id_seq', COALESCE((SELECT MAX(procedure_id)+1 FROM procedure_), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE procedureidentifier IN EXCLUSIVE MODE;
SELECT setval('procedureidentifier_procedure_identifier_id_seq', COALESCE((SELECT MAX(procedure_identifier_id)+1 FROM procedureidentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE procedureperformer IN EXCLUSIVE MODE;
SELECT setval('procedureperformer_procedure_performer_id_seq', COALESCE((SELECT MAX(procedure_performer_id)+1 FROM procedureperformer), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');
-- Condition 

LOCK TABLE condition_ IN EXCLUSIVE MODE;
SELECT setval('condition__condition_id_seq', COALESCE((SELECT MAX(condition_id)+1 FROM condition_), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE conditionidentifier IN EXCLUSIVE MODE;
SELECT setval('conditionidentifier_condition_identifier_id_seq', COALESCE((SELECT MAX(condition_identifier_id)+1 FROM conditionidentifier), 1), false)
WHERE EXISTS (SELECT 1 FROM pg_class c WHERE c.relkind = 'S');

LOCK TABLE conditioncategory IN EXCLUSIVE MODE;
SELECT setval('conditioncategory_condition_category_id_seq', COALESCE((SELECT MAX(condition_category_id)+1 FROM conditioncategory), 1), false)
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

LOCK TABLE allergyintolerancereaction IN EXCLUSIVE MODE;
SELECT setval('allergyintolerancereaction_allergy_reaction_id_seq', COALESCE((SELECT MAX(allergy_reaction_id)+1 FROM allergyintolerancereaction), 1), false)
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

LOCK TABLE encounterdiagnosis1 IN EXCLUSIVE MODE;
SELECT setval('encounterdiagnosis1_encounter_diagnosis_id_seq', COALESCE((SELECT MAX(encounter_diagnosis_id)+1 FROM encounterdiagnosis1), 1), false)
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
