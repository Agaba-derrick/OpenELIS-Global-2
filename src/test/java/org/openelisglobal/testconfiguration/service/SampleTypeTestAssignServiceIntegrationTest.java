package org.openelisglobal.testconfiguration.service;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openelisglobal.BaseWebContextSensitiveTest;
import org.openelisglobal.typeofsample.service.TypeOfSampleService;
import org.openelisglobal.typeofsample.service.TypeOfSampleTestService;
import org.openelisglobal.typeofsample.valueholder.TypeOfSample;
import org.openelisglobal.typeofsample.valueholder.TypeOfSampleTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for SampleTypeTestAssignService
 *
 * Follows the OpenELIS Global 2 backend integration testing standards: -
 * Extends BaseWebContextSensitiveTest for full Spring context and
 * Testcontainers support. - Uses DBUnit for data seeding.
 */
public class SampleTypeTestAssignServiceIntegrationTest extends BaseWebContextSensitiveTest {

    @Autowired
    private SampleTypeTestAssignService sampleTypeTestAssignService;

    @Autowired
    private TypeOfSampleService typeOfSampleService;

    @Autowired
    private TypeOfSampleTestService typeOfSampleTestService;

    @Before
    public void init() throws Exception {
        executeDataSetWithStateManagement("testdata/sample-type-test.xml");
    }

    @Test
    public void update_ShouldDeleteExistingAndCreateNew_WhenFlagsAreTrue() {
        TypeOfSample sampleType1001 = typeOfSampleService.get("1001");
        Assert.assertNotNull("Sample type should exist", sampleType1001);

        List<TypeOfSampleTest> existingLinks = typeOfSampleTestService.getTypeOfSampleTestsForTest("2001");
        Assert.assertEquals("Should have 1 existing link", 1, existingLinks.size());
        String linkIdToDelete = existingLinks.get(0).getId();

        // Perform the full update: delete existing, update sample type, create new link
        sampleType1001.setLocalAbbreviation("upd_abbrev");
        sampleTypeTestAssignService.update(sampleType1001, "2002", Arrays.asList(linkIdToDelete), "1001", true, true,
                null, "1");

        // Verify the old link is deleted
        List<TypeOfSampleTest> remainingLinksFor2001 = typeOfSampleTestService.getTypeOfSampleTestsForTest("2001");
        Assert.assertEquals("Old link should be deleted", 0, remainingLinksFor2001.size());

        // Verify the new link is created
        List<TypeOfSampleTest> newLinksFor2002 = typeOfSampleTestService.getTypeOfSampleTestsForTest("2002");
        Assert.assertTrue("Should have new link",
                newLinksFor2002.stream().anyMatch(l -> "1001".equals(l.getTypeOfSampleId())));

        // Verify the sample type was updated
        TypeOfSample updatedSampleType = typeOfSampleService.get("1001");
        Assert.assertEquals("Sample type abbreviation should be updated", "upd_abbrev",
                updatedSampleType.getLocalAbbreviation());
    }

    @Test
    public void update_ShouldDeactivateSampleType_WhenProvided() {
        TypeOfSample sampleType1001 = typeOfSampleService.get("1001");
        TypeOfSample sampleType1002 = typeOfSampleService.get("1002");

        Assert.assertTrue(sampleType1002.getIsActive());

        sampleType1002.setIsActive(false);

        // Perform assignment while deactivating another sample type
        sampleTypeTestAssignService.update(sampleType1001, "2002", null, "1001", false, false, sampleType1002, "1");

        TypeOfSample deactivatedSampleType = typeOfSampleService.get("1002");
        Assert.assertFalse("Sample type 1002 should be deactivated", deactivatedSampleType.getIsActive());

        // Verify assignment still happened
        List<TypeOfSampleTest> newLinksFor2002 = typeOfSampleTestService.getTypeOfSampleTestsForTest("2002");
        Assert.assertTrue("Should have new link for 1001",
                newLinksFor2002.stream().anyMatch(l -> "1001".equals(l.getTypeOfSampleId())));
    }

    @Test
    public void update_ShouldOnlyCreateNewLink_WhenFlagsAreFalse() {
        TypeOfSample sampleType1001 = typeOfSampleService.get("1001");
        String originalAbbrev = sampleType1001.getLocalAbbreviation();
        sampleType1001.setLocalAbbreviation("should_not_save");

        // Test with false flags
        sampleTypeTestAssignService.update(sampleType1001, "2002", null, "1001", false, false, null, "1");

        // Verify sample type was NOT updated
        TypeOfSample reloadedSampleType = typeOfSampleService.get("1001");
        Assert.assertEquals("Sample type should not be updated", originalAbbrev,
                reloadedSampleType.getLocalAbbreviation());

        // Verify the new link is created
        List<TypeOfSampleTest> linksFor2002 = typeOfSampleTestService.getTypeOfSampleTestsForTest("2002");
        Assert.assertTrue("Should have new link for 1001",
                linksFor2002.stream().anyMatch(l -> "1001".equals(l.getTypeOfSampleId())));
    }
}
