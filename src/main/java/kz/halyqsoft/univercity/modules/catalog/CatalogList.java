package kz.halyqsoft.univercity.modules.catalog;

import kz.halyqsoft.univercity.entity.beans.TASKS;
import kz.halyqsoft.univercity.entity.beans.ROLES;
import kz.halyqsoft.univercity.entity.beans.univercity.catalog.*;
import kz.halyqsoft.univercity.entity.beans.univercity.view.V_ACADEMIC_DEGREE;
import kz.halyqsoft.univercity.entity.beans.univercity.view.V_ORGANIZATION;
import kz.halyqsoft.univercity.entity.beans.univercity.view.V_SEMESTER_DATA;
import kz.halyqsoft.univercity.entity.beans.univercity.view.V_SPECIALITY;
import org.r3a.common.entity.Entity;
import org.r3a.common.entity.ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Omarbek
 * @created on 13.03.2018
 */
class CatalogList {

    private static Set<Entity> list;
    private static int id;

    static List<Entity> getCatalogList() {
        id = 0;
        list = new TreeSet<>(new CatalogEntityComparator());

        addCatalogEntityToList(NATIONALITY.class);
        addCatalogEntityToList(AWARD.class);
        addCatalogEntityToList(CONTRACT_TYPE.class);
        addCatalogEntityToList(DEGREE.class);
        addCatalogEntityToList(ENTRANCE_YEAR.class);
        addCatalogEntityToList(RATING_SCALE.class);
        addCatalogEntityToList(LANGUAGE_LEVEL.class);
        addCatalogEntityToList(LANGUAGE.class);
        addCatalogEntityToList(EQUIPMENT.class);
        addCatalogEntityToList(MEDICAL_CHECKUP_TYPE.class);
        addCatalogEntityToList(MILITARY_STATUS.class);
        addCatalogEntityToList(ORDER_TYPE.class);
        addCatalogEntityToList(POST.class);
        addCatalogEntityToList(CORPUS.class);
        addCatalogEntityToList(SCHOOL_TYPE.class);
        addCatalogEntityToList(SOCIAL_CATEGORY.class);
        addCatalogEntityToList(STUDENT_STATUS.class);
        addCatalogEntityToList(UNIVERSITY.class);
        addCatalogEntityToList(UNT_SUBJECT.class);
        addCatalogEntityToList(COUNTRY.class);
        addCatalogEntityToList(DEPARTMENT.class);
        addCatalogEntityToList(MARITAL_STATUS.class);
        addCatalogEntityToList(STUDENT_CATEGORY.class);
        addCatalogEntityToList(LOCK_REASON.class);
        addCatalogEntityToList(CREATIVE_EXAM_SUBJECT.class);

        addCatalogEntityToList(SPECIALITY.class);
        addCatalogEntityToList(ACADEMIC_DEGREE.class);
        addCatalogEntityToList(ORGANIZATION.class);
        addCatalogEntityToList(SEMESTER_DATA.class);

        addCatalogEntityToList(ROLES.class);
        addCatalogEntityToList(TASKS.class);
        addCatalogEntityToList(EDUCATION_MODULE_TYPE.class);
        addCatalogEntityToList(STUDY_DIRECT.class);
        addCatalogEntityToList(ACADEMIC_FORMULA.class);

        return new ArrayList<>(list);
    }

    private static void addCatalogEntityToList(Class<? extends Entity> entityClass) {
        CatalogEntity ce = new CatalogEntity();
        ce.setId(ID.valueOf(id++));
        ce.setEntityClass(entityClass);
        list.add(ce);
    }
}
