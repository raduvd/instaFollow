ALTER TABLE potentialFollowers
ADD COLUMN posts INTEGER;

ALTER TABLE potentialFollowers
ADD COLUMN followers INTEGER;

ALTER TABLE potentialFollowers
ADD COLUMN following INTEGER;