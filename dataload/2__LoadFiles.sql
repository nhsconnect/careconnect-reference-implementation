truncate table tempSimple;

truncate table tempDescription;

truncate table tempConcept;

truncate table tempRelationship;

COPY tempConcept FROM '/mysql_exp/yellow/tempConcept.txt';

COPY tempDescription FROM '/mysql_exp/yellow/tempDescription.txt';

COPY tempSimple FROM '/mysql_exp/yellow/tempSimple.txt';

COPY tempRelationship FROM '/mysql_exp/yellow/tempRelationship.txt';