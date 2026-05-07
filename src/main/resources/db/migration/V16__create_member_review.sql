CREATE TABLE member_review (
    review_id          BIGSERIAL PRIMARY KEY,
    recruit_post_id    BIGINT NOT NULL,
    writer_user_id     BIGINT NOT NULL,
    target_user_id     BIGINT NOT NULL,
    rating             SMALLINT NOT NULL,
    content            VARCHAR(1000) NOT NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_member_review_unique UNIQUE (recruit_post_id, writer_user_id, target_user_id)
);

CREATE INDEX idx_member_review_target_user_id 
    ON member_review(target_user_id);
