truncate table tempSimple;

truncate table tempDescription;

truncate table tempConcept;

truncate table tempRelationship;

COPY tempConcept FROM '/mysql_exp/DMD/tempConcept.txt';

COPY tempDescription FROM '/mysql_exp/DMD/tempDescription.txt';

COPY tempSimple FROM '/mysql_exp/DMD/tempSimple.txt';

COPY tempRelationship FROM '/mysql_exp/DMD/tempRelationship.txt';