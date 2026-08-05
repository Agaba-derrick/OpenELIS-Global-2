package org.openelisglobal.${module};

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openelisglobal.BaseWebContextSensitiveTest;
import org.openelisglobal.${module}.service.${ServiceName};
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for ${ServiceName}
 * 
 * Follows the OpenELIS Global 2 backend integration testing standards:
 * - Extends BaseWebContextSensitiveTest for full Spring context and
 * Testcontainers support.
 * - Uses DBUnit for data seeding.
 */
public class ${ServiceName}IntegrationTest extends BaseWebContextSensitiveTest {

    @Autowired
    private ${ServiceName} ${serviceName};

    @Before
    public void init() throws Exception {
        // Load data specific to this test suite
        // executeDataSetWithStateManagement("testdata/${serviceName}.xml");
    }

    @Test
    public void testMethod_ShouldBehavior() {
        // Arrange

        // Act

        // Assert
        // Do NOT use weak assertions like assertNotNull or assertNull.
        // Assert specifically on exact sizes, specific fields, and expected states:
        // Assert.assertEquals(1, results.size());
        // Assert.assertEquals("expectedValue", results.get(0).getSomeField());
    }
}
