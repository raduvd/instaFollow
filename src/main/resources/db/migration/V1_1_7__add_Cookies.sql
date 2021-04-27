CREATE TABLE IF NOT EXISTS cookie
(
    id SERIAL NOT NULL,
    name VARCHAR(50),
    value VARCHAR(50),
    domain  VARCHAR(50),
    path  VARCHAR(50),
    expiry VARCHAR(50),
    isSecure VARCHAR(50),
    userName VARCHAR(50) NOT NULL,

    PRIMARY KEY (id)
);