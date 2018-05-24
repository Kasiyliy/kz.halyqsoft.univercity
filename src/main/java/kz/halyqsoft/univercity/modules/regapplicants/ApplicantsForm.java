package kz.halyqsoft.univercity.modules.regapplicants;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import kz.halyqsoft.univercity.entity.beans.USERS;
import kz.halyqsoft.univercity.entity.beans.univercity.*;
import kz.halyqsoft.univercity.entity.beans.univercity.catalog.*;
import kz.halyqsoft.univercity.entity.beans.univercity.enumeration.Flag;
import kz.halyqsoft.univercity.entity.beans.univercity.view.V_COORDINATOR;
import kz.halyqsoft.univercity.entity.beans.univercity.view.V_ENTRANT_SPECIALITY;
import kz.halyqsoft.univercity.entity.beans.univercity.view.V_KBTU_ENTRANTS;
import kz.halyqsoft.univercity.utils.CommonUtils;
import kz.halyqsoft.univercity.utils.register.*;
import org.apache.commons.io.FileUtils;
import org.r3a.common.dblink.facade.CommonEntityFacadeBean;
import org.r3a.common.dblink.facade.CommonIDFacadeBean;
import org.r3a.common.dblink.utils.SessionFacadeFactory;
import org.r3a.common.entity.Entity;
import org.r3a.common.entity.ID;
import org.r3a.common.entity.query.QueryModel;
import org.r3a.common.entity.query.from.EJoin;
import org.r3a.common.entity.query.from.FromItem;
import org.r3a.common.entity.query.select.EAggregate;
import org.r3a.common.entity.query.where.ECriteria;
import org.r3a.common.vaadin.widget.dialog.Message;
import org.r3a.common.vaadin.widget.dialog.select.ESelectType;
import org.r3a.common.vaadin.widget.dialog.select.custom.grid.CustomGridSelectDialog;
import org.r3a.common.vaadin.widget.form.FormModel;
import org.r3a.common.vaadin.widget.form.field.fk.FKFieldModel;
import org.r3a.common.vaadin.widget.table.TableWidget;
import org.r3a.common.vaadin.widget.table.model.DBTableModel;

import javax.persistence.NoResultException;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.List;

/**
 * @author Omarbek
 * @created 21.05.2016 11:25:18
 */
public final class ApplicantsForm extends UsersForm {

    private static String replaced;
    private Button untNextButton;
    private Map<String, Integer> fontMap;

    private TableWidget specTW;

    private Button untButton, grantDocButton;
    private Button motherButton, fatherButton;
    private Button contractButton, moreButton;
    private Button finishButton;
    private Button downloadContractButton;
    private Button downloadButtonRegisterButton;
    private Button specButton;

    private boolean saveSpec;

    private Parent parent;
    private Unt unt;
    private Grant grant;
    private Contract contract;

    private static final int FATHER = 1;
    private static final int MOTHER = 2;
    private static final int ADDRESS_FACT = 2;
    private static String NEXT_BUTTON_CAPTION = "next";

    ApplicantsForm(final FormModel dataFM, ENTRANCE_YEAR entranceYear) throws Exception {
        super(dataFM, entranceYear);

        fontMap = new HashMap<>();
        fontMap.put("Bold", Font.BOLD);
        fontMap.put("Normal", Font.NORMAL);
        fontMap.put("Italic", Font.ITALIC);
        fontMap.put("Underline", Font.UNDERLINE);

        dataFM.getFieldModel("academicStatus").setInEdit(false);
        dataFM.getFieldModel("academicStatus").setInView(false);

        setAdviser(dataFM, "advisor");
        setAdviser(dataFM, "coordinator");

        grant = new Grant(dataAFW, this);
        grant.create(udfQM);

        unt = new Unt(dataAFW, this);
        unt.create(udfQM);

        parent = new Parent(dataAFW, this);
        parent.create("parents.data.father", FATHER);
        parent.create("parents.data.mother", MOTHER);

        contract = new Contract(dataAFW, this);
        contract.create(udfQM, military.getMilitaryFLFM());
    }

    @Override
    protected List<Button> getButtons() {
        List<Button> buttons = new ArrayList<>();
        buttons.add(specButton);
        buttons.add(untButton);
        buttons.add(grantDocButton);
        buttons.add(motherButton);
        buttons.add(fatherButton);
        buttons.add(contractButton);
        return buttons;
    }

