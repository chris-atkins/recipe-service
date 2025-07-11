CREATE TABLE image (
    id CHAR(24) PRIMARY KEY,
    image_id VARCHAR(36) NOT NULL,
    owning_user_id VARCHAR(24) NOT NULL,
    url text NOT NULL,

    UNIQUE(image_id)
);

