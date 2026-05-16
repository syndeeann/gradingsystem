-- =============================================================================
-- Grading System Database Schema
-- Database:   MariaDB 10.6
-- Created:    2026-05-16
-- Description: Complete schema for a school grading system supporting roles,
--              permissions, users, subjects, schedules, enrollments, grades,
--              and honor thresholds.
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;
SET sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- =============================================================================
-- DROP TABLES (safe re-run order)
-- =============================================================================
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS grades;
DROP TABLE IF EXISTS student_schedules;
DROP TABLE IF EXISTS schedules;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS honor_thresholds;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- TABLE: roles
-- =============================================================================
CREATE TABLE roles (
    id          TINYINT UNSIGNED    NOT NULL AUTO_INCREMENT,
    name        VARCHAR(20)         NOT NULL,
    description VARCHAR(255)        NOT NULL DEFAULT '',
    is_active   TINYINT(1)          NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_roles_name (name),
    CONSTRAINT chk_roles_name CHECK (name IN ('ADMIN', 'TEACHER', 'STUDENT', 'REGISTRAR', 'SECRETARY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE: permissions
-- =============================================================================
CREATE TABLE permissions (
    id          SMALLINT UNSIGNED   NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)        NOT NULL,
    resource    VARCHAR(30)         NOT NULL,
    action      VARCHAR(20)         NOT NULL,
    description VARCHAR(255)        NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    UNIQUE KEY uq_permissions_resource_action (resource, action),
    INDEX idx_permissions_resource (resource),
    CONSTRAINT chk_permissions_resource CHECK (resource IN ('GRADES', 'SUBJECTS', 'USERS', 'REPORTS', 'SCHEDULES', 'ENROLLMENTS', 'HONORS', 'STUDENTS', 'ROLES')),
    CONSTRAINT chk_permissions_action   CHECK (action   IN ('CREATE', 'READ', 'UPDATE', 'DELETE', 'VIEW_PAGE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE: role_permissions  (join table)
-- =============================================================================
CREATE TABLE role_permissions (
    role_id       TINYINT UNSIGNED    NOT NULL,
    permission_id SMALLINT UNSIGNED   NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    INDEX idx_role_permissions_permission_id (permission_id),
    CONSTRAINT fk_rp_role       FOREIGN KEY (role_id)       REFERENCES roles(id)       ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE: users
-- =============================================================================
CREATE TABLE users (
    id            CHAR(36)        NOT NULL,
    username      VARCHAR(50)     NOT NULL,
    email         VARCHAR(150)    NOT NULL,
    password_hash VARCHAR(255)    NOT NULL,
    full_name     VARCHAR(150)    NOT NULL,
    role_id       TINYINT UNSIGNED NOT NULL,
    is_active     TINYINT(1)      NOT NULL DEFAULT 1,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login    DATETIME                 DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username),
    UNIQUE KEY uq_users_email    (email),
    INDEX idx_users_role_id   (role_id),
    INDEX idx_users_is_active (is_active),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE: subjects
-- =============================================================================
CREATE TABLE subjects (
    id          INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    code        VARCHAR(20)     NOT NULL,
    name        VARCHAR(150)    NOT NULL,
    description TEXT                     DEFAULT NULL,
    units       TINYINT UNSIGNED NOT NULL DEFAULT 3,
    is_active   TINYINT(1)      NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_subjects_code (code),
    INDEX idx_subjects_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE: schedules
-- =============================================================================
CREATE TABLE schedules (
    id          INT UNSIGNED        NOT NULL AUTO_INCREMENT,
    subject_id  INT UNSIGNED        NOT NULL,
    teacher_id  CHAR(36)            NOT NULL,
    room        VARCHAR(50)         NOT NULL DEFAULT '',
    day_of_week VARCHAR(30)         NOT NULL,
    time_start  TIME                NOT NULL,
    time_end    TIME                NOT NULL,
    semester    TINYINT UNSIGNED    NOT NULL COMMENT '1=First, 2=Second, 3=Summer',
    school_year VARCHAR(9)          NOT NULL COMMENT 'e.g. 2025-2026',
    is_active   TINYINT(1)          NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    INDEX idx_schedules_subject_id  (subject_id),
    INDEX idx_schedules_teacher_id  (teacher_id),
    INDEX idx_schedules_school_year (school_year),
    INDEX idx_schedules_semester    (semester),
    CONSTRAINT fk_schedules_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_schedules_teacher FOREIGN KEY (teacher_id) REFERENCES users(id)    ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_schedules_time   CHECK (time_end > time_start),
    CONSTRAINT chk_schedules_sem    CHECK (semester IN (1, 2, 3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE: student_schedules  (enrollments)
-- =============================================================================
CREATE TABLE student_schedules (
    id               INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    student_id       CHAR(36)        NOT NULL,
    schedule_id      INT UNSIGNED    NOT NULL,
    enrollment_date  DATE            NOT NULL DEFAULT (CURRENT_DATE),
    status           VARCHAR(10)     NOT NULL DEFAULT 'ENROLLED',
    PRIMARY KEY (id),
    UNIQUE KEY uq_student_schedule (student_id, schedule_id),
    INDEX idx_ss_student_id  (student_id),
    INDEX idx_ss_schedule_id (schedule_id),
    INDEX idx_ss_status      (status),
    CONSTRAINT fk_ss_student  FOREIGN KEY (student_id)  REFERENCES users(id)      ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ss_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id)  ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_ss_status  CHECK (status IN ('ENROLLED', 'DROPPED', 'COMPLETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE: grades
-- =============================================================================
CREATE TABLE grades (
    id                   INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    student_schedule_id  INT UNSIGNED    NOT NULL,
    grade_value          DECIMAL(5,2)    NOT NULL,
    remarks              VARCHAR(255)             DEFAULT NULL,
    recorded_by          CHAR(36)        NOT NULL,
    recorded_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_grades_student_schedule (student_schedule_id),
    INDEX idx_grades_recorded_by (recorded_by),
    INDEX idx_grades_grade_value (grade_value),
    CONSTRAINT fk_grades_student_schedule FOREIGN KEY (student_schedule_id) REFERENCES student_schedules(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_grades_recorded_by      FOREIGN KEY (recorded_by)         REFERENCES users(id)            ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_grades_value           CHECK (grade_value BETWEEN 0.00 AND 100.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- TABLE: honor_thresholds
-- =============================================================================
CREATE TABLE honor_thresholds (
    id         TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    label      VARCHAR(50)      NOT NULL,
    min_grade  DECIMAL(5,2)     NOT NULL,
    max_grade  DECIMAL(5,2)     NOT NULL,
    is_active  TINYINT(1)       NOT NULL DEFAULT 1,
    created_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_honor_label (label),
    CONSTRAINT chk_honor_label CHECK (LENGTH(label) > 0),
    CONSTRAINT chk_honor_range CHECK (max_grade > min_grade)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================================================
-- SEED DATA
-- =============================================================================

-- ----------------------------------------------------------------------------
-- Roles (5)
-- ----------------------------------------------------------------------------
INSERT INTO roles (id, name, description, is_active) VALUES
(1, 'ADMIN',      'Full system access',                          1),
(2, 'TEACHER',    'Manages subjects and records student grades',  1),
(3, 'STUDENT',    'Views own grades and schedule',               1),
(4, 'REGISTRAR',  'Manages enrollments and official records',    1),
(5, 'SECRETARY',  'Administrative support, view-only reports',   1);

-- ----------------------------------------------------------------------------
-- Permissions — every resource × action combo
-- ----------------------------------------------------------------------------
INSERT INTO permissions (name, resource, action, description) VALUES
-- GRADES
('grades:create',    'GRADES',      'CREATE',    'Create a new grade entry'),
('grades:read',      'GRADES',      'READ',      'Read grade records'),
('grades:update',    'GRADES',      'UPDATE',    'Update an existing grade'),
('grades:delete',    'GRADES',      'DELETE',    'Delete a grade entry'),
('grades:view_page', 'GRADES',      'VIEW_PAGE', 'Access the Grades page'),
-- SUBJECTS
('subjects:create',    'SUBJECTS',  'CREATE',    'Create a new subject'),
('subjects:read',      'SUBJECTS',  'READ',      'Read subject records'),
('subjects:update',    'SUBJECTS',  'UPDATE',    'Update a subject'),
('subjects:delete',    'SUBJECTS',  'DELETE',    'Delete a subject'),
('subjects:view_page', 'SUBJECTS',  'VIEW_PAGE', 'Access the Subjects page'),
-- USERS
('users:create',    'USERS',        'CREATE',    'Create a new user account'),
('users:read',      'USERS',        'READ',      'Read user records'),
('users:update',    'USERS',        'UPDATE',    'Update a user account'),
('users:delete',    'USERS',        'DELETE',    'Delete a user account'),
('users:view_page', 'USERS',        'VIEW_PAGE', 'Access the Users page'),
-- REPORTS
('reports:create',    'REPORTS',    'CREATE',    'Generate new reports'),
('reports:read',      'REPORTS',    'READ',      'Read existing reports'),
('reports:update',    'REPORTS',    'UPDATE',    'Update report settings'),
('reports:delete',    'REPORTS',    'DELETE',    'Delete reports'),
('reports:view_page', 'REPORTS',    'VIEW_PAGE', 'Access the Reports page'),
-- SCHEDULES
('schedules:create',    'SCHEDULES','CREATE',    'Create a schedule'),
('schedules:read',      'SCHEDULES','READ',      'Read schedules'),
('schedules:update',    'SCHEDULES','UPDATE',    'Update a schedule'),
('schedules:delete',    'SCHEDULES','DELETE',    'Delete a schedule'),
('schedules:view_page', 'SCHEDULES','VIEW_PAGE', 'Access the Schedules page'),
-- ENROLLMENTS
('enrollments:create',    'ENROLLMENTS','CREATE',    'Enroll a student'),
('enrollments:read',      'ENROLLMENTS','READ',      'Read enrollment records'),
('enrollments:update',    'ENROLLMENTS','UPDATE',    'Update enrollment status'),
('enrollments:delete',    'ENROLLMENTS','DELETE',    'Remove an enrollment'),
('enrollments:view_page', 'ENROLLMENTS','VIEW_PAGE', 'Access the Enrollments page'),
-- HONORS
('honors:create',    'HONORS',   'CREATE',    'Create honor thresholds'),
('honors:read',      'HONORS',   'READ',      'Read honor records and honor roll'),
('honors:update',    'HONORS',   'UPDATE',    'Update or deactivate honor thresholds'),
('honors:view_page', 'HONORS',   'VIEW_PAGE', 'Access the Honors page'),
-- STUDENTS
('students:read',      'STUDENTS', 'READ',      'Read student profile'),
('students:view_page', 'STUDENTS', 'VIEW_PAGE', 'Access the Students page'),
-- ROLES
('roles:read',   'ROLES', 'READ',   'Read role and permission data'),
('roles:update', 'ROLES', 'UPDATE', 'Update role permissions');

-- ----------------------------------------------------------------------------
-- role_permissions — assign permissions per role
-- Resource filter is used; all IDs are resolved dynamically from the names.
-- ----------------------------------------------------------------------------

-- ADMIN: all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- TEACHER: grades (create/read/update/view), subjects/schedules/enrollments/honors/students (read/view)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE (resource = 'GRADES'      AND action IN ('CREATE','READ','UPDATE','VIEW_PAGE'))
                                 OR (resource = 'SUBJECTS'     AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'SCHEDULES'    AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'REPORTS'      AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'ENROLLMENTS'  AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'STUDENTS'     AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'HONORS'       AND action IN ('READ','VIEW_PAGE'));

-- STUDENT: read own grades/schedules/subjects/enrollments/honors
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions WHERE (resource = 'GRADES'      AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'SCHEDULES'    AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'SUBJECTS'     AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'ENROLLMENTS'  AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'HONORS'       AND action IN ('READ','VIEW_PAGE'));

-- REGISTRAR: manage enrollments + grades, read users/subjects/schedules/students/honors, view reports
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions WHERE (resource = 'ENROLLMENTS')
                                 OR (resource = 'GRADES'       AND action IN ('CREATE','READ','UPDATE','VIEW_PAGE'))
                                 OR (resource = 'USERS'        AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'SUBJECTS'     AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'SCHEDULES'    AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'REPORTS'      AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'STUDENTS'     AND action IN ('READ','VIEW_PAGE'))
                                 OR (resource = 'HONORS'       AND action IN ('READ','VIEW_PAGE'));

-- SECRETARY: view-only on most resources
INSERT INTO role_permissions (role_id, permission_id)
SELECT 5, id FROM permissions WHERE action IN ('READ','VIEW_PAGE')
                                AND resource IN ('GRADES','SUBJECTS','SCHEDULES','REPORTS','ENROLLMENTS','STUDENTS','HONORS');

-- ----------------------------------------------------------------------------
-- Users
-- Passwords are bcrypt hashes of 'Password@123' (cost 12) — replace in prod.
-- Hash verified: $2b$12$bysxkBQVfAyAIqm4f2TDuOaYF/V2GTiIZ8qgZjcSPl/z1nU3izQz2
-- ----------------------------------------------------------------------------
INSERT INTO users (id, username, email, password_hash, full_name, role_id, is_active) VALUES
-- Admins (2)
('a0000001-0000-0000-0000-000000000001', 'admin1',       'admin1@school.edu',       '$2b$12$bysxkBQVfAyAIqm4f2TDuOaYF/V2GTiIZ8qgZjcSPl/z1nU3izQz2', 'Alice Administrator',  1, 1),
('a0000001-0000-0000-0000-000000000002', 'admin2',       'admin2@school.edu',       '$2b$12$bysxkBQVfAyAIqm4f2TDuOaYF/V2GTiIZ8qgZjcSPl/z1nU3izQz2', 'Bob Administrator',    1, 1),
-- Teachers (2)
('t0000001-0000-0000-0000-000000000001', 'teacher1',     'teacher1@school.edu',     '$2b$12$bysxkBQVfAyAIqm4f2TDuOaYF/V2GTiIZ8qgZjcSPl/z1nU3izQz2', 'Carlos Reyes',         2, 1),
('t0000001-0000-0000-0000-000000000002', 'teacher2',     'teacher2@school.edu',     '$2b$12$bysxkBQVfAyAIqm4f2TDuOaYF/V2GTiIZ8qgZjcSPl/z1nU3izQz2', 'Diana Santos',         2, 1),
-- Registrar (1)
('r0000001-0000-0000-0000-000000000001', 'registrar1',   'registrar1@school.edu',   '$2b$12$bysxkBQVfAyAIqm4f2TDuOaYF/V2GTiIZ8qgZjcSPl/z1nU3izQz2', 'Elena Cruz',           4, 1),
-- Secretary (1)
('s0000001-0000-0000-0000-000000000001', 'secretary1',   'secretary1@school.edu',   '$2b$12$bysxkBQVfAyAIqm4f2TDuOaYF/V2GTiIZ8qgZjcSPl/z1nU3izQz2', 'Fernando Lim',         5, 1),
-- Students (5)
('u0000001-0000-0000-0000-000000000001', 'student1',     'student1@school.edu',     '$2b$12$bysxkBQVfAyAIqm4f2TDuOaYF/V2GTiIZ8qgZjcSPl/z1nU3izQz2', 'Grace Tan',            3, 1),
('u0000001-0000-0000-0000-000000000002', 'student2',     'student2@school.edu',     '$2b$12$bysxkBQVfAyAIqm4f2TDuOaYF/V2GTiIZ8qgZjcSPl/z1nU3izQz2', 'Henry Uy',             3, 1),
('u0000001-0000-0000-0000-000000000003', 'student3',     'student3@school.edu',     '$2b$12$bysxkBQVfAyAIqm4f2TDuOaYF/V2GTiIZ8qgZjcSPl/z1nU3izQz2', 'Iris Bautista',        3, 1),
('u0000001-0000-0000-0000-000000000004', 'student4',     'student4@school.edu',     '$2b$12$bysxkBQVfAyAIqm4f2TDuOaYF/V2GTiIZ8qgZjcSPl/z1nU3izQz2', 'James Flores',         3, 1),
('u0000001-0000-0000-0000-000000000005', 'student5',     'student5@school.edu',     '$2b$12$bysxkBQVfAyAIqm4f2TDuOaYF/V2GTiIZ8qgZjcSPl/z1nU3izQz2', 'Karen Dela Cruz',      3, 1);

-- ----------------------------------------------------------------------------
-- Subjects (3)
-- ----------------------------------------------------------------------------
INSERT INTO subjects (id, code, name, description, units, is_active) VALUES
(1, 'CS101',  'Introduction to Computing',    'Fundamentals of computer science and programming',    3, 1),
(2, 'MATH101', 'Mathematics in the Modern World', 'Applied mathematics for non-math majors',         3, 1),
(3, 'ENG101',  'Purposive Communication',     'Academic and professional communication skills',      3, 1);

-- ----------------------------------------------------------------------------
-- Schedules (2)
-- ----------------------------------------------------------------------------
INSERT INTO schedules (id, subject_id, teacher_id, room, day_of_week, time_start, time_end, semester, school_year, is_active) VALUES
(1, 1, 't0000001-0000-0000-0000-000000000001', 'Room 101', 'Monday/Wednesday/Friday', '08:00:00', '09:00:00', 1, '2025-2026', 1),
(2, 2, 't0000001-0000-0000-0000-000000000002', 'Room 205', 'Tuesday/Thursday',        '10:00:00', '11:30:00', 1, '2025-2026', 1);

-- ----------------------------------------------------------------------------
-- Student Schedules / Enrollments
-- ----------------------------------------------------------------------------
INSERT INTO student_schedules (id, student_id, schedule_id, enrollment_date, status) VALUES
(1,  'u0000001-0000-0000-0000-000000000001', 1, '2025-06-10', 'ENROLLED'),
(2,  'u0000001-0000-0000-0000-000000000002', 1, '2025-06-10', 'ENROLLED'),
(3,  'u0000001-0000-0000-0000-000000000003', 1, '2025-06-10', 'ENROLLED'),
(4,  'u0000001-0000-0000-0000-000000000004', 1, '2025-06-10', 'DROPPED'),
(5,  'u0000001-0000-0000-0000-000000000005', 1, '2025-06-10', 'COMPLETED'),
(6,  'u0000001-0000-0000-0000-000000000001', 2, '2025-06-10', 'ENROLLED'),
(7,  'u0000001-0000-0000-0000-000000000002', 2, '2025-06-10', 'ENROLLED'),
(8,  'u0000001-0000-0000-0000-000000000003', 2, '2025-06-10', 'COMPLETED'),
(9,  'u0000001-0000-0000-0000-000000000004', 2, '2025-06-10', 'ENROLLED'),
(10, 'u0000001-0000-0000-0000-000000000005', 2, '2025-06-10', 'COMPLETED');

-- ----------------------------------------------------------------------------
-- Grades
-- (Only COMPLETED and ENROLLED records that have been graded)
-- ----------------------------------------------------------------------------
INSERT INTO grades (student_schedule_id, grade_value, remarks, recorded_by, recorded_at) VALUES
-- Schedule 1 (CS101) grades
(1,  88.50, 'Good performance',        't0000001-0000-0000-0000-000000000001', '2025-10-15 09:00:00'),
(2,  92.00, 'Excellent',               't0000001-0000-0000-0000-000000000001', '2025-10-15 09:05:00'),
(3,  75.00, 'Satisfactory',            't0000001-0000-0000-0000-000000000001', '2025-10-15 09:10:00'),
(5,  96.75, 'Outstanding performance', 't0000001-0000-0000-0000-000000000001', '2025-10-15 09:15:00'),
-- Schedule 2 (MATH101) grades
(8,  89.00, 'Very good',               't0000001-0000-0000-0000-000000000002', '2025-10-16 10:00:00'),
(10, 93.50, 'Excellent',               't0000001-0000-0000-0000-000000000002', '2025-10-16 10:05:00');

-- ----------------------------------------------------------------------------
-- Honor Thresholds
-- ----------------------------------------------------------------------------
INSERT INTO honor_thresholds (label, min_grade, max_grade, is_active) VALUES
('WITH HONORS',         88.00, 91.99, 1),
('WITH HIGH HONORS',    92.00, 95.99, 1),
('WITH HIGHEST HONORS', 96.00, 100.00, 1);

-- =============================================================================
-- TABLE: audit_log
-- =============================================================================
CREATE TABLE audit_log (
    id         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    user_id    CHAR(36)         NULL COMMENT 'NULL for system-initiated actions',
    action     VARCHAR(50)      NOT NULL,
    entity     VARCHAR(50)      NOT NULL,
    entity_id  VARCHAR(36)      NULL,
    created_at DATETIME(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_audit_user   (user_id),
    INDEX idx_audit_entity (entity, entity_id),
    INDEX idx_audit_ts     (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
