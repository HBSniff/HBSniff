
create table clerk_fig1
(
    id         int auto_increment
        primary key,
    occupation varchar(255) null
);

create table manager_fig1
(
    id         int auto_increment
        primary key,
    name       varchar(255)   null,
    salary     double null,
    bonus      double null,
    department varchar(255)   null
);

create table manager_fig3
(
    id    int auto_increment
        primary key,
    bonus double null
);

create table person_fig1
(
    id         int auto_increment
        primary key,
    name       varchar(255)   null,
    salary     double null,
    type       varchar(255)   null,
    department varchar(255)   null
);

create table person_fig3
(
    id           int auto_increment
        primary key,
    name         varchar(255)       null,
    university   varchar(255)       null,
    majorSubject varchar(255)       null,
    isEntrolled  tinyint(1) null,
    salary       double     null,
    type         varchar(255)       null,
    occupation   varchar(255)       null
);

create table student_fig1
(
    id         int auto_increment
        primary key,
    name       varchar(255) null,
    university varchar(255) null
);

CREATE TABLE `hibernate_sequences` (
  `sequence_name` varchar(255),
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
