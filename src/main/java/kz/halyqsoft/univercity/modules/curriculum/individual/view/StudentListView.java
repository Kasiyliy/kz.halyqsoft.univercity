package kz.halyqsoft.univercity.modules.curriculum.individual.view;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import kz.halyqsoft.univercity.entity.beans.univercity.catalog.*;
import kz.halyqsoft.univercity.entity.beans.univercity.enumeration.UserType;
import kz.halyqsoft.univercity.entity.beans.univercity.view.VStudent;
import kz.halyqsoft.univercity.filter.FStudentFilter;
import kz.halyqsoft.univercity.filter.FSubjectFilter;
import kz.halyqsoft.univercity.filter.panel.StudentFilterPanel;
import org.r3a.common.dblink.facade.CommonEntityFacadeBean;
import org.r3a.common.dblink.utils.SessionFacadeFactory;
import org.r3a.common.entity.Entity;
import org.r3a.common.entity.ID;
import org.r3a.common.entity.beans.AbstractTask;
import org.r3a.common.entity.event.EntityEvent;
import org.r3a.common.entity.event.EntityListener;
import org.r3a.common.entity.query.QueryModel;
import org.r3a.common.entity.query.from.EJoin;
import org.r3a.common.entity.query.from.FromItem;
import org.r3a.common.entity.query.where.ECriteria;
import org.r3a.common.vaadin.view.AbstractTaskView;
import org.r3a.common.vaadin.widget.ERefreshType;
import org.r3a.common.vaadin.widget.dialog.Message;
import org.r3a.common.vaadin.widget.filter2.AbstractFilterBean;
import org.r3a.common.vaadin.widget.filter2.FilterPanelListener;
import org.r3a.common.vaadin.widget.grid.GridWidget;
import org.r3a.common.vaadin.widget.grid.model.DBGridModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Omarbek Dinassil
 * @created Mar 27, 2017 4:40:23 PM
 */
@SuppressWarnings({"serial", "unchecked"})
public class StudentListView extends AbstractTaskView implements EntityListener, FilterPanelListener {

    private final StudentFilterPanel filterPanel;
    private GridWidget studentGW;

    public StudentListView(AbstractTask task) throws Exception {
        super(task);
        filterPanel = new StudentFilterPanel(new FStudentFilter());
    }

    @Override
    public void initView(boolean readOnly) throws Exception {
        filterPanel.addFilterPanelListener(this);
        TextField tf = new TextField();
        tf.setNullRepresentation("");
        tf.setNullSettingAllowed(true);
        filterPanel.addFilterComponent("code", tf);

//        tf = new TextField();
//        tf.setNullRepresentation("");
//        tf.setNullSettingAllowed(true);
//        filterPanel.addFilterComponent("firstname", tf);
//
//        tf = new TextField();
//        tf.setNullRepresentation("");
//        tf.setNullSettingAllowed(true);
//        filterPanel.addFilterComponent("lastname", tf);

        ComboBox cb = new ComboBox();
        cb.setNullSelectionAllowed(true);
        cb.setTextInputAllowed(false);
        cb.setFilteringMode(FilteringMode.OFF);
        List<ID> idList = new ArrayList<ID>(4);
        idList.add(ID.valueOf(1));
        idList.add(ID.valueOf(2));
        idList.add(ID.valueOf(3));
        idList.add(ID.valueOf(5));
        QueryModel<STUDENT_STATUS> ssQM = new QueryModel<STUDENT_STATUS>(STUDENT_STATUS.class);
        ssQM.addWhereIn("id", idList);
        ssQM.addOrder("id");
        BeanItemContainer<STUDENT_STATUS> ssBIC = new BeanItemContainer<STUDENT_STATUS>(STUDENT_STATUS.class, SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookup(ssQM));
        cb.setContainerDataSource(ssBIC);
        filterPanel.addFilterComponent("studentStatus", cb);

        cb = new ComboBox();
        cb.setNullSelectionAllowed(true);
        cb.setTextInputAllowed(false);
        cb.setFilteringMode(FilteringMode.OFF);
        QueryModel<LOCK_REASON> lrQM = new QueryModel<LOCK_REASON>(LOCK_REASON.class);
        lrQM.addWhere("userType", ECriteria.EQUAL, USER_TYPE.STUDENT_ID);
        lrQM.addOrder("id");
        BeanItemContainer<LOCK_REASON> lrBIC = new BeanItemContainer<>(LOCK_REASON.class,
                SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookup(lrQM));
        cb.setContainerDataSource(lrBIC);
        filterPanel.addFilterComponent("lockReason", cb);

        cb = new ComboBox();
        cb.setNullSelectionAllowed(true);
        cb.setTextInputAllowed(true);
        cb.setFilteringMode(FilteringMode.CONTAINS);
        cb.setPageLength(0);
        cb.setWidth(250, Unit.PIXELS);
        QueryModel<DEPARTMENT> facultyQM = new QueryModel<>(DEPARTMENT.class);
        facultyQM.addWhereNull("parent");
        facultyQM.addWhereAnd("deleted", Boolean.FALSE);
        facultyQM.addOrder("deptName");
        BeanItemContainer<DEPARTMENT> facultyBIC = new BeanItemContainer<DEPARTMENT>(DEPARTMENT.class, SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookup(facultyQM));
        cb.setContainerDataSource(facultyBIC);
        filterPanel.addFilterComponent("faculty", cb);

        ComboBox specialtyCB = new ComboBox();
        specialtyCB.setNullSelectionAllowed(true);
        specialtyCB.setTextInputAllowed(true);
        specialtyCB.setFilteringMode(FilteringMode.CONTAINS);
        specialtyCB.setPageLength(0);
        specialtyCB.setWidth(250, Unit.PIXELS);
        cb.addValueChangeListener(new FacultyChangeListener(specialtyCB));
        filterPanel.addFilterComponent("speciality", specialtyCB);

        getContent().addComponent(filterPanel);
        getContent().setComponentAlignment(filterPanel, Alignment.TOP_CENTER);

        studentGW = new GridWidget(VStudent.class);
        studentGW.showToolbar(false);
        studentGW.addEntityListener(this);
        DBGridModel studentGM = (DBGridModel) studentGW.getWidgetModel();
        studentGM.setReadOnly(true);
        studentGM.setRefreshType(ERefreshType.MANUAL);
        studentGM.setMultiSelect(false);
        studentGM.setRowNumberVisible(true);
        studentGM.setRowNumberWidth(50);

        doFilter(filterPanel.getFilterBean());

        getContent().addComponent(studentGW);
        getContent().setComponentAlignment(studentGW, Alignment.MIDDLE_CENTER);
    }

