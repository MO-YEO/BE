-- V17__add_fields_to_recruit_application.sql
-- recruit_application 테이블에 지원자 상세 정보 필드 추가

ALTER TABLE recruit_application 
ADD COLUMN name VARCHAR(255),
ADD COLUMN role VARCHAR(255),
ADD COLUMN introduction TEXT,
ADD COLUMN required_skills VARCHAR(255),
ADD COLUMN phone_number VARCHAR(255),
ADD COLUMN contact_email VARCHAR(255),
ADD COLUMN github_url VARCHAR(255);
