package org.openelisglobal.testmethod.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openelisglobal.BaseWebContextSensitiveTest;
import org.openelisglobal.common.util.IdValuePair;
import org.openelisglobal.testmethod.service.TestMethodService.InlineCreateData;
import org.openelisglobal.testmethod.service.TestMethodService.TestMethodDto;
import org.openelisglobal.testmethod.valueholder.TestMethod;
import org.springframework.beans.factory.annotation.Autowired;

public class TestMethodServiceTest extends BaseWebContextSensitiveTest {

    @Autowired
    private TestMethodService testMethodService;

    @Before
    public void init() throws Exception {
        executeDataSetWithStateManagement("testdata/test_method.xml");
    }

    @Test
    public void findLinkById_shouldReturnExistingTestMethodLink() {
        TestMethod link = testMethodService.findLinkById("1000");

        assertEquals("1000", link.getId());
        assertEquals("10", link.getTestId());
        assertEquals("100", link.getMethodId());
        assertTrue(link.getIsDefaultMethod());
        assertEquals("2025-01-01", link.getEffectiveDate().toString());
        assertEquals("Y", link.getIsActive());
    }

    @Test
    public void findLinkById_shouldReturnNullWhenLinkDoesNotExist() {
        TestMethod link = testMethodService.findLinkById("non-existent-link-id");

        assertNull(link);
    }

    @Test
    public void getActiveTestMethodsByTestId_shouldReturnOnlyActiveLinksForTest() {
        List<TestMethod> activeLinks = testMethodService.getActiveTestMethodsByTestId("10");

        assertEquals(2, activeLinks.size());
        assertEquals("1000", activeLinks.get(0).getId());
        assertEquals("100", activeLinks.get(0).getMethodId());
        assertTrue(activeLinks.get(0).getIsDefaultMethod());
        assertEquals("Y", activeLinks.get(0).getIsActive());

        assertEquals("1001", activeLinks.get(1).getId());
        assertEquals("200", activeLinks.get(1).getMethodId());
        assertFalse(activeLinks.get(1).getIsDefaultMethod());
        assertEquals("Y", activeLinks.get(1).getIsActive());
    }

    @Test
    public void testMethodLinkExists_shouldReturnTrueForExistingActiveLink() {
        boolean exists = testMethodService.testMethodLinkExists("10", "100");

        assertTrue(exists);
    }

    @Test
    public void testMethodLinkExists_shouldReturnFalseForNonExistentOrInactiveLink() {
        boolean nonExistent = testMethodService.testMethodLinkExists("10", "999");
        boolean inactive = testMethodService.testMethodLinkExists("10", "300");

        assertFalse(nonExistent);
        assertFalse(inactive);
    }

    @Test
    public void linkMethod_shouldInsertNewLinkAndClearExistingDefaultWhenNewIsDefault() {
        TestMethod newLink = new TestMethod();
        newLink.setTestId("10");
        newLink.setMethodId("300");
        newLink.setIsDefaultMethod(true);
        newLink.setEffectiveDate(Date.valueOf("2025-03-01"));
        newLink.setIsActive("Y");
        newLink.setSysUserId("1");

        TestMethod savedLink = testMethodService.linkMethod(newLink);

        assertEquals("10", savedLink.getTestId());
        assertEquals("300", savedLink.getMethodId());
        assertTrue(savedLink.getIsDefaultMethod());
        assertEquals("2025-03-01", savedLink.getEffectiveDate().toString());
        assertEquals("Y", savedLink.getIsActive());

        // Verify pre-existing default (1000) was cleared to false
        TestMethod oldDefault = testMethodService.findLinkById("1000");
        assertFalse(oldDefault.getIsDefaultMethod());

        // Verify 300 is now the new default method id
        assertEquals("300", testMethodService.getDefaultMethodId("10"));
    }

    @Test
    public void updateLink_shouldUpdateDefaultAndEffectiveDateEnforcingSingleDefault() {
        TestMethod linkToUpdate = testMethodService.findLinkById("1001");
        linkToUpdate.setIsDefaultMethod(true);
        linkToUpdate.setEffectiveDate(Date.valueOf("2025-06-01"));

        TestMethod updated = testMethodService.updateLink(linkToUpdate);

        assertEquals("1001", updated.getId());
        assertTrue(updated.getIsDefaultMethod());
        assertEquals("2025-06-01", updated.getEffectiveDate().toString());

        // Verify old default (1000) is cleared
        TestMethod oldDefault = testMethodService.findLinkById("1000");
        assertFalse(oldDefault.getIsDefaultMethod());
        assertEquals("200", testMethodService.getDefaultMethodId("10"));
    }

    @Test
    public void removeLink_shouldDeactivateLink() {
        testMethodService.removeLink("1000", "1");

        TestMethod deactivated = testMethodService.findLinkById("1000");
        assertEquals("N", deactivated.getIsActive());

        List<TestMethod> activeLinks = testMethodService.getActiveTestMethodsByTestId("10");
        assertEquals(1, activeLinks.size());
        assertEquals("1001", activeLinks.get(0).getId());
    }

