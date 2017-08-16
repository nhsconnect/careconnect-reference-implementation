
INSERT IGNORE INTO careconnect.medical_departments
  (id,department,lastUpdated)
VALUES
  (1,'Neighbourhood','2016-07-25 12:00:00'),
  (2,'Hospital','2016-07-25 12:00:00'),
  (3,'Community Care','2016-07-25 12:00:00'),
  (4,'Primary Care','2016-07-25 12:00:00'),
  (5,'Mental Health','2016-07-25 12:00:00');


INSERT IGNORE INTO careconnect.organizations
  (id,org_code,site_code,org_name,lastUpdated)
VALUES
  (1,'GPC001','Z26556','GP Connect Demonstrator','2016-07-25 12:00:00'),
  (2,'R1A14','Z33433','Test GP Care Trust','2016-07-25 12:00:00'),
  (3,'R1A14','Z33432','Test GP Second Care Trust','2016-07-25 12:00:00'),
  (4,'R1A17','Z33433','The Hockey Surgery','2016-07-25 12:00:00');