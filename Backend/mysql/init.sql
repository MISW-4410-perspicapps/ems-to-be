-- Use the database created by Docker environment variables
USE ems_database;

-- Grant all privileges to the user on this database
GRANT ALL PRIVILEGES ON ems_database.* TO 'ems_user'@'%';
FLUSH PRIVILEGES;

create table ate
(
    id          int auto_increment
        primary key,
    employeeId  int null,
    managerId   int null,
    directoryId int null,
    ateStatus   bit null,
    status      bit null
)
    charset = latin1;

INSERT INTO ems_database.ate (id, employeeId, managerId, directoryId, ateStatus, status) VALUES (1, 7, 4, 5, true, true);
INSERT INTO ems_database.ate (id, employeeId, managerId, directoryId, ateStatus, status) VALUES (2, 5, 6, 12, true, true);

create table roles
(
    id       int auto_increment
        primary key,
    rolename varchar(20) null,
    status   bit         null
)
    charset = latin1;

INSERT INTO ems_database.roles (id, rolename, status) VALUES (1, 'Admin', true);
INSERT INTO ems_database.roles (id, rolename, status) VALUES (2, 'Manager', true);
INSERT INTO ems_database.roles (id, rolename, status) VALUES (3, 'Employee', true);
INSERT INTO ems_database.roles (id, rolename, status) VALUES (4, 'NA', false);

create table registration
(
    id             int auto_increment
        primary key,
    firstname      varchar(255) null,
    lastname       varchar(255) null,
    email          varchar(255) null,
    address        varchar(255) null,
    phone          varchar(20)  null,
    username       varchar(255) null,
    password       varchar(255) null,
    role           int          not null,
    managerstatus  bit          null,
    managerId      int          null,
    activitystatus bit          null,
    date           date         null,
    constraint role_for_registration
        foreign key (role) references roles (id)
)
    charset = latin1;

INSERT INTO ems_database.registration (id, firstname, lastname, email, address, phone, username, password, role, managerstatus, managerId, activitystatus, date) VALUES (1, 'Gagan', 'Shetty', 'gsolur@albany.edu', '489 washington avenue', '9035245991', 'admin', 'admin', 1, false, 1, true, '2017-05-08');
INSERT INTO ems_database.registration (id, firstname, lastname, email, address, phone, username, password, role, managerstatus, managerId, activitystatus, date) VALUES (2, 'Kate', 'Winslet', 'wkate@albany.edu', '523 North Allen, Albany', '9078734456', 'KW001', 'kw001', 2, true, 2, true, '2017-05-08');
INSERT INTO ems_database.registration (id, firstname, lastname, email, address, phone, username, password, role, managerstatus, managerId, activitystatus, date) VALUES (3, 'Johny', 'Depp', 'jsparrow@albany.edu', '583 western avenue', '9876545678', 'JD002', 'jd002', 3, false, 2, true, '2017-05-08');
INSERT INTO ems_database.registration (id, firstname, lastname, email, address, phone, username, password, role, managerstatus, managerId, activitystatus, date) VALUES (4, 'Jai', 'Kumar', 'jai@albany.edu', '235 clifton park', '9878656789', 'JK005', 'jk005', 2, true, 2, true, '2017-05-08');
INSERT INTO ems_database.registration (id, firstname, lastname, email, address, phone, username, password, role, managerstatus, managerId, activitystatus, date) VALUES (5, 'Ganapati', 'Naik', 'naik@ait.com', '111 clifton park', '9897655678', 'GN003', 'gn003', 3, false, 4, true, '2017-05-08');
INSERT INTO ems_database.registration (id, firstname, lastname, email, address, phone, username, password, role, managerstatus, managerId, activitystatus, date) VALUES (6, 'vishu', 'Murundi', 'vishu@albany.edu', '94 North Allen street, Albany', '9878656789', 'VM004', 'vm004', 2, true, 2, true, '2017-05-08');
INSERT INTO ems_database.registration (id, firstname, lastname, email, address, phone, username, password, role, managerstatus, managerId, activitystatus, date) VALUES (7, 'Tejus', 'Gowda', 'tejus@yahoo.com', '90 kent steert', '9078734456', 'TG006', 'tg006', 3, false, 6, true, '2017-05-08');
INSERT INTO ems_database.registration (id, firstname, lastname, email, address, phone, username, password, role, managerstatus, managerId, activitystatus, date) VALUES (8, 'Achala', 'Rao', 'arao@gmail.com', '750 Los Angles', '9035245991', 'ML007', 'ml007', 3, false, 0, false, '2017-05-10');
INSERT INTO ems_database.registration (id, firstname, lastname, email, address, phone, username, password, role, managerstatus, managerId, activitystatus, date) VALUES (9, 'Harish', 'Kumar', 'sharish@albany.edu', '94 North Allen street, Albany', '9878656789', 'e1234', '1234', 3, false, 6, true, '2017-05-11');

