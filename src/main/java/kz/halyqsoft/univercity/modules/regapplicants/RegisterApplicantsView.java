package kz.halyqsoft.univercity.modules.regapplicants;

import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import kz.halyqsoft.univercity.entity.beans.univercity.STUDENT;
import kz.halyqsoft.univercity.entity.beans.univercity.catalog.ENTRANCE_YEAR;
import kz.halyqsoft.univercity.entity.beans.univercity.catalog.LEVEL;
import kz.halyqsoft.univercity.entity.beans.univercity.catalog.STUDENT_CATEGORY;
import kz.halyqsoft.univercity.entity.beans.univercity.catalog.STUDENT_DIPLOMA_TYPE;
import kz.halyqsoft.univercity.utils.CommonUtils;
import org.r3a.common.dblink.facade.CommonEntityFacadeBean;
import org.r3a.common.dblink.utils.SessionFacadeFactory;
import org.r3a.common.entity.ID;
import org.r3a.common.entity.beans.AbstractTask;
import org.r3a.common.entity.query.QueryModel;
import org.r3a.common.entity.query.where.ECriteria;
import org.r3a.common.vaadin.view.AbstractTaskView;
import org.r3a.common.vaadin.widget.form.FormModel;
import org.r3a.common.vaadin.widget.form.field.fk.FKFieldModel;

import java.util.Calendar;

/**
 * @author Omarbek
 * @created on 13.03.2018
 */
public class RegisterApplicantsView extends AbstractTaskView {

    static Button regButton;
    private VerticalLayout registerVL;
    private boolean removeAll = false;

    public RegisterApplicantsView(AbstractTask task) throws Exception {
        super(task);
    }

    @Override
    public void initView(boolean b) throws Exception {
        registerVL = new VerticalLayout();
        regButton = new Button();
        regButton.setCaption(getUILocaleUtil().getCaption("sign.up"));
        regButton.setWidth(150.0F, Sizeable.Unit.PIXELS);
        regButton.setIcon(new ThemeResource("img/button/add.png"));
        regButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                createApplicants();
                removeAll = true;
            }
        });
        registerVL.addComponent(regButton);
        registerVL.setComponentAlignment(regButton, Alignment.MIDDLE_CENTER);
        getContent().addComponent(registerVL);
    }

    private void createApplicants() {
        if (removeAll) {
            registerVL.removeAllComponents();
        }
        FormModel studentFM = new FormModel(STUDENT.class, true);
        studentFM.setReadOnly(false);
        studentFM.setTitleVisible(false);
        QueryModel cityQM = ((FKFieldModel) studentFM.getFieldModel("citizenship")).getQueryModel();
        cityQM.addWhereNull("parent");
        cityQM.addOrder("countryName");
        QueryModel nationalityQM = ((FKFieldModel) studentFM.getFieldModel("nationality")).getQueryModel();
        nationalityQM.addOrder("nationName");

        studentFM.getFieldModel("firstName").setWidth(300);
        studentFM.getFieldModel("lastName").setWidth(300);
        studentFM.getFieldModel("middleName").setWidth(300);
        studentFM.getFieldModel("firstNameEN").setWidth(300);
        studentFM.getFieldModel("lastNameEN").setWidth(300);
        studentFM.getFieldModel("middleNameEN").setWidth(300);
        studentFM.getFieldModel("birthDate").setWidth(300);
        studentFM.getFieldModel("sex").setWidth(300);
        studentFM.getFieldModel("maritalStatus").setWidth(300);
        studentFM.getFieldModel("nationality").setWidth(300);
        studentFM.getFieldModel("citizenship").setWidth(300);
        studentFM.getFieldModel("phoneMobile").setWidth(300);
        studentFM.getFieldModel("level").setWidth(300);
        studentFM.getFieldModel("category").setWidth(300);
        studentFM.getFieldModel("diplomaType").setWidth(300);
        try {
            studentFM.createNew();

            Calendar c = Calendar.getInstance();
            QueryModel<ENTRANCE_YEAR> qmEntranceYear = new QueryModel<>(ENTRANCE_YEAR.class);
            qmEntranceYear.addWhere("beginYear", ECriteria.EQUAL, c.get(Calendar.YEAR));
            ENTRANCE_YEAR ey = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupSingle(qmEntranceYear);

            ((STUDENT) studentFM.getEntity()).setEntranceYear(ey);
            ((STUDENT) studentFM.getEntity()).setLevel(SessionFacadeFactory.getSessionFacade(
                    CommonEntityFacadeBean.class).lookup(LEVEL.class, ID.valueOf(1)));
            ((STUDENT) studentFM.getEntity()).setCategory(SessionFacadeFactory.getSessionFacade(
                    CommonEntityFacadeBean.class).lookup(STUDENT_CATEGORY.class, ID.valueOf(1)));
            ((STUDENT) studentFM.getEntity()).setDiplomaType(SessionFacadeFactory.getSessionFacade(
                    CommonEntityFacadeBean.class).lookup(STUDENT_DIPLOMA_TYPE.class, ID.valueOf(1)));

//            String qewe = "qewe";
//            ((STUDENT) studentFM.getEntity()).setFirstName(qewe);
//            ((STUDENT) studentFM.getEntity()).setFirstNameEN(qewe);
//            ((STUDENT) studentFM.getEntity()).setLastName(qewe);
//            ((STUDENT) studentFM.getEntity()).setLastNameEN(qewe);
//            ((STUDENT) studentFM.getEntity()).setBirthDate(new Date());
//            ((STUDENT) studentFM.getEntity()).setSex(SessionFacadeFactory.getSessionFacade(
//                    CommonEntityFacadeBean.class).lookup(SEX.class, ID.valueOf(1)));
//            ((STUDENT) studentFM.getEntity()).setMaritalStatus(SessionFacadeFactory.getSessionFacade(
//                    CommonEntityFacadeBean.class).lookup(MARITAL_STATUS.class, ID.valueOf(1)));
//            ((STUDENT) studentFM.getEntity()).setNationality(SessionFacadeFactory.getSessionFacade(
//                    CommonEntityFacadeBean.class).lookup(NATIONALITY.class, ID.valueOf(1)));
//            ((STUDENT) studentFM.getEntity()).setCitizenship(SessionFacadeFactory.getSessionFacade(
//                    CommonEntityFacadeBean.class).lookup(COUNTRY.class, ID.valueOf(1)));
//            ((STUDENT) studentFM.getEntity()).setEmail(qewe + "@mail.ru");
//            ((STUDENT) studentFM.getEntity()).setPhoneMobile("707");


            registerVL.addComponent(new ApplicantsForm(studentFM, ey));

        } catch (Exception ex) {
            CommonUtils.showMessageAndWriteLog("Unable to create new applicant", ex);
        }
    }
}
