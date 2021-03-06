package kz.halyqsoft.univercity.entity.beans.univercity;

import kz.halyqsoft.univercity.entity.beans.USERS;
import org.r3a.common.entity.AbstractEntity;
import org.r3a.common.entity.EFieldType;
import org.r3a.common.entity.FieldInfo;

import javax.persistence.*;
import java.util.Date;

@Entity
public class DOCUMENT extends AbstractEntity{

    @FieldInfo(type = EFieldType.FK_COMBO, inGrid = false, inEdit = false, inView = false)
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "creator_employee_id", referencedColumnName = "ID", nullable = false)})
    private EMPLOYEE creatorEmployee;


    @FieldInfo(type = EFieldType.FK_COMBO, inGrid = false, inEdit = false, inView = false)
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "document_status_id", referencedColumnName = "ID", nullable = false)})
    private DOCUMENT_STATUS documentStatus;

    @FieldInfo(type = EFieldType.FK_COMBO, inGrid = false, inEdit = false, inView = false)
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "document_importance_id", referencedColumnName = "ID", nullable = false)})
    private DOCUMENT_IMPORTANCE documentImportance;


    @FieldInfo(type = EFieldType.FK_COMBO, inGrid = false, inEdit = false, inView = false)
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "pdf_document_id", referencedColumnName = "ID", nullable = false)})
    private PDF_DOCUMENT pdfDocument;


    @FieldInfo(type = EFieldType.TEXT, inGrid = false, inEdit = false, inView = false)
    @Column(name = "message")
    private String message;

    @FieldInfo(type = EFieldType.TEXT, inGrid = false, inEdit = false, inView = false)
    @Column(name = "note")
    private String note;

    @FieldInfo(type = EFieldType.DATETIME, required = false, readOnlyFixed = true, inGrid = false, inEdit = false, inView = false)
    @Column(name = "deadline_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deadlineDate;



    @FieldInfo(type = EFieldType.DATETIME, required = false, readOnlyFixed = true, inGrid = false, inEdit = false, inView = false)
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;


    @FieldInfo(type = EFieldType.DATETIME, required = false, readOnlyFixed = true, inGrid = false, inEdit = false, inView = false)
    @Column(name = "updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;


    @FieldInfo(type = EFieldType.BOOLEAN, required = false, readOnlyFixed = true, inGrid = false, inEdit = false, inView = false)
    @Column(name = "deleted")
    private boolean deleted;

    public EMPLOYEE getCreatorEmployee() {
        return creatorEmployee;
    }

    public void setCreatorEmployee(EMPLOYEE creatorEmployee) {
        this.creatorEmployee = creatorEmployee;
    }

    public DOCUMENT_STATUS getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(DOCUMENT_STATUS documentStatus) {
        this.documentStatus = documentStatus;
    }

    public PDF_DOCUMENT getPdfDocument() {
        return pdfDocument;
    }

    public void setPdfDocument(PDF_DOCUMENT pdfDocument) {
        this.pdfDocument = pdfDocument;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(Date deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    public DOCUMENT_IMPORTANCE getDocumentImportance() {
        return documentImportance;
    }

    public void setDocumentImportance(DOCUMENT_IMPORTANCE documentImportance) {
        this.documentImportance = documentImportance;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public DOCUMENT() {
    }

}
