CREATE SCHEMA IF NOT EXISTS basic_DB;
create table if not exists basic_DB.users
(
    id int primary key,
    login varchar(50) not null unique,
    password varchar(100) not null
    );


create table  if not exists basic_DB.files
(
    id int primary key,
    file_name varchar(50),
    type varchar(10) not null ,
    content BLOB not null,
    create_date timestamp,
    size bigint not null,
    user_id int,
    CONSTRAINT fk_users_files FOREIGN KEY (user_id)
    REFERENCES basic_DB.users (id) MATCH SIMPLE
    );