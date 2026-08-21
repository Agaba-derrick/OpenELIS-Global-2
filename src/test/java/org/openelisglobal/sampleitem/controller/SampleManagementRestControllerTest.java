package org.openelisglobal.sampleitem.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.openelisglobal.BaseWebContextSensitiveTest;
import org.openelisglobal.analysis.service.AnalysisService;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.common.action.IActionConstants;
import org.openelisglobal.common.services.IStatusService;
import org.openelisglobal.common.services.StatusService;
import org.openelisglobal.login.valueholder.UserSessionData;
import org.openelisglobal.sampleitem.dto.AddTestsResponse;
import org.openelisglobal.sampleitem.dto.CancelTestResponse;
import org.openelisglobal.sampleitem.dto.CreateAliquotResponse;
import org.openelisglobal.sampleitem.dto.SearchSamplesResponse;
import org.openelisglobal.sampleitem.form.AddTestsForm;
import org.openelisglobal.sampleitem.form.CancelTestForm;
import org.openelisglobal.sampleitem.form.CreateAliquotForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration tests for {@link SampleManagementRestController} exercising all
 * four REST endpoints through the full Spring MVC stack with MockMvc.
 *
 * <p>
 * Related: Feature 001-sample-management
 */
@Rollback
@WithMockUser(username = "admin", roles = { "ADMIN", "RESULTS" })
public class SampleManagementRestControllerTest extends BaseWebContextSensitiveTest {

    private static final String BASE_PATH = "/rest/sample-management";
    private static final String ACCESSION = "SM-TEST-001";

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private IStatusService statusService;