    private void setAdviser(FormModel dataFM, String field) {
        FKFieldModel advisorFM = (FKFieldModel) dataFM.getFieldModel(field);
        advisorFM.setSelectType(ESelectType.CUSTOM_GRID);
        advisorFM.setDialogHeight(400);
        advisorFM.setDialogWidth(600);

        try {
            TextField fioTF = new TextField();
            fioTF.setImmediate(true);
            fioTF.setWidth(400, Unit.PIXELS);

            QueryModel<DEPARTMENT> chairQM = new QueryModel<>(DEPARTMENT.class);
            chairQM.addWhereNotNull("parent");
            chairQM.addWhereAnd("deleted", Boolean.FALSE);
            chairQM.addOrder("deptName");
            BeanItemContainer<DEPARTMENT> chairBIC = new BeanItemContainer<>(DEPARTMENT.class, SessionFacadeFactory
                    .getSessionFacade(CommonEntityFacadeBean.class).lookup(chairQM));
            ComboBox chairCB = new ComboBox();
            chairCB.setContainerDataSource(chairBIC);
            chairCB.setImmediate(true);
            chairCB.setNullSelectionAllowed(true);
            chairCB.setTextInputAllowed(true);
            chairCB.setFilteringMode(FilteringMode.CONTAINS);
            chairCB.setPageLength(0);
            chairCB.setWidth(400, Unit.PIXELS);

            QueryModel<POST> postQM = new QueryModel<>(POST.class);
            postQM.addOrder("postName");
            BeanItemContainer<POST> postBIC = new BeanItemContainer<>(POST.class, SessionFacadeFactory
                    .getSessionFacade(CommonEntityFacadeBean.class).lookup(postQM));
            ComboBox postCB = new ComboBox();
            postCB.setContainerDataSource(postBIC);
            postCB.setImmediate(true);
            postCB.setNullSelectionAllowed(true);
            postCB.setTextInputAllowed(true);
            postCB.setFilteringMode(FilteringMode.STARTSWITH);
            postCB.setPageLength(0);
            postCB.setWidth(400, Unit.PIXELS);

            CustomGridSelectDialog cgsd = advisorFM.getCustomGridSelectDialog();
            cgsd.getSelectModel().setMultiSelect(false);
            cgsd.getFilterModel().addFilter("department", chairCB);
            cgsd.getFilterModel().addFilter("post", postCB);
            cgsd.getFilterModel().addFilter("fio", fioTF);
            cgsd.initFilter();
        } catch (Exception ex) {
            CommonUtils.showMessageAndWriteLog("Unable to initialize custom grid dialog", ex);
        }
    }

