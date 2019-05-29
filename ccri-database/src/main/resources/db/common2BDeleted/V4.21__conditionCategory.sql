
update Concept set CODE= 'encounter-diagnosis', DISPLAY = 'Encounter Diagnosis' where CODE = 'diagnosis' AND CODESYSTEM_ID=15;
update Concept set CODE= 'presenting-complaint', DISPLAY = 'Presenting Complaint' where CODE = 'complaint' AND CODESYSTEM_ID=15;
update Concept set CODE= 'issue', DISPLAY = 'issue' where CODE = 'Issue' AND CODESYSTEM_ID=15;
update Concept set CODE= 'deleted', DISPLAY = 'Removed' where CODE = 'need' AND CODESYSTEM_ID=15;
update Concept set CODE= 'problem-list-item', DISPLAY = 'Problem List Item' where CODE = 'problem' AND CODESYSTEM_ID=15;
update Concept set CODE= 'co-morbidity', DISPLAY = 'Co-morbidity' where CODE = 'symptom' AND CODESYSTEM_ID=15;