CREATE TABLE turnstile_type (
  id        BIGINT                 NOT NULL,
  type_name CHARACTER VARYING(100) NOT NULL
);

ALTER TABLE ONLY turnstile_type
  ADD CONSTRAINT pk_turnstile_type PRIMARY KEY (id);

INSERT INTO turnstile_type VALUES (1, 'Первый');
INSERT INTO turnstile_type VALUES (2, 'Второй');
INSERT INTO turnstile_type VALUES (3, 'Третий');
INSERT INTO turnstile_type VALUES (4, 'Четвертый');

ALTER TABLE user_arrival
  ADD COLUMN come_in BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE user_arrival
  ADD COLUMN turnstile_type_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE ONLY user_arrival
  ADD CONSTRAINT fk_user_arrival_turnstile_type FOREIGN KEY (turnstile_type_id) REFERENCES turnstile_type (id)
ON UPDATE RESTRICT ON DELETE RESTRICT;

CREATE OR REPLACE VIEW V_STUDENT AS
  SELECT
    stu.ID,
    usr.FIRST_NAME,
    usr.LAST_NAME,
    usr.MIDDLE_NAME,
    usr.FIRST_NAME_EN,
    usr.LAST_NAME_EN,
    usr.MIDDLE_NAME_EN,
    usr.BIRTH_DATE,
    usr.SEX_ID,
    c.SEX_NAME,
    usr.MARITAL_STATUS_ID,
    d.STATUS_NAME                  MARITAL_STATUS_NAME,
    usr.NATIONALITY_ID,
    e.NATION_NAME,
    usr.CITIZENSHIP_ID,
    f.COUNTRY_NAME                 CITIZENSHIP_NAME,
    usr.CODE                       USER_CODE,
    usr.LOGIN,
    usr.EMAIL,
    usr.PHONE_MOBILE,
    stu.LEVEL_ID,
    g.LEVEL_NAME,
    stu.CATEGORY_ID,
    h.CATEGORY_NAME,
    stu.ACADEMIC_STATUS_ID,
    i.STATUS_NAME                  ACADEMIC_STATUS_NAME,
    stu.NEED_DORM,
    stu.ENTRANCE_YEAR_ID,
    j.ENTRANCE_YEAR,
    j.BEGIN_YEAR                   ENTRANCE_BEGIN_YEAR,
    j.END_YEAR                     ENTRANCE_END_YEAR,
    k.FACULTY_ID,
    l.DEPT_NAME                    FACULTY_NAME,
    l.DEPT_SHORT_NAME              FACULTY_SHORT_NAME,
    l.CODE                         FACULTY_CODE,
    k.CHAIR_ID,
    m.DEPT_NAME                    CHAIR_NAME,
    m.DEPT_SHORT_NAME              CHAIR_SHORT_NAME,
    m.CODE                         CHAIR_CODE,
    k.SPECIALITY_ID,
    n.CODE || ' - ' || n.SPEC_NAME SPECIALITY_NAME,
    n.CODE                         SPECIALITY_CODE,
    k.STUDY_YEAR_ID,
    k.EDUCATION_TYPE_ID,
    o.TYPE_NAME                    EDUCATION_TYPE_NAME,
    k.ENTRY_DATE,
    k.END_DATE,
    k.STUDENT_STATUS_ID,
    p.STATUS_NAME                  STUDENT_STATUS_NAME,
    usr.DELETED,
    usr.CREATED,
    usr.UPDATED
  FROM STUDENT stu INNER JOIN USERS usr ON stu.ID = usr.ID
    INNER JOIN SEX c ON usr.SEX_ID = c.ID
    INNER JOIN MARITAL_STATUS d ON usr.MARITAL_STATUS_ID = d.ID
    INNER JOIN NATIONALITY e ON usr.NATIONALITY_ID = e.ID
    INNER JOIN COUNTRY f ON usr.CITIZENSHIP_ID = f.ID
    INNER JOIN LEVEL g ON stu.LEVEL_ID = g.ID
    INNER JOIN STUDENT_CATEGORY h ON stu.CATEGORY_ID = h.ID
    LEFT JOIN ACADEMIC_STATUS i ON stu.ACADEMIC_STATUS_ID = i.ID
    INNER JOIN ENTRANCE_YEAR j ON stu.ENTRANCE_YEAR_ID = j.ID
    INNER JOIN STUDENT_EDUCATION k ON k.STUDENT_ID = stu.ID AND k.CHILD_ID IS NULL
    INNER JOIN DEPARTMENT l ON k.FACULTY_ID = l.ID
    INNER JOIN DEPARTMENT m ON k.CHAIR_ID = m.ID
    INNER JOIN SPECIALITY n ON k.SPECIALITY_ID = n.ID
    INNER JOIN STUDENT_EDUCATION_TYPE o ON k.EDUCATION_TYPE_ID = o.ID
    INNER JOIN STUDENT_STATUS p ON k.STUDENT_STATUS_ID = p.ID;

CREATE OR REPLACE VIEW V_EMPLOYEE AS
  SELECT
    a.ID,
    b.FIRST_NAME,
    b.LAST_NAME,
    b.MIDDLE_NAME,
    b.FIRST_NAME_EN,
    b.LAST_NAME_EN,
    b.MIDDLE_NAME_EN,
    b.BIRTH_DATE,
    b.SEX_ID,
    c.SEX_NAME,
    b.MARITAL_STATUS_ID,
    d.STATUS_NAME  MARITAL_STATUS_NAME,
    b.NATIONALITY_ID,
    e.NATION_NAME,
    b.CITIZENSHIP_ID,
    f.COUNTRY_NAME CITIZENSHIP_NAME,
    b.CODE,
    b.LOGIN,
    b.EMAIL,
    b.PHONE_MOBILE,
    h.EMPLOYEE_TYPE_ID,
    i.TYPE_NAME    EMPLOYEE_TYPE_NAME,
    h.DEPT_ID,
    j.DEPT_NAME,
    j.DEPT_SHORT_NAME,
    j.CODE         DEPT_CODE,
    h.POST_ID,
    k.POST_NAME,
    h.LIVE_LOAD,
    h.HIRE_DATE,
    h.DISMISS_DATE,
    h.ADVISER,
    a.EMPLOYEE_STATUS_ID,
    g.STATUS_NAME  EMPLOYEE_STATUS_NAME,
    b.DELETED,
    b.CREATED,
    b.UPDATED
  FROM EMPLOYEE a INNER JOIN USERS b ON a.ID = b.ID
    INNER JOIN SEX c ON b.SEX_ID = c.ID
    INNER JOIN MARITAL_STATUS d ON b.MARITAL_STATUS_ID = d.ID
    LEFT JOIN NATIONALITY e ON b.NATIONALITY_ID = e.ID
    INNER JOIN COUNTRY f ON b.CITIZENSHIP_ID = f.ID
    INNER JOIN EMPLOYEE_STATUS g ON a.EMPLOYEE_STATUS_ID = g.ID AND g.ID != 4
    LEFT JOIN EMPLOYEE_DEPT h ON h.EMPLOYEE_ID = a.ID AND h.DISMISS_DATE IS NULL
    LEFT JOIN EMPLOYEE_TYPE i ON h.EMPLOYEE_TYPE_ID = i.ID
    LEFT JOIN DEPARTMENT j ON h.DEPT_ID = j.ID
    LEFT JOIN POST k ON h.POST_ID = k.ID
  WHERE b.id NOT IN (1, 2);