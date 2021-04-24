CREATE TABLE IF NOT EXISTS followers
(
    id VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS potentialFollowers
(
    id          VARCHAR(50) NOT NULL,
    isFollower  BOOLEAN,
    isFollowRequested BOOLEAN,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS processedPicture
(
    idPicName          VARCHAR(50) NOT NULL,
    isProcessed        BOOLEAN,
    pageName           VARCHAR(50),
    PRIMARY KEY (idPicName)
);