-- 로컬 개발용 데이터베이스(스키마) 생성
-- Database: medi_database
CREATE DATABASE medi_database CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- medicine : 약 정보 테이블
--  - 이름, 효능, 설명, 상호작용, 이미지 URL 저장
create table if not exists medicine
(
    created_at       datetime(6)  null,
    id               bigint auto_increment
    primary key,
    updated_at       datetime(6)  null,
    name             varchar(150) not null,
    benefit          text         not null,
    description      text         not null,
    drug_interaction varchar(255) null,
    image_url        varchar(255) null
    );


-- user : 사용자 계정 테이블
--  - 회원 정보 (아이디, 이름, 전화번호, 비밀번호, 권한 등)
--  - 상태(활성/비활성), 동의 여부, refresh token 관리
create table if not exists user
(
    inactive_date date                         null,
    created_at    datetime(6)                  null,
    id            bigint auto_increment
    primary key,
    updated_at    datetime(6)                  null,
    memberid      varchar(15)                  not null,
    name          varchar(15)                  not null,
    phone         varchar(15)                  not null,
    password      varchar(40)                  not null,
    refresh_token varchar(255)                 null,
    consent       varchar(15)                  null,
    role          enum ('ADMIN', 'USER')       null,
    status        varchar(15) default 'ACTIVE' null
    );


-- status_table : 상태 기록 테이블
--  - 사용자의 건강 상태, 컨디션 기록
--  - 음주 여부, 상태, 메모 등을 기록
create table if not exists status_table
(
    date             date                           not null,
    created_at       datetime(6)                    null,
    id               bigint auto_increment
    primary key,
    updated_at       datetime(6)                    null,
    memo             varchar(255)                   null,
    drink            enum ('DRINK', 'NODRINK')      not null,
    status           enum ('BAD', 'GOOD', 'NOTBAD') not null,
    status_condition enum ('BAD', 'GOOD', 'NOTBAD') not null
    );


-- calendar : 일정 테이블
--  - member와 status_table과 연관
--  - 특정 날짜의 상태 기록과 연결됨
create table if not exists calendar
(
    created_at datetime(6) null,
    id         bigint auto_increment
    primary key,
    member_id  bigint      null,
    status_id  bigint      null,
    updated_at datetime(6) null,
    date       varchar(40) not null,
    constraint FK4xr1o686dphnmfq2ehy0d166
    foreign key (member_id) references user (id),
    constraint FK6mcpgj7lrsnr4wfej2x9bnq5y
    foreign key (status_id) references status_table (id)
    );


-- user_medicine : 사용자 복용 약 테이블
--  - 사용자가 어떤 약을 복용하는지 기록
--  - 알람 여부, 복용 시간, 식사 전/후 등 기록
--  - medicine, user 테이블과 연관
create table if not exists user_medicine
(
    is_alarm    bit         not null,
    is_check    bit         not null,
    created_at  datetime(6) null,
    id          bigint auto_increment
    primary key,
    medicine_id bigint      null,
    member_id   bigint      null,
    updated_at  datetime(6) null,
    dosage      varchar(30) null,
    time        varchar(30) null,
    meal        varchar(15) null,
    constraint FK604c1kl9ngvcd2toodhoe6twv
    foreign key (member_id) references user (id),
    constraint FKnfn672hqfum0atlesq0pos45s
    foreign key (medicine_id) references medicine (id)
    );