    @Override
    protected VerticalLayout getMessForm(FormModel dataFM, Label mess) {

        untButton.setEnabled(false);
        motherButton.setEnabled(false);
        contractButton.setEnabled(false);
        specButton.setEnabled(false);
        grantDocButton.setEnabled(false);
        fatherButton.setEnabled(false);

        downloadContractButton = new Button();
        downloadContractButton.setCaption(getUILocaleUtil().getCaption("download.contract"));

        downloadButtonRegisterButton = new Button();
        downloadButtonRegisterButton.setCaption(getUILocaleUtil().getCaption("download.contract.register"));


        STUDENT_EDUCATION studentEducation = new STUDENT_EDUCATION();
        try {
            STUDENT student = (STUDENT) dataFM.getEntity();
            studentEducation.setStudent(student);
            SPECIALITY speciality = (student).getEntrantSpecialities().iterator().next().getSpeciality();
            studentEducation.setFaculty(speciality.getDepartment().getParent());
            studentEducation.setChair(speciality.getDepartment());
            studentEducation.setSpeciality(speciality);
            studentEducation.setStudyYear(SessionFacadeFactory.getSessionFacade(
                    CommonEntityFacadeBean.class).lookup(STUDY_YEAR.class, ID.valueOf(1)));
            studentEducation.setLanguage(SessionFacadeFactory.getSessionFacade(
                    CommonEntityFacadeBean.class).lookup(LANGUAGE.class, ID.valueOf(1)));
            studentEducation.setEducationType(SessionFacadeFactory.getSessionFacade(
                    CommonEntityFacadeBean.class).lookup(STUDENT_EDUCATION_TYPE.class, ID.valueOf(1)));
            DateFormat uriDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            studentEducation.setEntryDate(uriDateFormat.parse(beginYear + "-09-01"));
            studentEducation.setStatus(SessionFacadeFactory.getSessionFacade(
                    CommonEntityFacadeBean.class).lookup(STUDENT_STATUS.class, ID.valueOf(1)));
            studentEducation.setCreated(new Date());

            SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).create(studentEducation);


            StreamResource myResource = createResourceStudent("85", student);
            FileDownloader fileDownloader = new FileDownloader(myResource);
            myResource.setMIMEType("application/pdf");
            myResource.setCacheTime(0);
            fileDownloader.extend(downloadContractButton);

            StreamResource myResourceParents = createResourceStudent("27", student);
            FileDownloader fileDownloaderParent = new FileDownloader(myResourceParents);
            myResourceParents.setMIMEType("application/pdf");
            myResourceParents.setCacheTime(0);
            fileDownloaderParent.extend(downloadButtonRegisterButton);

            StreamResource myResourceTitul = createResourceStudent("32", student);
            FileDownloader fileDownloaderTitul = new FileDownloader(myResourceTitul);
            myResourceTitul.setMIMEType("application/pdf");
            myResourceTitul.setCacheTime(0);
            fileDownloaderTitul.extend(downloadButtonRegisterButton);

            StreamResource myResourceReg = createResourceStudent("33", student);
            FileDownloader fileDownloaderReg = new FileDownloader(myResourceReg);
            myResourceReg.setMIMEType("application/pdf");
            myResourceReg.setCacheTime(0);
            fileDownloaderReg.extend(downloadButtonRegisterButton);

            if (student.isNeedDorm() == true) {
                StreamResource myResourceDorm = createResourceStudent("92", student);
                FileDownloader fileDownloaderDorm = new FileDownloader(myResourceDorm);
                myResourceDorm.setMIMEType("application/pdf");
                myResourceDorm.setCacheTime(0);
                fileDownloaderDorm.extend(downloadContractButton);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        VerticalLayout messForm = new VerticalLayout();
        messForm.addComponent(mess);

        Button againButton = new Button(getUILocaleUtil().getCaption("back.to.new.applicant.registration"));
        againButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                RegisterApplicantsView.regButton.click();
            }
        });

        messForm.addComponent(downloadButtonRegisterButton);
        messForm.addComponent(downloadContractButton);
        messForm.addComponent(againButton);
        return messForm;
    }

    @Override
    protected Map<Boolean, String> getConditionsMap() {
        Map<Boolean, String> conditionsMap = new HashMap<>();
        conditionsMap.put(!saveData, getUILocaleUtil().getMessage("info.save.base.data.first"));
        conditionsMap.put(!savePass, getUILocaleUtil().getMessage("info.save.passport"));
        conditionsMap.put(!saveFactAddress, getUILocaleUtil().getMessage("info.save.address"));
        conditionsMap.put(!saveSpec, getUILocaleUtil().getMessage("info.save.speciality"));
        conditionsMap.put(!saveEduc, getUILocaleUtil().getMessage("info.save.educ"));
        return conditionsMap;
    }

    @Override
    protected boolean checkForMoreButton(FormModel dataFM) {
        return (flagSave(flag, dataFM) && (Flag.MAIN_DATA.equals(flag) || Flag.CONTRACT.equals(flag)))
                || !(Flag.MAIN_DATA.equals(flag) || Flag.CONTRACT.equals(flag));
    }

    @Override
    protected Button getAfterMedButton() {
        return specButton;
    }


    @Override
    protected void initOwnButtons(FormModel dataFM) {
        specButton = createFormButton("speciality", true);
        specButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                specTW = new TableWidget(V_ENTRANT_SPECIALITY.class);
                specTW.addEntityListener(ApplicantsForm.this);
                specTW.setWidth("667px");
                DBTableModel entrantSpecialityTM = (DBTableModel) specTW.getWidgetModel();
                entrantSpecialityTM.setPageLength(5);
                QueryModel entrantSpecialityQM = ((DBTableModel) specTW.getWidgetModel()).getQueryModel();
                ID studentId1 = ID.valueOf(-1);
                if (!dataFM.isCreateNew()) {
                    try {
                        studentId1 = dataFM.getEntity().getId();
                        if (dataAFW.save()) {
                            saveData = true;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();//TODO catch
                    }
                }
                entrantSpecialityQM.addWhere("student", ECriteria.EQUAL, studentId1);

                if ((flagSave(flag, dataFM) && Flag.MAIN_DATA.equals(flag))
                        || !Flag.MAIN_DATA.equals(flag)) {
                    flag = Flag.SPECIALITY;
                    registrationHSP.removeComponent(contentHL);
                    addToLayout(specTW, untButton, event);
                    registrationHSP.addComponent(contentHL);
                }
            }
        });

        untButton = createFormButton("unt", true);
        untButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if ((flagSave(flag, dataFM) && Flag.MAIN_DATA.equals(flag))
                        || !Flag.MAIN_DATA.equals(flag)) {
                    setActive(event);
                    flag = Flag.UNT;

                    Button savedUntButton = unt.createRates(createSaveButton());

                    VerticalLayout dataUNT = new VerticalLayout();
                    dataUNT.addComponent(unt.getCertificateGFW());
                    dataUNT.addComponent(savedUntButton);
                    dataUNT.setComponentAlignment(savedUntButton, Alignment.MIDDLE_CENTER);
                    dataUNT.addComponent(unt.getUntRatesTW());

                    contentHL.removeAllComponents();
                    contentHL.addComponent(dataUNT);

                    untNextButton = new Button();
                    untNextButton.setIcon(new ThemeResource("img/button/arrow_right.png"));
                    untNextButton.setCaption(getUILocaleUtil().getCaption(NEXT_BUTTON_CAPTION));
                    untNextButton.setEnabled(false);
                    untNextButton.addClickListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            grantDocButton.click();
                        }
                    });
                    contentHL.addComponent(untNextButton);
                    contentHL.setComponentAlignment(untNextButton, Alignment.MIDDLE_CENTER);
                }
            }
        });

        grantDocButton = createFormButton("grant.document", false);
        grantDocButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if ((flagSave(flag, dataFM) && Flag.MAIN_DATA.equals(flag))
                        || !Flag.MAIN_DATA.equals(flag)) {
                    addToLayout(Flag.GRANT_DOC, grant.getMainGFW(), motherButton, event);
                }
            }
        });

        motherButton = createFormButton("parents.data.mother", false);
        motherButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if ((flagSave(flag, dataFM) && (Flag.MAIN_DATA.equals(flag) || Flag.GRANT_DOC.equals(flag)))
                        || !(Flag.MAIN_DATA.equals(flag) || Flag.GRANT_DOC.equals(flag))) {
                    addToLayout(Flag.MOTHER, parent.getMotherGFW(), fatherButton, event);
                }
            }
        });

        fatherButton = createFormButton("parents.data.father", false);
        fatherButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if ((flagSave(flag, dataFM) && (Flag.MAIN_DATA.equals(flag) || Flag.MOTHER.equals(flag)))
                        || !(Flag.MAIN_DATA.equals(flag) || Flag.MOTHER.equals(flag))) {
                    addToLayout(Flag.FATHER, parent.getFatherGFW(), contractButton, event);
                }
            }
        });

        contractButton = createFormButton("contract.data", false);
        contractButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if ((flagSave(flag, dataFM) && (Flag.MAIN_DATA.equals(flag) || Flag.FATHER.equals(flag)))
                        || !(Flag.MAIN_DATA.equals(flag) || Flag.FATHER.equals(flag))) {
                    addToLayout(Flag.CONTRACT, contract.getMainGFW(), moreButton, event);
                }
            }
        });

    }


    @Override
    protected void initSpec(FormModel dataFM) {
        saveSpec = false;
        saveEduc = false;
        saveFactAddress = false;
        specTW = new TableWidget(V_ENTRANT_SPECIALITY.class);
        specTW.addEntityListener(ApplicantsForm.this);
        specTW.setWidth("667px");
        DBTableModel entrantSpecialityTM = (DBTableModel) specTW.getWidgetModel();
        entrantSpecialityTM.setPageLength(5);
        QueryModel entrantSpecialityQM = ((DBTableModel) specTW.getWidgetModel()).getQueryModel();
        ID studentId1 = ID.valueOf(-1);

        if (!dataFM.isCreateNew()) {
            try {
                studentId1 = dataFM.getEntity().getId();
            } catch (Exception ex) {
                ex.printStackTrace();//TODO catch
            }
        }
        entrantSpecialityQM.addWhere("student", ECriteria.EQUAL, studentId1);
    }

    @Override
    protected boolean checkFlag(Flag flag) {
        Boolean saved;
        switch (flag) {
            case SPECIALITY:
                if (specTW.getEntityCount() > 0) {
                    saveSpec = true;
                }
                break;
            case GRANT_DOC:
                saved = grant.save();
                if (saved == null || saved) {
                    return true;
                }
                break;
            case MOTHER:
                saved = parent.save(MOTHER);
                if (saved == null || saved) {
                    return true;
                }
                break;
            case FATHER:
                saved = parent.save(FATHER);
                if (saved == null || saved) {
                    return true;
                }
                break;
            case CONTRACT:
                saved = contract.save();
                if (saved == null || saved) {
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    protected String getLogin(String s) throws Exception {
        return null;
    }

    public static StreamResource createResourceStudent(String value, STUDENT student) {
        String fileName = "";
        if (value.equals("92")) {
            fileName = "Договор общага.pdf";
        } else if (value.equals("85")) {
            fileName = "Договор на рус.pdf";
        } else if (value.equals("32")) {
            fileName = "Титул.pdf";
        } else if (value.equals("33")) {
            fileName = "Қолхат.pdf";
        } else {
            fileName = "Өтініш.pdf";
        }
        return new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                Document docum = new Document();
                Paragraph title = new Paragraph();
                QueryModel<PDF_PROPERTY> propertyQM = new QueryModel<>(PDF_PROPERTY.class);
                FromItem doc = propertyQM.addJoin(EJoin.INNER_JOIN, "pdfDocument", PDF_DOCUMENT.class, "id");

                propertyQM.addWhere(doc, "id", ECriteria.EQUAL, value);
                propertyQM.addOrder("orderNumber");
                List<PDF_PROPERTY> properties = null;
                try {
                    properties = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookup(propertyQM);
                    ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream();
                    PdfWriter pdfWriter = PdfWriter.getInstance(docum, byteArrayOutputStream1);
                    docum.open();
                    if (value.equals("32")) {
                        PdfContentByte canvas = pdfWriter.getDirectContent();
                        Rectangle rect = new Rectangle(36, 26, 559, 816);
                        Rectangle rect1 = new Rectangle(86, 790, 187, 685);
                        rect1.setBorderWidth(1);
                        rect1.setBorder(Rectangle.BOX);
                        rect.setBorder(Rectangle.BOX);
                        rect.setBorderWidth(1);
                        canvas.rectangle(rect1);
                        canvas.rectangle(rect);
                        title = new Paragraph("ОҢТҮСТІК ҚАЗАҚСТАН",
                                getFont(12, Font.BOLD));
                        title.setAlignment(Element.ALIGN_CENTER);
                        title.setSpacingBefore(10f);
                    } else if (value.equals("33")) {
                        Rectangle one = new Rectangle(70, 140);
                        Rectangle two = new Rectangle(1000, 800);
                        docum.setPageSize(one);
                        docum.setMargins(2, 2, 2, 2);
                        docum.open();
                        docum.setPageSize(two);
                        docum.setMargins(20, 20, 20, 20);
                        docum.newPage();
                        PdfContentByte canvas = pdfWriter.getDirectContent();
                        canvas.moveTo(12, 554);
                        canvas.lineTo(700, 554);
                        canvas.lineTo(700, 344);
                        canvas.lineTo(12, 344);
                        canvas.moveTo(12, 524);
                        canvas.lineTo(700, 524);
                        canvas.moveTo(12, 506);
                        canvas.lineTo(700, 506);
                        canvas.moveTo(12, 488);
                        canvas.lineTo(700, 488);
                        canvas.moveTo(12, 470);
                        canvas.lineTo(700, 470);
                        canvas.moveTo(12, 452);
                        canvas.lineTo(700, 452);
                        canvas.moveTo(12, 416);
                        canvas.lineTo(700, 416);
                        canvas.moveTo(12, 380);
                        canvas.lineTo(700, 380);
                        canvas.moveTo(12, 362);
                        canvas.lineTo(700, 362);
                        canvas.moveTo(12, 344);
                        canvas.lineTo(12, 344);
                        canvas.moveTo(12, 554);
                        canvas.lineTo(12, 344);
                        canvas.moveTo(250, 554);
                        canvas.lineTo(250, 344);
                        canvas.moveTo(570, 554);
                        canvas.lineTo(570, 344);

                        canvas.moveTo(12, 334);
                        canvas.lineTo(640, 334);
                        canvas.lineTo(640, 309);
                        canvas.lineTo(12, 309);

                        canvas.moveTo(160, 334);
                        canvas.lineTo(160, 309);
                        canvas.moveTo(410, 334);
                        canvas.lineTo(410, 309);
                        canvas.moveTo(560, 334);
                        canvas.lineTo(560, 309);
                        canvas.moveTo(12, 334);
                        canvas.lineTo(12, 309);
                        canvas.closePathStroke();
                        title = new Paragraph("ОҢТҮСТІК ҚАЗАҚСТАН ПЕДАГОГИКАЛЫҚ УНИВЕРСИТЕТІ",
                                getFont(12, Font.BOLD));
                        title.setSpacingBefore(0f);
                        title.setIndentationLeft(150f);
                    } else if (value.equals("27")) {
                        title = new Paragraph("ОҢТҮСТІК ҚАЗАҚСТАН ПЕДАГОГИКАЛЫҚ УНИВЕРСИТЕТІ",
                                getFont(12, Font.BOLD));
                        title.setAlignment(Element.ALIGN_CENTER);
                    } else {
                        title = new Paragraph("Договор",
                                getFont(12, Font.BOLD));
                        title.setAlignment(Element.ALIGN_CENTER);
                    }
                    docum.add(title);

                    for (PDF_PROPERTY property : properties) {

                        String text = new String(property.getText());
                        setReplaced(text, student);
                        Paragraph paragraph = new Paragraph(replaced,
                                getFont(Integer.parseInt(property.getSize().toString()), CommonUtils.getFontMap(property.getFont().toString())));

                        if (property.isCenter() == true) {
                            paragraph.setAlignment(Element.ALIGN_CENTER);
                        }
                        paragraph.setSpacingBefore(property.getY());
                        paragraph.setIndentationLeft(property.getX());


                        docum.add(paragraph);
                    }

                    pdfWriter.close();
                    docum.close();
                    return new ByteArrayInputStream(byteArrayOutputStream1.toByteArray());

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }, fileName);
    }

    private static void setReplaced(String text, STUDENT student) throws Exception {
        Date date = Calendar.getInstance().getTime();

        STUDENT_EDUCATION studentEducation = new STUDENT_EDUCATION();
        studentEducation.setStudent(student);

        SPECIALITY speciality = (student).getEntrantSpecialities().iterator().next().getSpeciality();
        studentEducation.setFaculty(speciality.getDepartment().getParent());

        STUDENT_RELATIVE studentRelativeMother = getStudent_relative(student, MOTHER);
        STUDENT_RELATIVE studentRelativeFather = getStudent_relative(student, FATHER);

        USER_ADDRESS userAddress;
        QueryModel<USER_ADDRESS> userAddressQueryModel = new QueryModel<>(USER_ADDRESS.class);
        userAddressQueryModel.addWhere("user", ECriteria.EQUAL, student.getId());
        userAddressQueryModel.addWhereAnd("addressType", ECriteria.EQUAL, ID.valueOf(ADDRESS_FACT));
        userAddress = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupSingle(userAddressQueryModel);

        QueryModel<EDUCATION_DOC> educationDocQueryModel = new QueryModel<>(EDUCATION_DOC.class);

        EDUCATION_DOC educationDoc;
        FromItem sc = educationDocQueryModel.addJoin(EJoin.INNER_JOIN, "id", USER_DOCUMENT.class, "id");
        educationDocQueryModel.addWhere(sc, "user", ECriteria.EQUAL, student.getId());
        educationDoc = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupSingle(educationDocQueryModel);

        UNT_CERTIFICATE untCertificate;
        QueryModel<UNT_CERTIFICATE> untCertificateQueryModel = new QueryModel<>(UNT_CERTIFICATE.class);
        FromItem unt = untCertificateQueryModel.addJoin(EJoin.INNER_JOIN, "id", USER_DOCUMENT.class, "id");
        untCertificateQueryModel.addWhere(unt, "user", ECriteria.EQUAL, student.getId());
        untCertificate = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class)
                .lookupSingle(untCertificateQueryModel);


        ACCOUNTANT_PRICE accountantPrice;
        QueryModel<ACCOUNTANT_PRICE> accountantPriceQueryModel = new QueryModel<>(ACCOUNTANT_PRICE.class);
        accountantPriceQueryModel.addWhere("diplomaType", ECriteria.EQUAL, student.getDiplomaType().getId());
        accountantPriceQueryModel.addWhere("level", ECriteria.EQUAL, student.getLevel().getId());
        accountantPriceQueryModel.addWhere("contractPaymentType", ECriteria.EQUAL, ID.valueOf(2));
        accountantPriceQueryModel.addWhere("deleted", ECriteria.EQUAL, false);
        accountantPrice = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupSingle(accountantPriceQueryModel);


        String money = String.valueOf(accountantPrice.getPrice());
        String inLetters = accountantPrice.getPriceInLetters();
        if (student.isNeedDorm()) {
            accountantPriceQueryModel.addWhere("contractPaymentType", ECriteria.EQUAL, ID.valueOf(1));
        }
        accountantPrice = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupSingle(accountantPriceQueryModel);

        String ochnii = student.getDiplomaType().toString();
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        String today = formatter.format(date);
        if (student.getDiplomaType().toString().equals("Очный")) {
            ochnii = "очной";
        } else if (student.getDiplomaType().toString().equals("Заочный")) {
            ochnii = "заочной";
        }

        DateFormat form = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");

        Date dateBirth = (Date) form.parse(student.getBirthDate().toString());
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateBirth);
        String birthdayDate = cal.get(Calendar.DATE) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR);
        Date date1 = formatter.parse(birthdayDate);
        String birthday = formatter.format(date1);

        Date dateDocument = (Date) form.parse(educationDoc.getIssueDate().toString());
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(dateDocument);
        String formatDate = cal1.get(Calendar.DATE) + "." + (cal1.get(Calendar.MONTH) + 1) + "." + cal1.get(Calendar.YEAR);
        Date date2 = formatter.parse(formatDate);
        String attestationDate = formatter.format(date2);

        Date created = (Date) form.parse(student.getCreated().toString());
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(created);
        String format = cal2.get(Calendar.DATE) + "." + (cal2.get(Calendar.MONTH) + 1) + "." + cal2.get(Calendar.YEAR);
        Date date3 = formatter.parse(format);
        String createdDate = formatter.format(date3);

        String dorm = "қажет емес";
        if (student.isNeedDorm()) {
            dorm = "қажет";
        }

        if (student.getMiddleName() == null) {
            student.setMiddleName("");
        }

        V_COORDINATOR coordinator = new V_COORDINATOR();
        coordinator.setFio("-");
        if (student.getCoordinator() == null) {
            student.setCoordinator(coordinator);
        }
        if (educationDoc.getEndYear() == null) {
            educationDoc.setEndYear(Calendar.getInstance().get(Calendar.YEAR));
        }
        if (studentRelativeFather.getPhoneMobile() == null) {
            studentRelativeFather.setPhoneMobile("***");
        }
        if (studentRelativeMother.getPhoneMobile() == null) {
            studentRelativeMother.setPhoneMobile("***");
        }

        if (studentRelativeMother.getWorkPlace() == null) {
            studentRelativeMother.setWorkPlace("-");
        }
        if (studentRelativeFather.getWorkPlace() == null) {
            studentRelativeFather.setWorkPlace("-");
        }
        if (studentRelativeFather.getPostName() == null) {
            studentRelativeFather.setPostName("-");
        }
        if (studentRelativeMother.getPostName() == null) {
            studentRelativeMother.setPostName("-");
        }


        replaced = text.replaceAll("\\$fio", student.toString())
                .replaceAll("\\$money", money)
                .replaceAll("\\$faculty", studentEducation.getFaculty().toString())
                .replaceAll("\\$DataMonthYear", today + " года")
                .replaceAll("\\$formaobuch", ochnii)
                .replaceAll("\\$data\\$month\\$year", today)
                .replaceAll("\\$email", userAddress.getPostalCode())
                .replaceAll("\\$rekvizit", userAddress.getStreet())
                .replaceAll("\\$phone", "+7" + student.getPhoneMobile())
                .replaceAll("\\$InLetters", inLetters)
                .replaceAll("\\$Obshaga", String.valueOf(accountantPrice.getPrice()))
                .replaceAll("\\$Dorm", accountantPrice.getPriceInLetters())
                .replaceAll("\\$aboutMe", "My name is " + student.toString())
                .replaceAll("\\$country", student.getCitizenship().toString())
                .replaceAll("\\$status", student.getMaritalStatus().toString())
                .replaceAll("\\$father", studentRelativeFather.getFio())
                .replaceAll("\\$mother", studentRelativeMother.getFio())
                .replaceAll("\\$numFather", "+7 " + studentRelativeFather.getPhoneMobile())
                .replaceAll("\\$numMother", "+7 " + studentRelativeMother.getPhoneMobile())
                .replaceAll("\\$gender", student.getSex().toString())
                .replaceAll("\\$birthYear", birthday)
                .replaceAll("\\$nationality", student.getNationality().toString())
                .replaceAll("\\$info", educationDoc.getEndYear().toString() + ", "
                        + educationDoc.getEducationType() + ", " + educationDoc.getSchoolName())
                .replaceAll("\\$speciality", speciality.getSpecName())
                .replaceAll("\\$parentsAddress", studentRelativeFather.getFio() + ", "
                        + studentRelativeFather.getWorkPlace() + "    "
                        + studentRelativeFather.getPostName() + '\n'
                        + studentRelativeMother.getFio() + ", "
                        + studentRelativeMother.getWorkPlace() + "    "
                        + studentRelativeMother.getPostName())
                .replaceAll("\\$trudovoe", "Ничем не занимался")
                .replaceAll("\\$name", student.getLastName())
                .replaceAll("\\$surname", student.getFirstName())
                .replaceAll("\\$firstName", student.getMiddleName())
                .replaceAll("\\$education", educationDoc.getEducationType().toString())
                .replaceAll("\\$technic", student.getCoordinator().toString())
                .replaceAll("\\$attestat", attestationDate)
                .replaceAll("\\$nomer", educationDoc.getDocumentNo())
                .replaceAll("\\$ent", untCertificate.getDocumentNo())
                .replaceAll("\\$document", createdDate)
                .replaceAll("\\$diplomaType", student.getDiplomaType().toString())
                .replaceAll("қажет, қажет емес", dorm);
    }

    public static STUDENT_RELATIVE getStudent_relative(STUDENT student, int relativeType) throws Exception {
        STUDENT_RELATIVE studentRelative;
        try {
            QueryModel<STUDENT_RELATIVE> relative = new QueryModel<>(STUDENT_RELATIVE.class);
            relative.addWhere("student", ECriteria.EQUAL, student.getId());
            relative.addWhere("relativeType", ECriteria.EQUAL, ID.valueOf(relativeType));
            studentRelative = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class)
                    .lookupSingle(relative);
        } catch (NoResultException ex) {
            studentRelative = new STUDENT_RELATIVE();
            studentRelative.setFio("-");
        }
        return studentRelative;
    }

    public static Font getFont(int fontSize, int font) {
        String fontPath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/WEB-INF/classes/fonts";
        BaseFont timesNewRoman = null;
        try {
            timesNewRoman = BaseFont.createFont(fontPath + "/TimesNewRoman/times.ttf", BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED);
        } catch (Exception e) {
            e.printStackTrace();//TODO catch
        }
        return new Font(timesNewRoman, fontSize, font);
    }

    private boolean preSaveSpeciality(Entity e, boolean isNew) {
        V_ENTRANT_SPECIALITY ves = (V_ENTRANT_SPECIALITY) e;
        ENTRANT_SPECIALITY es;
        FormModel fm = dataAFW.getWidgetModel();
        if (isNew) {

            es = new ENTRANT_SPECIALITY();
            try {
                STUDENT s = (STUDENT) fm.getEntity();
                es.setStudent(s);
                es.setUniversity(ves.getUniversity());
                es.setSpeciality(SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookup(SPECIALITY.class, ves.getSpeciality().getId()));
                es.setLanguage(ves.getLanguage());
                SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).create(es);

                QueryModel entrantSpecialityQM = ((DBTableModel) specTW.getWidgetModel()).getQueryModel();
                entrantSpecialityQM.addWhere("student", ECriteria.EQUAL, s.getId());

                specTW.refresh();

                if (es.getUniversity().getId().equals(ID.valueOf(421))) {
                    if ("000000000000".equals(s.getCode())) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(s.getCreated());
                        Integer year = calendar.get(Calendar.YEAR);

                        LEVEL level = s.getLevel();

                        DEPARTMENT faculty = es.getSpeciality().getDepartment().getParent();

                        QueryModel<V_KBTU_ENTRANTS> qm = new QueryModel<>(V_KBTU_ENTRANTS.class);
                        qm.addSelect("id", EAggregate.COUNT);
                        qm.addWhere("level", ECriteria.EQUAL, level.getId());
                        qm.addWhereAnd("createdYear", ECriteria.EQUAL, year);
                        qm.addWhereAnd("faculty", ECriteria.EQUAL, faculty.getId());

                        try {
                            Integer count = (Integer) SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItems(qm);
                            String code = CommonUtils.getCode(count);

                            code = faculty.getCode() + code;
                            if (level.getId().equals(ID.valueOf(2))) {
                                code = "MD" + code;
                            } else {
                                code = "BD" + code;
                            }
                            code = year.toString().substring(2) + code;
                            s.setCode(code);
                            SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).merge(s);
                            dataAFW.refresh();
                        } catch (Exception ex) {
                            CommonUtils.showMessageAndWriteLog("Unable to generate code for entrant", ex);
                        }
                    }
                }

                showSavedNotification();
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to create an entrant speciality", ex);
            }
        } else {
            try {
                es = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookup(ENTRANT_SPECIALITY.class, ves.getId());
                es.setUniversity(ves.getUniversity());
                es.setSpeciality(SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookup(SPECIALITY.class, ves.getSpeciality().getId()));
                es.setLanguage(ves.getLanguage());
                SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).merge(es);
                specTW.refresh();
                showSavedNotification();
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to merge an entrant speciality", ex);
            }
        }

        return false;
    }

    private boolean addFiles(Object source) {
        QueryModel<USER_DOCUMENT_FILE> udfQM = new QueryModel<>(USER_DOCUMENT_FILE.class);
        udfQM.addSelect("id");
        udfQM.addSelect("fileName");
        udfQM.addWhere("deleted", Boolean.FALSE);

        if (source.equals(unt.getUntRatesTW())) {
            return true;
        } else if (source.equals(specTW)) {
            FormModel entrantSpecialityFM = ((DBTableModel) specTW.getWidgetModel()).getFormModel();
            FKFieldModel specialityFKFM = (FKFieldModel) entrantSpecialityFM.getFieldModel("speciality");
            specialityFKFM.setDialogWidth(600);
            specialityFKFM.setDialogHeight(600);
            QueryModel specialityQM = specialityFKFM.getQueryModel();
            specialityQM.addWhere("deleted", Boolean.FALSE);
            return true;
        }
        return false;
    }

    @Override
    public boolean preCreate(Object source, int buttonId) {
        if (source.equals(unt.getUntRatesTW())) {
            try {
                unt.getCertificateGFW().post();
            } catch (Exception e) {
                CommonUtils.showMessageAndWriteLog("Unable to pre create unt rates", e);
            }

            if (unt.getCertificateGFW().getWidgetModel().isCreateNew()) {
                Message.showInfo(getUILocaleUtil().getMessage("info.save.base.data.first"));
                return false;
            }

            return true;
        }
        if (source.equals(specTW)) {
            saveData = true;
            if (dataAFW.getWidgetModel().isCreateNew() || dataAFW.getWidgetModel().isModified()) {
                boolean success = dataAFW.save();
                if (!success) {
                    return false;
                }
            }

            int count = specTW.getEntityCount();
            if (count == 4) {
                Message.showInfo(getUILocaleUtil().getMessage("more.records.not.required"));
                return false;
            }

            FormModel entrantSpecialityFM = ((DBTableModel) specTW.getWidgetModel()).getFormModel();
            FKFieldModel specialityFKFM = (FKFieldModel) entrantSpecialityFM.getFieldModel("speciality");
            specialityFKFM.setDialogWidth(600);
            specialityFKFM.setDialogHeight(600);
            QueryModel specialityQM = specialityFKFM.getQueryModel();
            specialityQM.addWhere("deleted", Boolean.FALSE);
            try {
                specialityQM.addWhere("level", ECriteria.EQUAL, ((STUDENT) dataAFW.getWidgetModel().getEntity()).getLevel().getId());
            } catch (Exception ex) {
                ex.printStackTrace();//TODO catch
            }

            return true;
        }

        return super.preCreate(source, buttonId);
    }

    @Override
    public boolean onEdit(Object source, Entity e, int buttonId) {
        return addFiles(source) || super.onEdit(source, e, buttonId);
    }

    @Override
    public boolean onPreview(Object source, Entity e, int buttonId) {
        return addFiles(source) || super.onPreview(source, e, buttonId);
    }

    @Override
    public boolean preSave(Object source, Entity e, boolean isNew, int buttonId) throws Exception {
        if (source.equals(specTW)) {
            return preSaveSpeciality(e, isNew);
        } else if (source.equals(grant.getMainGFW())) {
            return grant.preSave(e, isNew);
        } else if (source.equals(unt.getCertificateGFW())) {
            return unt.preSaveCertificate(e, isNew);
        } else if (source.equals(unt.getUntRatesTW())) {
            boolean preSaveRates = unt.preSaveRates(e, isNew);
            if (unt.getUntRatesTW().getEntityCount() > 1) {
                untNextButton.setEnabled(true);
            }
            return preSaveRates;
        } else if (source.equals(parent.getFatherGFW())) {
            return parent.preSave(e, isNew, FATHER);
        } else if (source.equals(parent.getMotherGFW())) {
            return parent.preSave(e, isNew, MOTHER);
        } else if (source.equals(contract.getMainGFW())) {
            return contract.preSave(e, isNew);
        }
        return super.preSave(source, e, isNew, buttonId);
    }

    @Override
    public boolean preDelete(Object source, List<Entity> entities, int buttonId) {
        if (source.equals(unt.getUntRatesTW())) {
            List<UNT_CERT_SUBJECT> delList = new ArrayList<>();
            for (Entity e : entities) {
                try {
                    delList.add(SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookup(UNT_CERT_SUBJECT.class, e.getId()));
                } catch (Exception ex) {
                    CommonUtils.showMessageAndWriteLog("Unable to delete user Unt rates", ex);
                }
            }

            try {
                SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).delete(delList);
                unt.getUntRatesTW().refresh();
            } catch (Exception ex) {
                LOG.error("Unable to delete user Unt rates", ex);
                Message.showError(getUILocaleUtil().getMessage("error.cannotdelentity"));
            }

            return false;
        } else if (source.equals(specTW)) {
            List<ENTRANT_SPECIALITY> delList = new ArrayList<>();
            for (Entity e : entities) {
                try {
                    delList.add(SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookup(ENTRANT_SPECIALITY.class, e.getId()));
                } catch (Exception ex) {
                    CommonUtils.showMessageAndWriteLog("Unable to delete entrant specialities", ex);
                }
            }

            try {
                SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).delete(delList);
                specTW.refresh();
            } catch (Exception ex) {
                LOG.error("Unable to delete entrant specialities: ", ex);
                Message.showError(getUILocaleUtil().getMessage("error.cannotdelentity"));
            }

            return false;
        }

        return super.preDelete(source, entities, buttonId);
    }

    @Override
    public void deferredCreate(Object source, Entity e) {
        STUDENT student = (STUDENT) e;
        if (source.equals(dataAFW)) {
            student.setCreated(new Date());
            try {
                student.setId(SessionFacadeFactory.getSessionFacade(CommonIDFacadeBean.class).getID("S_USER"));
                student.setCategory(SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookup(STUDENT_CATEGORY.class, ID.valueOf(1)));
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to create a social category", ex);
            }
            student.setCode("000000000000");//TODO
            student.setFirstName(student.getFirstName());
            student.setLastName(student.getLastName());
            student.setFirstNameEN(student.getFirstNameEN());
            student.setLastNameEN(student.getLastNameEN());
        }
    }

    @Override
    public String getViewName() {
        return "ApplicantsForm";
    }
}
