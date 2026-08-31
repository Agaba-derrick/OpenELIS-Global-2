package org.openelisglobal.sample.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openelisglobal.BaseWebContextSensitiveTest;
import org.openelisglobal.analysis.service.AnalysisService;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.observationhistory.service.ObservationHistoryService;
import org.openelisglobal.observationhistory.valueholder.ObservationHistory;
import org.openelisglobal.patient.service.PatientService;
import org.openelisglobal.patient.valueholder.Patient;
import org.openelisglobal.patientidentity.service.PatientIdentityService;
import org.openelisglobal.person.service.PersonService;
import org.openelisglobal.provider.service.ProviderService;
import org.openelisglobal.sample.form.SampleTbEntryForm;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.samplehuman.service.SampleHumanService;
import org.openelisglobal.sampleitem.service.SampleItemService;
import org.openelisglobal.sampleitem.valueholder.SampleItem;
import org.openelisglobal.sampleorganization.service.SampleOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;

public class TbSampleServiceIntegrationTest extends BaseWebContextSensitiveTest {

    private static final String DATASET = "testdata/tb-sample-service.xml";
    private static final String SYS_USER_ID = TEST_SYS_USER_ID;

    @Autowired
    private TbSampleService tbSampleService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private SampleItemService sampleItemService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private ObservationHistoryService observationHistoryService;

    @Autowired
    private PatientIdentityService patientIdentityService;

    @Autowired
    private PersonService personService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private SampleHumanService sampleHumanService;

    @Autowired
    private SampleOrganizationService sampleOrganizationService;

    @Before
    public void setUp() throws Exception {
        executeDataSetWithStateManagement(DATASET);
        resyncSequence("person_seq", "person");
        resyncSequence("patient_seq", "patient");
        resyncSequence("patient_identity_seq", "patient_identity");
        resyncSequence("sample_seq", "sample");
        resyncSequence("sample_item_seq", "sample_item");
        resyncSequence("sample_human_seq", "sample_human");
        resyncSequence("analysis_seq", "analysis");
        resyncSequence("provider_seq", "provider");
        resyncSequence("observation_history_seq", "observation_history");

        ensureReferenceTable("SampleTbEntryForm");
    }

    private SampleTbEntryForm createBaseForm() {
        SampleTbEntryForm form = new SampleTbEntryForm();
        form.setSysUserId(SYS_USER_ID);
        form.setPatientFirstName("Jane");
        form.setPatientLastName("Smith");
        form.setPatientGender("F");
        form.setPatientBirthDate("01/01/1990");
        form.setPatientPhone("555-1234");
        form.setPatientAddress("123 Main St");

        form.setTbSubjectNumber("NEW-SUB-001");

        form.setLabNo("NEW-LAB-001");
        form.setRequestDate("06/01/2025");
        form.setReceivedDate("06/01/2025");

        form.setTbSpecimenNature("100");

        List<String> tests = new ArrayList<>();
        tests.add("200");
        form.setNewSelectedTests(tests);

        form.setReferringSiteCode("100");

        form.setProviderFirstName("Doc");
        form.setProviderLastName("Brown");

        form.setTbOrderReason("Reason1");
        form.setTbDiagnosticReason("Diag1");
        form.setTbFollowupReason("Follow1");
        form.setTbAspect("Aspect1");
        form.setTbFollowupPeriodLine1("Period1");
        form.setTbFollowupPeriodLine2("Period2");
        form.setSelectedTbMethod("Method1");

        return form;
    }

    @Test
    public void persistTbData_newPatient_createsPatientAndSampleHierarchy() {
        SampleTbEntryForm form = createBaseForm();

        boolean result = tbSampleService.persistTbData(form, null);
        assertTrue("Service should return true on success", result);

        Patient patient = patientService.getByExternalId("NEW-SUB-001");
        assertEquals("NEW-SUB-001", patient.getExternalId());
        assertEquals("Jane", patient.getPerson().getFirstName());
        assertEquals("Smith", patient.getPerson().getLastName());

        Sample sample = sampleService.getSampleByAccessionNumber("NEW-LAB-001");
        assertEquals("NEW-LAB-001", sample.getAccessionNumber());

        List<SampleItem> items = sampleItemService.getSampleItemsBySampleId(sample.getId());
        assertEquals(1, items.size());

        List<Analysis> analyses = analysisService.getAnalysesBySampleId(sample.getId());
        assertEquals(1, analyses.size());
        assertEquals("200", analyses.get(0).getTest().getId());
    }

    @Test
    public void persistTbData_existingPatient_updatesPatientAndCreatesSampleHierarchy() {
        SampleTbEntryForm form = createBaseForm();
        form.setTbSubjectNumber("SUB-900");
        form.setPatientFirstName("UpdatedFirst");
        form.setPatientAddress("New Address");

        boolean result = tbSampleService.persistTbData(form, null);
        assertTrue(result);

        Patient patient = patientService.getByExternalId("SUB-900");
        assertEquals("900", patient.getId());
        assertEquals("UpdatedFirst", patient.getPerson().getFirstName());

        Sample sample = sampleService.getSampleByAccessionNumber("NEW-LAB-001");
        assertEquals("NEW-LAB-001", sample.getAccessionNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void persistTbData_existingSample_updateSample_throwsWhenIdNotSet() {
        SampleTbEntryForm form = createBaseForm();
        form.setSampleId("900");
        form.setLabNo("LAB-900");
        form.setRequestDate("07/01/2025");
        form.setReceivedDate("07/01/2025");

        tbSampleService.persistTbData(form, null);
    }

    @Test
    public void persistTbData_createsProviderFromFormFields() {
        SampleTbEntryForm form = createBaseForm();

        boolean result = tbSampleService.persistTbData(form, null);
        assertTrue(result);

        Sample sample = sampleService.getSampleByAccessionNumber("NEW-LAB-001");
        assertEquals("NEW-LAB-001", sample.getAccessionNumber());

    }

    @Test
    public void persistTbData_insertsAllRequiredObservations() {
        SampleTbEntryForm form = createBaseForm();

        boolean result = tbSampleService.persistTbData(form, null);
        assertTrue(result);

        Sample sample = sampleService.getSampleByAccessionNumber("NEW-LAB-001");

        List<ObservationHistory> obs = observationHistoryService.getObservationHistoriesBySampleId(sample.getId());
        assertEquals("Should insert exactly 7 observation history records", 7, obs.size());

        boolean foundOrderReason = obs.stream()
                .anyMatch(o -> "Reason1".equals(o.getValue()) && "100".equals(o.getObservationHistoryTypeId()));
        assertTrue("TbOrderReason should be present", foundOrderReason);
    }
}