create table permission
(
    id         int auto_increment
        primary key,
    permission varchar(20) null,
    status     bit         null
)
    charset = latin1;

INSERT INTO ems_database.permission (id, permission, status) VALUES (1, 'Public', true);
INSERT INTO ems_database.permission (id, permission, status) VALUES (2, 'Private', true);
INSERT INTO ems_database.permission (id, permission, status) VALUES (3, 'Protect', true);
INSERT INTO ems_database.permission (id, permission, status) VALUES (4, 'Default', true);

create table directorydetails
(
    id            int auto_increment
        primary key,
    directoryName varchar(500)  null,
    managerId     int           not null,
    permissionId  int           not null,
    accessibleIds varchar(1000) null,
    status        bit           null,
    ateIds        varchar(1000) null,
    constraint managerId_for_directory
        foreign key (managerId) references registration (id),
    constraint permissionId_for_directory
        foreign key (permissionId) references permission (id)
)
    charset = latin1;

INSERT INTO ems_database.directorydetails (id, directoryName, managerId, permissionId, accessibleIds, status, ateIds) VALUES (1, 'DIR-2', 2, 4, '2,3,4,6,', true, 'NA');
INSERT INTO ems_database.directorydetails (id, directoryName, managerId, permissionId, accessibleIds, status, ateIds) VALUES (2, 'KW-Public', 2, 3, '2,3,4,6,', true, 'NA');
INSERT INTO ems_database.directorydetails (id, directoryName, managerId, permissionId, accessibleIds, status, ateIds) VALUES (3, 'DIR-4', 4, 4, '4,2,5,', true, 'NA');
INSERT INTO ems_database.directorydetails (id, directoryName, managerId, permissionId, accessibleIds, status, ateIds) VALUES (4, 'DIR-6', 6, 4, '6,2,7,9,', true, 'NA');
INSERT INTO ems_database.directorydetails (id, directoryName, managerId, permissionId, accessibleIds, status, ateIds) VALUES (5, 'JK-Prot', 4, 3, '4,2,5,', true, '7,');
INSERT INTO ems_database.directorydetails (id, directoryName, managerId, permissionId, accessibleIds, status, ateIds) VALUES (6, 'JK-Public', 4, 1, 'ALL,', true, 'NA');
INSERT INTO ems_database.directorydetails (id, directoryName, managerId, permissionId, accessibleIds, status, ateIds) VALUES (7, 'KW-Protect', 2, 3, '2,3,4,6,', true, 'NA');
INSERT INTO ems_database.directorydetails (id, directoryName, managerId, permissionId, accessibleIds, status, ateIds) VALUES (8, 'KW-Protect2', 2, 3, '2,3,4,6,', true, 'NA');
INSERT INTO ems_database.directorydetails (id, directoryName, managerId, permissionId, accessibleIds, status, ateIds) VALUES (9, 'VM-Priv', 6, 2, '6,2,', true, 'NA');
INSERT INTO ems_database.directorydetails (id, directoryName, managerId, permissionId, accessibleIds, status, ateIds) VALUES (10, 'KW-Public2', 2, 1, 'ALL,', true, 'NA');
INSERT INTO ems_database.directorydetails (id, directoryName, managerId, permissionId, accessibleIds, status, ateIds) VALUES (11, 'VM-Pub', 6, 1, 'ALL,', true, 'NA');
INSERT INTO ems_database.directorydetails (id, directoryName, managerId, permissionId, accessibleIds, status, ateIds) VALUES (12, 'VM-Prot', 6, 3, '6,2,7,9,', true, '5,');

create table employeedetails
(
    id           int auto_increment
        primary key,
    employeeId   int          not null,
    role         varchar(255) null,
    divisionName varchar(255) null,
    supervisor   varchar(255) null,
    status       bit          null,
    constraint employee_id_for_employeeDetails
        foreign key (employeeId) references registration (id)
)
    charset = latin1;

INSERT INTO ems_database.employeedetails (id, employeeId, role, divisionName, supervisor, status) VALUES (1, 3, 'Software Engineer', 'Procurment', 'Kate Winslet', true);
INSERT INTO ems_database.employeedetails (id, employeeId, role, divisionName, supervisor, status) VALUES (2, 1, 'Company Administrator', 'Company', 'Gagan Shetty', true);
INSERT INTO ems_database.employeedetails (id, employeeId, role, divisionName, supervisor, status) VALUES (3, 2, 'Company Manager', 'Company', 'Kate Winslet', true);
INSERT INTO ems_database.employeedetails (id, employeeId, role, divisionName, supervisor, status) VALUES (4, 4, 'Sub Manager 1', 'NA', 'Kate Winslet', true);

create table leavedetails
(
    id            int auto_increment
        primary key,
    employeeId    int           not null,
    managerId     int           not null,
    details       varchar(5000) null,
    requestStatus bit           null,
    response      varchar(20)   null,
    date          date          null,
    status        bit           null,
    constraint employeeId_for_leaveDetails
        foreign key (employeeId) references registration (id),
    constraint managerId_for_leaves
        foreign key (managerId) references registration (id)
)
    charset = latin1;