    @Override
    public void doFilter(AbstractFilterBean filterBean) {
        FStudentFilter sf = (FStudentFilter) filterBean;
        int i = 1;
        Map<Integer, Object> params = new HashMap<Integer, Object>();
        StringBuilder sb = new StringBuilder();
        if (sf.getCode() != null && sf.getCode().trim().length() >= 2) {
            sb.append("lower(b.CODE) like '");
            sb.append(sf.getCode().trim().toLowerCase());
            sb.append("%'");
        }
//        if (sf.getFirstname() != null && sf.getFirstname().trim().length() >= 3) {
//            if (sb.length() > 0) {
//                sb.append(" and ");
//            }
//            sb.append("lower(b.FIRST_NAME) like '");
//            sb.append(sf.getFirstname().trim().toLowerCase());
//            sb.append("%'");
//        }
//        if (sf.getLastname() != null && sf.getLastname().trim().length() >= 3) {
//            if (sb.length() > 0) {
//                sb.append(" and ");
//            }
//            sb.append("lower(b.LAST_NAME) like '");
//            sb.append(sf.getLastname().trim().toLowerCase());
//            sb.append("%'");
//        }
        if (sf.getStudentStatus() != null) {
            params.put(i, sf.getStudentStatus().getId().getId());
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("d.STUDENT_STATUS_ID = ?" + i++);
        }
        if (sf.getLockReason() != null) {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            params.put(i, 1);
            sb.append("b.LOCKED = ?" + i++);
            sb.append(" and ");
            params.put(i, sf.getLockReason().getId().getId());
            sb.append("b.LOCK_REASON_ID = ?" + i++);
        }
        if (sf.getFaculty() != null) {
            params.put(i, sf.getFaculty().getId().getId());
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("d.FACULTY_ID = ?" + i++);
        }
        if (sf.getSpeciality() != null) {
            params.put(i, sf.getSpeciality().getId().getId());
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("d.SPECIALITY_ID = ?" + i++);
        }

        List<VStudent> list = new ArrayList<VStudent>();
        if (sb.length() > 0) {
            sb.insert(0, " where ");
            String sql = "SELECT " +
                    "  a.ID, " +
                    "  b.CODE, " +
                    "  trim(b.LAST_NAME || ' ' || b.FIRST_NAME || ' ' || coalesce(b.MIDDLE_NAME, '')) FIO, " +
                    "  c.CATEGORY_NAME, " +
                    "  e.STATUS_NAME, " +
                    "  f.DEPT_SHORT_NAME                                                              FACULTY, " +
                    "  g.SPEC_NAME, " +
                    "  CASE WHEN b.locked = FALSE " +
                    "    THEN NULL " +
                    "  ELSE h.reason END                                                              LOCK_REASON " +
                    "FROM STUDENT a INNER JOIN USERS b ON a.ID = b.ID " +
                    "  INNER JOIN STUDENT_CATEGORY c ON a.CATEGORY_ID = c.ID " +
                    "  INNER JOIN STUDENT_EDUCATION d ON a.ID = d.STUDENT_ID AND d.CHILD_ID IS NULL " +
                    "  INNER JOIN STUDENT_STATUS e ON d.STUDENT_STATUS_ID = e.ID " +
                    "  INNER JOIN DEPARTMENT f ON d.FACULTY_ID = f.ID " +
                    "  INNER JOIN SPECIALITY g ON d.SPECIALITY_ID = g.ID " +
                    "  LEFT JOIN LOCK_REASON h ON b.LOCK_REASON_ID = h.ID "
                    + sb.toString() +
                    " ORDER BY FIO;";
            try {
                List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sql, params);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;
                        VStudent vs = new VStudent();
                        vs.setId(ID.valueOf((long) oo[0]));
                        vs.setCode((String) oo[1]);
                        vs.setFio((String) oo[2]);
                        vs.setCategory((String) oo[3]);
                        vs.setStatus((String) oo[4]);
                        vs.setFaculty((String) oo[5]);
                        vs.setSpecialty((String) oo[6]);
                        vs.setLockReason((String) oo[7]);
                        list.add(vs);
                    }
                }
            } catch (Exception ex) {
                LOG.error("Unable to load student list: ", ex);
                Message.showError(ex.toString());
            }
        }

        ((DBGridModel) studentGW.getWidgetModel()).setEntities(list);
        try {
            studentGW.refresh();
        } catch (Exception ex) {
            LOG.error("Unable to refresh student grid: ", ex);
            Message.showError(ex.toString());
        }
    }

    @Override
    public void clearFilter() {
        ((DBGridModel) studentGW.getWidgetModel()).setEntities(new ArrayList<VStudent>());
        try {
            studentGW.refresh();
        } catch (Exception ex) {
            LOG.error("Unable to refresh student grid: ", ex);
            Message.showError(ex.toString());
        }
    }

    public void refresh() throws Exception {
        FStudentFilter subjectFilter = (FStudentFilter) filterPanel.getFilterBean();
        if (subjectFilter.hasFilter()) {
            doFilter(subjectFilter);
        }
    }

    @Override
    public void beforeRefresh(Object source, int buttonId) {
    }

    @Override
    public void deferredCreate(Object source, Entity entity) {
    }

    @Override
    public void deferredDelete(Object source, List<Entity> entities) {
    }

    @Override
    public void handleEntityEvent(EntityEvent ev) {
        if (ev.getSource().equals(studentGW)) {
            if (ev.getAction() == EntityEvent.SELECTED) {
                VStudent student = (VStudent) ev.getEntities().get(0);
                PrintView pv = null;
                try {
                    pv = new PrintView(student, (FStudentFilter) filterPanel.getFilterBean());
                } catch (Exception e) {
                    e.printStackTrace();//TODO catch
                }
                new PrintDialog(pv,StudentListView.this);
            }
        }
    }

    @Override
    public void onAccept(Object source, List<Entity> entities, int buttonId) {
    }

    @Override
    public void onCreate(Object source, Entity entity, int buttonId) {
    }

    @Override
    public void onDelete(Object source, List<Entity> entities, int buttonId) {
    }

    @Override
    public boolean onEdit(Object source, Entity entity, int buttonId) {
        return false;
    }

    @Override
    public void onException(Object source, Throwable th) {
    }

    @Override
    public void onFilter(Object source, QueryModel qm, int buttonId) {
    }

    @Override
    public boolean onPreview(Object source, Entity entity, int buttonId) {
        return false;
    }

    @Override
    public void onRefresh(Object source, List<Entity> entities) {
    }

    @Override
    public boolean preCreate(Object source, int buttonId) {
        return false;
    }

    @Override
    public boolean preDelete(Object source, List<Entity> entities, int buttonId) {
        return false;
    }

    @Override
    public boolean preSave(Object source, Entity entity, boolean isNew, int buttonId) {
        return false;
    }

    private class FacultyChangeListener implements ValueChangeListener {

        private final ComboBox specialtyCB;

        public FacultyChangeListener(ComboBox specialtyCB) {
            this.specialtyCB = specialtyCB;
        }

        @Override
        public void valueChange(ValueChangeEvent ev) {
            List<SPECIALITY> list = new ArrayList<SPECIALITY>(1);
            DEPARTMENT faculty = (DEPARTMENT) ev.getProperty().getValue();
            if (faculty != null) {
                QueryModel<SPECIALITY> specialtyQM = new QueryModel<SPECIALITY>(SPECIALITY.class);
                FromItem fi = specialtyQM.addJoin(EJoin.INNER_JOIN, "department", DEPARTMENT.class, "id");
                specialtyQM.addWhere("deleted", Boolean.FALSE);
                specialtyQM.addWhereAnd(fi, "parent", ECriteria.EQUAL, faculty.getId());
                specialtyQM.addOrder("specName");
                try {
                    list = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookup(specialtyQM);
                } catch (Exception ex) {
                    LOG.error("Unable to load specialty list: ", ex);
                    Message.showError(ex.toString());
                }
            }

            BeanItemContainer<SPECIALITY> specialtyBIC = new BeanItemContainer<SPECIALITY>(SPECIALITY.class, list);
            specialtyCB.setContainerDataSource(specialtyBIC);
        }
    }
}
