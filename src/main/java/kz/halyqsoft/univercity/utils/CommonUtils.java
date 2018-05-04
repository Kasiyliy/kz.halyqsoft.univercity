package kz.halyqsoft.univercity.utils;

import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import kz.halyqsoft.univercity.entity.beans.USERS;
import kz.halyqsoft.univercity.entity.beans.univercity.EMPLOYEE;
import kz.halyqsoft.univercity.entity.beans.univercity.STUDENT;
import kz.halyqsoft.univercity.entity.beans.univercity.USER_DOCUMENT_FILE;
import org.r3a.common.dblink.facade.CommonEntityFacadeBean;
import org.r3a.common.dblink.utils.SessionFacadeFactory;
import org.r3a.common.entity.ID;
import org.r3a.common.entity.file.FileBean;
import org.r3a.common.entity.query.QueryModel;
import org.r3a.common.vaadin.AbstractSecureWebUI;
import org.r3a.common.vaadin.AbstractWebUI;
import org.r3a.common.vaadin.locale.UILocaleUtil;
import org.r3a.common.vaadin.widget.dialog.Message;
import org.r3a.common.vaadin.widget.form.field.filelist.FileListFieldModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Omarbek
 * @created on 15.03.2018
 */
public class CommonUtils {

    public static final Logger LOG = LoggerFactory.getLogger("ROOT");
    public static int currentYear = Calendar.getInstance().get(Calendar.YEAR);

    public static String getCurrentUserLogin() {
        return AbstractSecureWebUI.getInstance().getUsername();
    }

    public static USERS getCurrentUser() throws Exception {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("login", getCurrentUserLogin());
            EMPLOYEE employee = getEmployee(params);
            if (employee != null) {
                return employee;
            }
            STUDENT student = getStudent(params);
            if (student != null) {
                return student;
            }
        } catch (Exception e) {
            CommonUtils.showMessageAndWriteLog("Unable to get user", e);
        }
        return null;
    }

    private static STUDENT getStudent(Map<String, Object> params) throws Exception {
        return (STUDENT) SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).
                getEntityByNamedQuery("STUDENT.getStudentByLogin", params);
    }

    private static EMPLOYEE getEmployee(Map<String, Object> params) throws Exception {
        return (EMPLOYEE) SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).
                getEntityByNamedQuery("EMPLOYEE.getEmployeeByLogin", params);
    }

    public static String getCode(Integer count) {
        String code = String.valueOf(count);
        if (count < 10) {
            code = "000" + code;
        } else if (count < 100) {
            code = "00" + code;
        } else if (count < 1000) {
            code = "0" + code;
        }
        return code;
    }

    private static String getCodeBuilder(Integer count) {
        String code = String.valueOf(count);
        StringBuilder codeSB = new StringBuilder();
        if (count < 10) {
            codeSB.append("000");
        } else if (count < 100) {
            codeSB.append("00");
        } else if (count < 1000) {
            codeSB.append("0");
        }
        codeSB.append(code);
        return codeSB.toString();
    }

    public static void addFiles(QueryModel<USER_DOCUMENT_FILE> udfQM, FileListFieldModel medicalCheckupFLFM) {
        try {
            List udfList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(udfQM);
            if (!udfList.isEmpty()) {
                for (Object o : udfList) {
                    Object[] oo = (Object[]) o;
                    FileBean fe = new FileBean(USER_DOCUMENT_FILE.class);
                    fe.setId(ID.valueOf((Long) oo[0]));
                    fe.setFileName((String) oo[1]);
                    fe.setNewFile(false);
                    medicalCheckupFLFM.getFileList().add(fe);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to load education document copies: ", ex);
        }
    }

    public static void deleteFiles(FileListFieldModel flfm) {
        for (FileBean fe : flfm.getDeleteList()) {
            try {
                USER_DOCUMENT_FILE udf = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookup(USER_DOCUMENT_FILE.class, fe.getId());
                udf.setDeleted(true);
                SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).merge(udf);
            } catch (Exception ex) {
                LOG.error("Unable to delete repatriate doc copy: ", ex);
            }
        }
    }

    public static UILocaleUtil getUILocaleUtil() {
        return AbstractWebUI.getInstance().getUILocaleUtil();
    }

    public static void showSavedNotification() {
        AbstractWebUI.getInstance().showNotificationInfo(getUILocaleUtil().getMessage("info.record.saved"));
    }

    public static void showMessageAndWriteLog(String message, Exception ex) {
        LOG.error(message + ": ", ex);
        Message.showError(ex.toString());
    }

    public static HorizontalLayout createButtonPanel() {
        HorizontalLayout buttonPanel = new HorizontalLayout();
        buttonPanel.setSpacing(true);
        buttonPanel.setWidthUndefined();
        return buttonPanel;
    }

    public static Button createSaveButton() {
        Button save = new Button();
        save.setData(10);
        save.setWidth(120.0F, Sizeable.Unit.PIXELS);
        save.setIcon(new ThemeResource("img/button/ok.png"));
        save.addStyleName("save");
        save.setCaption(getUILocaleUtil().getCaption("save"));
        return save;
    }

    public static Button createCancelButton() {
        Button cancel = new Button();
        cancel.setData(11);
        cancel.setWidth(120.0F, Sizeable.Unit.PIXELS);
        cancel.setIcon(new ThemeResource("img/button/cancel.png"));
        cancel.addStyleName("cancel");
        cancel.setCaption(getUILocaleUtil().getCaption("cancel"));
        return cancel;
    }
}
