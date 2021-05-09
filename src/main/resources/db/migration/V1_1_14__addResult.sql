CREATE TABLE IF NOT EXISTS results
(
    addedAt timestamp ,
    processType VARCHAR(50),
    followed integer,
    removed integer,
    confirmedRemoved integer,
    confirmedFollowing integer,
    PRIMARY KEY (addedAt)
);