    @Test
    public void getDefaultMethodId_shouldReturnDefaultMethodIdOrNull() {
        String defaultMethodId = testMethodService.getDefaultMethodId("10");
        assertEquals("100", defaultMethodId);

        String nullDefault = testMethodService.getDefaultMethodId("999");
        assertNull(nullDefault);
    }

    @Test
    public void getMethodDisplayListForTest_shouldReturnIdValuePairsForActiveMethods() {
        List<IdValuePair> displayList = testMethodService.getMethodDisplayListForTest("10");

        assertEquals(2, displayList.size());
        assertEquals("100", displayList.get(0).getId());
        assertEquals("Microscopy", displayList.get(0).getValue());

        assertEquals("200", displayList.get(1).getId());
        assertEquals("PCR", displayList.get(1).getValue());
    }

    @Test
    public void getMethodDisplayListForTest_shouldReturnNullWhenNoActiveMethods() {
        List<IdValuePair> displayList = testMethodService.getMethodDisplayListForTest("999");

        assertNull(displayList);
    }

    @Test
    public void getLinkedMethodDtos_shouldReturnDtoListWithDetails() {
        List<TestMethodDto> dtos = testMethodService.getLinkedMethodDtos("10");

        assertEquals(2, dtos.size());

        assertEquals("1000", dtos.get(0).id);
        assertEquals("100", dtos.get(0).methodId);
        assertEquals("Microscopy", dtos.get(0).methodName);
        assertEquals("MIC", dtos.get(0).methodCode);
        assertTrue(dtos.get(0).isDefault);
        assertEquals("2025-01-01", dtos.get(0).effectiveDate);

        assertEquals("1001", dtos.get(1).id);
        assertEquals("200", dtos.get(1).methodId);
        assertEquals("PCR", dtos.get(1).methodName);
        assertEquals("PCR", dtos.get(1).methodCode);
        assertFalse(dtos.get(1).isDefault);
        assertEquals("2025-01-15", dtos.get(1).effectiveDate);
    }

    @Test
    public void linkMethodDto_shouldReturnDtoForLinkedMethod() {
        TestMethod tm = new TestMethod();
        tm.setTestId("20");
        tm.setMethodId("100");
        tm.setIsDefaultMethod(false);
        tm.setEffectiveDate(Date.valueOf("2025-02-15"));
        tm.setIsActive("Y");
        tm.setSysUserId("1");

        TestMethodDto dto = testMethodService.linkMethodDto(tm);

        assertEquals("100", dto.methodId);
        assertEquals("Microscopy", dto.methodName);
        assertEquals("MIC", dto.methodCode);
        assertFalse(dto.isDefault);
        assertEquals("2025-02-15", dto.effectiveDate);
    }

    @Test
    public void updateLinkDto_shouldReturnDtoForUpdatedLink() {
        TestMethod link = testMethodService.findLinkById("1001");
        link.setEffectiveDate(Date.valueOf("2025-05-01"));

        TestMethodDto dto = testMethodService.updateLinkDto(link);

        assertEquals("1001", dto.id);
        assertEquals("200", dto.methodId);
        assertEquals("PCR", dto.methodName);
        assertEquals("PCR", dto.methodCode);
        assertEquals("2025-05-01", dto.effectiveDate);
    }

    @Test
    public void createAndLinkMethod_shouldCreateMethodLocalizationAndLinkToTest() {
        InlineCreateData data = new InlineCreateData();
        data.nameEnglish = "ELISA Assay";
        data.nameFrench = "Dosage ELISA";
        data.code = "elisa";
        data.isDefault = false;
        data.effectiveDate = Date.valueOf("2025-04-01");
        data.sysUserId = "1";

        TestMethodDto dto = testMethodService.createAndLinkMethod("10", data);

        assertEquals("ELISA Assay", dto.methodName);
        assertEquals("ELISA", dto.methodCode);
        assertFalse(dto.isDefault);
        assertEquals("2025-04-01", dto.effectiveDate);

        // Verify active methods for test 10 increased to 3
        List<TestMethod> activeLinks = testMethodService.getActiveTestMethodsByTestId("10");
        assertEquals(3, activeLinks.size());
    }

    @Test
    public void copyMethodsFromTest_shouldCopyNonDuplicateActiveMethodsFromSourceToTarget() {
        // Test 10 has active methods 100 and 200.
        // Test 20 already has method 200.
        // Copying from 10 to 20 should copy only method 100.

        testMethodService.copyMethodsFromTest("10", "20", "1");

        List<TestMethod> targetLinks = testMethodService.getActiveTestMethodsByTestId("20");
        assertEquals(2, targetLinks.size());

        TestMethod existingLink = targetLinks.stream().filter(l -> "200".equals(l.getMethodId())).findFirst()
                .orElse(null);
        TestMethod copiedLink = targetLinks.stream().filter(l -> "100".equals(l.getMethodId())).findFirst()
                .orElse(null);

        assertEquals("200", existingLink.getMethodId());
        assertTrue(existingLink.getIsDefaultMethod());

        assertEquals("100", copiedLink.getMethodId());
        assertEquals("20", copiedLink.getTestId());
        assertFalse(copiedLink.getIsDefaultMethod());
    }
}
