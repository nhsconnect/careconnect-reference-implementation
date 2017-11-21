use careconnect;

SET SQL_SAFE_UPDATES = 0;

delete from AllergyIntoleranceManifestation where ALLERGY_REACTION_ID > 2;
delete from AllergyIntoleranceReaction where ALLERGY_ID > 2;

delete from AllergyIntoleranceIdentifier where ALLERGY_ID > 2;
delete from AllergyIntoleranceCategory where ALLERGY_ID > 2;
delete from AllergyIntolerance where ALLERGY_ID > 2;