INSERT INTO ems_database.leavedetails (id, employeeId, managerId, details, requestStatus, response, date, status) VALUES (1, 3, 2, 'I need leave tomorrow.', false, 'Accepted', '2017-05-08', true);
INSERT INTO ems_database.leavedetails (id, employeeId, managerId, details, requestStatus, response, date, status) VALUES (2, 9, 6, 'I need leave tomorrow', true, 'NA', '2017-05-11', true);

create table leaves
(
    id             int auto_increment
        primary key,
    leaveNum       int null,
    registrationId int not null,
    status         bit null,
    constraint registration_id_for_leaves
        foreign key (registrationId) references registration (id)
)
    charset = latin1;

INSERT INTO ems_database.leaves (id, leaveNum, registrationId, status) VALUES (1, 4, 1, true);
INSERT INTO ems_database.leaves (id, leaveNum, registrationId, status) VALUES (2, 4, 2, true);
INSERT INTO ems_database.leaves (id, leaveNum, registrationId, status) VALUES (3, 3, 3, true);
INSERT INTO ems_database.leaves (id, leaveNum, registrationId, status) VALUES (4, 4, 4, true);
INSERT INTO ems_database.leaves (id, leaveNum, registrationId, status) VALUES (5, 4, 5, true);
INSERT INTO ems_database.leaves (id, leaveNum, registrationId, status) VALUES (6, 4, 6, true);
INSERT INTO ems_database.leaves (id, leaveNum, registrationId, status) VALUES (7, 4, 7, true);
INSERT INTO ems_database.leaves (id, leaveNum, registrationId, status) VALUES (8, 4, 8, true);
INSERT INTO ems_database.leaves (id, leaveNum, registrationId, status) VALUES (9, 4, 9, true);

create table payment
(
    id             int auto_increment
        primary key,
    registrationId int          not null,
    accountNum     varchar(255) null,
    salary         double       null,
    bonus          double       null,
    total          double       null,
    status         bit          null,
    constraint registration_id_for_payment
        foreign key (registrationId) references registration (id)
)
    charset = latin1;

INSERT INTO ems_database.payment (id, registrationId, accountNum, salary, bonus, total, status) VALUES (1, 1, 'NA', 100000, 0, 0, true);
INSERT INTO ems_database.payment (id, registrationId, accountNum, salary, bonus, total, status) VALUES (2, 2, 'NA', 100000, 0, 0, true);
INSERT INTO ems_database.payment (id, registrationId, accountNum, salary, bonus, total, status) VALUES (3, 3, 'NA', 100000, 1000, 0, true);
INSERT INTO ems_database.payment (id, registrationId, accountNum, salary, bonus, total, status) VALUES (4, 4, 'NA', 100000, 0, 0, true);
INSERT INTO ems_database.payment (id, registrationId, accountNum, salary, bonus, total, status) VALUES (5, 5, 'NA', 100000, 0, 8333.33333333333, true);
INSERT INTO ems_database.payment (id, registrationId, accountNum, salary, bonus, total, status) VALUES (6, 6, 'NA', 100000, 0, 0, true);
INSERT INTO ems_database.payment (id, registrationId, accountNum, salary, bonus, total, status) VALUES (7, 7, 'NA', 100000, 0, 0, true);
INSERT INTO ems_database.payment (id, registrationId, accountNum, salary, bonus, total, status) VALUES (8, 8, 'NA', 100000, 0, 0, true);
INSERT INTO ems_database.payment (id, registrationId, accountNum, salary, bonus, total, status) VALUES (9, 9, 'NA', 100000, 0, 0, true);

create table employeedocs
(
    id             int auto_increment
        primary key,
    docName        varchar(255)  null,
    docFile        longblob      null,
    docType        varchar(255)  null,
    comment        varchar(1000) null,
    registrationId int           not null,
    directoryId    int           not null,
    date           date          null,
    status         bit           null,
    constraint directory_id_for_employeeDocs
        foreign key (directoryId) references directorydetails (id),
    constraint registrationId_for_documents
        foreign key (registrationId) references registration (id)
)
    charset = latin1;

INSERT INTO ems_database.employeedocs (id, docName, docFile, docType, comment, registrationId, directoryId, date, status) VALUES (1, 'Reports', '' , 'image/png', 'RCB captain.', 5, 2, '2017-05-10', true);
INSERT INTO ems_database.employeedocs (id, docName, docFile, docType, comment, registrationId, directoryId, date, status) VALUES (2, 'Imagw', '', 'image/png', 'Image', 9, 4, '2017-05-11', true);
INSERT INTO ems_database.employeedocs (id, docName, docFile, docType, comment, registrationId, directoryId, date, status) VALUES (3, 'Prueba','', 'image/png', 'Prueba de imagen', 3, 1, '2025-06-10', true);



