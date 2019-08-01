package org.openelisglobal.workplan.form;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.SafeHtml;

import org.openelisglobal.common.form.BaseForm;
import org.openelisglobal.common.validator.ValidationHelper;
import org.openelisglobal.validation.annotations.ValidDate;
import org.openelisglobal.common.util.IdValuePair;
import org.openelisglobal.common.util.validator.CustomDateValidator.DateRelation;
import org.openelisglobal.resultvalidation.bean.AnalysisItem;
import org.openelisglobal.test.beanItems.TestResultItem;

public class WorkplanForm extends BaseForm {
    public interface PrintWorkplan {
    }

    @ValidDate(relative = DateRelation.TODAY, groups = { PrintWorkplan.class })
    private String currentDate = "";

    // for display
    private String searchLabel;

    // for display
    private List<IdValuePair> searchTypes;

    @Pattern(regexp = ValidationHelper.ID_REGEX, groups = { PrintWorkplan.class })
    private String selectedSearchID = "";

    @Pattern(regexp = ValidationHelper.ID_REGEX, groups = { PrintWorkplan.class })
    private String testTypeID = "";

    @SafeHtml(groups = { PrintWorkplan.class })
    private String testName = "";

    @NotNull(groups = { PrintWorkplan.class })
    private Boolean searchFinished = false;

    @Valid
    private List<TestResultItem> workplanTests;

    @Valid
    private List<AnalysisItem> resultList;

    // TODO switch to an enum?
    // @Pattern(regexp = "^$|^test$|^panel$", groups = { PrintWorkplan.class })
    @Pattern(regexp = "^[a-zA-Z]*")
    private String workplanType = "";

    @Pattern(regexp = "^$|^WorkPlanByPanel.do$|^WorkPlanByTest.do$", groups = { PrintWorkplan.class })
    private String searchAction = "";

    // for display
    private List<IdValuePair> testSections;

    // for display
    private List<IdValuePair> testSectionsByName;

    @Pattern(regexp = ValidationHelper.ID_REGEX, groups = { PrintWorkplan.class })
    private String testSectionId;

    public WorkplanForm() {
        setFormName("WorkplanForm");
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getSearchLabel() {
        return searchLabel;
    }

    public void setSearchLabel(String searchLabel) {
        this.searchLabel = searchLabel;
    }

    public List<IdValuePair> getSearchTypes() {
        return searchTypes;
    }

    public void setSearchTypes(List<IdValuePair> searchTypes) {
        this.searchTypes = searchTypes;
    }

    public String getSelectedSearchID() {
        return selectedSearchID;
    }

    public void setSelectedSearchID(String selectedSearchID) {
        this.selectedSearchID = selectedSearchID;
    }

    public String getTestTypeID() {
        return testTypeID;
    }

    public void setTestTypeID(String testTypeID) {
        this.testTypeID = testTypeID;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Boolean getSearchFinished() {
        return searchFinished;
    }

    public void setSearchFinished(Boolean searchFinished) {
        this.searchFinished = searchFinished;
    }

    public List<TestResultItem> getWorkplanTests() {
        return workplanTests;
    }

    public void setWorkplanTests(List<TestResultItem> workplanTests) {
        this.workplanTests = workplanTests;
    }

    public List<AnalysisItem> getResultList() {
        return resultList;
    }

    public void setResultList(List<AnalysisItem> resultList) {
        this.resultList = resultList;
    }

    public String getWorkplanType() {
        return workplanType;
    }

    public void setWorkplanType(String workplanType) {
        this.workplanType = workplanType;
    }

    public String getSearchAction() {
        return searchAction;
    }

    public void setSearchAction(String searchAction) {
        this.searchAction = searchAction;
    }

    public List<IdValuePair> getTestSections() {
        return testSections;
    }

    public void setTestSections(List<IdValuePair> testSections) {
        this.testSections = testSections;
    }

    public List<IdValuePair> getTestSectionsByName() {
        return testSectionsByName;
    }

    public void setTestSectionsByName(List<IdValuePair> testSectionsByName) {
        this.testSectionsByName = testSectionsByName;
    }

    public String getTestSectionId() {
        return testSectionId;
    }

    public void setTestSectionId(String testSectionId) {
        this.testSectionId = testSectionId;
    }
}
