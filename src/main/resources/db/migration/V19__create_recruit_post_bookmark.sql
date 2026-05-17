-- V19__create_recruit_post_bookmark.sql
-- 프로젝트 모집글 북마크 테이블 생성

CREATE TABLE IF NOT EXISTS recruit_post_bookmark (
    recruit_post_bookmark_id BIGSERIAL PRIMARY KEY,
    recruit_post_id          BIGINT NOT NULL,
    user_id                  BIGINT NOT NULL,
    created_at               TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_recruit_post_bookmark
        UNIQUE (recruit_post_id, user_id),

    CONSTRAINT fk_recruit_post_bookmark_post
        FOREIGN KEY (recruit_post_id) REFERENCES recruit_post (recruit_post_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_recruit_post_bookmark_user
        FOREIGN KEY (user_id) REFERENCES app_user (user_id)
        ON DELETE CASCADE
);
