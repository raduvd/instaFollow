ALTER TABLE potentialFollowers
ADD COLUMN followRequestSentAtDate DATE;

ALTER TABLE potentialFollowers
ADD COLUMN followBackRefused BOOLEAN;