    private ObjectMapper objectMapper;
    private MockHttpSession session;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        executeDataSetWithStateManagement("testdata/sample-management-controller.xml");
        session = buildAuthenticatedSession();
    }

    @Test
    public void searchByAccessionNumber_shouldReturnSampleItems() throws Exception {
        MvcResult result = mockMvc
                .perform(get(BASE_PATH + "/search").param("accessionNumber", ACCESSION).session(session))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        SearchSamplesResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                SearchSamplesResponse.class);

        assertEquals(ACCESSION, response.getAccessionNumber());
        assertEquals("Total count should be exactly 2", 2, response.getTotalCount());
        assertEquals("Should return exactly 2 sample items", 2, response.getSampleItems().size());
        assertEquals("External ID of first item should match", "SM-TEST-001-1",
                response.getSampleItems().get(0).getExternalId());
        assertEquals("External ID of second item should match", "SM-TEST-001-2",
                response.getSampleItems().get(1).getExternalId());
    }

    @Test
    public void searchByAccessionNumber_unknownAccession_returnsEmpty() throws Exception {
        MvcResult result = mockMvc
                .perform(get(BASE_PATH + "/search").param("accessionNumber", "UNKNOWN").session(session))
                .andExpect(status().isOk()).andReturn();

        SearchSamplesResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                SearchSamplesResponse.class);

        assertEquals("UNKNOWN", response.getAccessionNumber());
        assertNotNull(response.getSampleItems());
        assertEquals(0, response.getSampleItems().size());
        assertEquals(0, response.getTotalCount());
    }

    @Test
    public void searchByAccessionNumber_blankAccessionNumber_returnsEmpty200() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_PATH + "/search").param("accessionNumber", "").session(session))
                .andExpect(status().isOk()).andReturn();

        SearchSamplesResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                SearchSamplesResponse.class);

        assertEquals("", response.getAccessionNumber());
        assertEquals(0, response.getSampleItems().size());
        assertEquals(0, response.getTotalCount());
    }

    @Test
    public void createAliquot_shouldCreateChildSampleItem() throws Exception {
        CreateAliquotForm form = new CreateAliquotForm();
        form.setParentSampleItemId("10001");
        form.setQuantityToTransfer(new BigDecimal("2.5"));
        form.setNumberOfAliquots(1);
        form.setNotes("Test Aliquot");

        MvcResult result = mockMvc
                .perform(post(BASE_PATH + "/aliquot").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)).session(session))
                .andExpect(status().isCreated()).andReturn();

        CreateAliquotResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                CreateAliquotResponse.class);

        assertTrue("Aliquot external ID should use .{n} suffix",
                response.getAliquot().getExternalId().matches("SM-TEST-001-1\\.\\d+"));
        assertEquals(1, response.getAliquotCount());

        // Parent remaining quantity was 10.0, we took 2.5
        assertEquals("Parent remaining quantity should be reduced to 7.5", 7.5,
                response.getParentUpdatedRemainingQuantity().doubleValue(), 0.001);
        assertEquals("Aliquot quantity should be 2.5", 2.5, response.getQuantityPerAliquot().doubleValue(), 0.001);
    }

    @Test
    public void createAliquot_zeroQuantity_returns400() throws Exception {
        CreateAliquotForm form = new CreateAliquotForm();
        form.setParentSampleItemId("10001");
        form.setQuantityToTransfer(new BigDecimal("0"));

        mockMvc.perform(post(BASE_PATH + "/aliquot").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)).session(session)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void createAliquot_invalidParentId_returns400() throws Exception {
        CreateAliquotForm form = new CreateAliquotForm();
        form.setParentSampleItemId("99999");
        form.setQuantityToTransfer(new BigDecimal("1.0"));
        form.setNumberOfAliquots(1);

        mockMvc.perform(post(BASE_PATH + "/aliquot").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)).session(session)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void addTestsToSamples_shouldLinkTestsToSampleItem() throws Exception {
        AddTestsForm form = new AddTestsForm();
        form.setSampleItemIds(Arrays.asList("10001"));
        form.setTestIds(Arrays.asList("2"));

        MvcResult result = mockMvc
                .perform(post(BASE_PATH + "/add-tests").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)).session(session))
                .andExpect(status().isOk()).andReturn();

        AddTestsResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                AddTestsResponse.class);

        assertEquals("Should successfully add exactly 1 test", 1, response.getSuccessCount());
        assertEquals(1, response.getResults().size());
        assertTrue(response.getResults().get(0).isSuccess());
        assertEquals("10001", response.getResults().get(0).getSampleItemId());
        assertEquals("2", response.getResults().get(0).getAddedTestIds().get(0));
        assertTrue(response.getResults().get(0).getSkippedTestIds().isEmpty());
    }

    @Test
    public void addTestsToSamples_duplicateTest_isSkipped() throws Exception {
        AddTestsForm form = new AddTestsForm();
        form.setSampleItemIds(Arrays.asList("10002"));
        form.setTestIds(Arrays.asList("1"));

        MvcResult result = mockMvc
                .perform(post(BASE_PATH + "/add-tests").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)).session(session))
                .andExpect(status().isOk()).andReturn();

        AddTestsResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                AddTestsResponse.class);

        assertEquals(0, response.getSuccessCount());
        assertEquals(1, response.getResults().size());
        assertTrue(response.getResults().get(0).isSuccess());
        assertEquals("10002", response.getResults().get(0).getSampleItemId());
        assertTrue(response.getResults().get(0).getAddedTestIds().isEmpty());
        assertEquals("1", response.getResults().get(0).getSkippedTestIds().get(0));
    }

    @Test
    public void addTestsToSamples_invalidSampleItemId_returns400() throws Exception {
        AddTestsForm form = new AddTestsForm();
        form.setSampleItemIds(Arrays.asList("99999"));
        form.setTestIds(Arrays.asList("1"));

        mockMvc.perform(post(BASE_PATH + "/add-tests").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)).session(session)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void cancelTest_shouldSetAnalysisStatusToCancelled() throws Exception {
        CancelTestForm form = new CancelTestForm();
        form.setAnalysisId("10001");
        form.setSampleItemId("10001");

        MvcResult result = mockMvc
                .perform(post(BASE_PATH + "/cancel-test").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)).session(session))
                .andExpect(status().isOk()).andReturn();

        CancelTestResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                CancelTestResponse.class);

        assertTrue(response.isSuccess());
        assertEquals("10001", response.getAnalysisId());

        Analysis analysis = analysisService.getAnalysisById("10001");
        String canceledStatusId = statusService.getStatusID(StatusService.AnalysisStatus.Canceled);
        assertEquals(canceledStatusId, analysis.getStatusId());
    }

    @Test
    public void cancelTest_completedAnalysis_returns400() throws Exception {
        // Analysis 10002 is Finalized on sample item 10002
        CancelTestForm form = new CancelTestForm();
        form.setAnalysisId("10002");
        form.setSampleItemId("10002");

        mockMvc.perform(post(BASE_PATH + "/cancel-test").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)).session(session)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    private MockHttpSession buildAuthenticatedSession() {
        UserDetails userDetails = User.withUsername("admin").password("N/A").authorities("ROLE_ADMIN", "ROLE_RESULTS")
                .build();
        SecurityContext sc = new SecurityContextImpl();
        sc.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, "N/A", userDetails.getAuthorities()));

        UserSessionData usd = new UserSessionData();
        usd.setSytemUserId(1);

        MockHttpSession httpSession = new MockHttpSession();
        httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
        httpSession.setAttribute(IActionConstants.USER_SESSION_DATA, usd);
        return httpSession;
    }
}
