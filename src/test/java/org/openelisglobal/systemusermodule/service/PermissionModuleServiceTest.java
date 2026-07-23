package org.openelisglobal.systemusermodule.service;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openelisglobal.BaseWebContextSensitiveTest;
import org.openelisglobal.common.util.ConfigurationProperties;
import org.openelisglobal.common.util.DefaultConfigurationProperties;
import org.openelisglobal.systemusermodule.valueholder.PermissionModule;
import org.openelisglobal.systemusermodule.valueholder.RoleModule;
import org.openelisglobal.systemusermodule.valueholder.SystemUserModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

public class PermissionModuleServiceTest extends BaseWebContextSensitiveTest {

    @Autowired
    private PermissionModuleService<PermissionModule> permissionModuleService;

    private String originalPermissionsAgent;

    @Before
    public void setup() throws Exception {
        executeDataSetWithStateManagement("testdata/permission-module.xml");
        originalPermissionsAgent = ConfigurationProperties.getInstance().getPropertyValue("permissions.agent");
    }

    @After
    public void tearDown() {
        setPermissionsAgent(originalPermissionsAgent);
    }

    private void setPermissionsAgent(String agent) {
        DefaultConfigurationProperties.OEProperties props = (DefaultConfigurationProperties.OEProperties) ReflectionTestUtils
                .getField(ConfigurationProperties.getInstance(), "finalProperties");
        props.setPropertyValue("permissions.agent", agent);
    }

    @Test
    public void getAllPermissionModules_ShouldDelegateToRoleModuleService_WhenAgentIsRole() {
        setPermissionsAgent("Role");
        List<PermissionModule> modules = permissionModuleService.getAllPermissionModules();
        assertEquals(3, modules.size());

        RoleModule first = (RoleModule) modules.stream().filter(m -> "2001".equals(m.getSystemModule().getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected RoleModule for system module 2001 not found"));
        assertEquals(RoleModule.class, first.getClass());
        assertEquals("2001", first.getSystemModule().getId());
        assertEquals("Y", first.getHasSelect());
        assertEquals("Y", first.getHasAdd());
        assertEquals("N", first.getHasUpdate());
        assertEquals("N", first.getHasDelete());
        assertEquals("3001", first.getRole().getId());
    }

    @Test
    public void getAllPermissionModules_ShouldDelegateToSystemUserModuleService_WhenAgentIsUser() {
        setPermissionsAgent("USER");
        List<PermissionModule> modules = permissionModuleService.getAllPermissionModules();
        assertEquals(3, modules.size());

        SystemUserModule first = (SystemUserModule) modules.stream()
                .filter(m -> "2001".equals(m.getSystemModule().getId())).findFirst()
                .orElseThrow(() -> new AssertionError("Expected SystemUserModule for system module 2001 not found"));
        assertEquals(SystemUserModule.class, first.getClass());
        assertEquals("2001", first.getSystemModule().getId());
        assertEquals("Y", first.getHasSelect());
        assertEquals("N", first.getHasAdd());
        assertEquals("Y", first.getHasUpdate());
        assertEquals("N", first.getHasDelete());
        assertEquals("1001", first.getSystemUser().getId());
    }

    @Test
    public void doesUserHaveAnyModules_ShouldDelegateToRoleModuleService_WhenAgentIsRole() {
        setPermissionsAgent("Role");
        assertEquals(true, permissionModuleService.doesUserHaveAnyModules(1001));
        assertEquals(false, permissionModuleService.doesUserHaveAnyModules(9999));
    }

    @Test
    public void doesUserHaveAnyModules_ShouldDelegateToSystemUserModuleService_WhenAgentIsUser() {
        setPermissionsAgent("USER");
        assertEquals(true, permissionModuleService.doesUserHaveAnyModules(1001));
        assertEquals(false, permissionModuleService.doesUserHaveAnyModules(9999));
    }

    @Test
    public void getAllPermittedPagesFromAgentId_ShouldDelegateToRoleModuleService_WhenAgentIsRole() {
        setPermissionsAgent("Role");
        Set<String> permittedPages = permissionModuleService.getAllPermittedPagesFromAgentId(3001);
        assertEquals(2, permittedPages.size());
        assertEquals(true, permittedPages.contains("Module 1"));
        assertEquals(true, permittedPages.contains("Module 2"));
    }

    @Test
    public void getAllPermittedPagesFromAgentId_ShouldDelegateToSystemUserModuleService_WhenAgentIsUser() {
        setPermissionsAgent("USER");
        Set<String> permittedPages = permissionModuleService.getAllPermittedPagesFromAgentId(1002);
        assertEquals(1, permittedPages.size());
        assertEquals(true, permittedPages.contains("Module 2"));
    }

    @Test
    public void getPageOfPermissionModules_ShouldDelegateToRoleModuleService_WhenAgentIsRole() {
        setPermissionsAgent("Role");
        List<PermissionModule> modules = permissionModuleService.getPageOfPermissionModules(1);
        assertEquals(true, modules.size() > 0);
        assertEquals(true, modules.stream().allMatch(m -> m instanceof RoleModule));
    }

    @Test
    public void getPageOfPermissionModules_ShouldDelegateToSystemUserModuleService_WhenAgentIsUser() {
        setPermissionsAgent("USER");
        List<PermissionModule> modules = permissionModuleService.getPageOfPermissionModules(1);
        assertEquals(true, modules.size() > 0);
        assertEquals(true, modules.stream().allMatch(m -> m instanceof SystemUserModule));
    }

    @Test
    public void getTotalPermissionModuleCount_ShouldDelegateToRoleModuleService_WhenAgentIsRole() {
        setPermissionsAgent("Role");
        assertEquals(3, permissionModuleService.getTotalPermissionModuleCount().intValue());
    }

    @Test
    public void getTotalPermissionModuleCount_ShouldDelegateToSystemUserModuleService_WhenAgentIsUser() {
        setPermissionsAgent("USER");
        assertEquals(3, permissionModuleService.getTotalPermissionModuleCount().intValue());
    }

    @Test
    public void getAllPermissionModulesByAgentId_ShouldDelegateToRoleModuleService_WhenAgentIsRole() {
        setPermissionsAgent("Role");
        List<PermissionModule> modules = permissionModuleService.getAllPermissionModulesByAgentId(3001);
        assertEquals(2, modules.size());
        assertEquals(true, modules.stream().allMatch(m -> "3001".equals(((RoleModule) m).getRole().getId())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAllPermissionModulesByAgentId_currentlyThrowsBecausePropertyIsHardcodedToRoleId() {
        // NOTE: This test documents a known defect in
        // PermissionModuleServiceImpl.getAllPermissionModulesByAgentId.
        // The implementation delegates to
        // getActivePermissionModule().getAllMatching("role.id", agentId)
        // which is hardcoded to "role.id". For USER configuration (SystemUserModule),
        // there is no
        // "role" property (the field is "systemUser"), so Hibernate throws an
        // IllegalArgumentException.
        setPermissionsAgent("USER");
        permissionModuleService.getAllPermissionModulesByAgentId(1003);
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    public void getData_ShouldDelegateToRoleModuleService_WhenAgentIsRole() {
        setPermissionsAgent("Role");
        PermissionModule module = permissionModuleService.get("4001");
        permissionModuleService.getData(module);
        assertEquals("Module 1", module.getSystemModule().getSystemModuleName());
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    public void getData_ShouldDelegateToSystemUserModuleService_WhenAgentIsUser() {
        setPermissionsAgent("USER");
        PermissionModule module = permissionModuleService.get("2");
        permissionModuleService.getData(module);
        assertEquals("Module 2", module.getSystemModule().getSystemModuleName());
    }

    @Test
    public void get_ShouldReturnCorrectRecordForRole() {
        setPermissionsAgent("Role");
        PermissionModule module = permissionModuleService.get("4003");
        assertEquals("4003", module.getId());
        assertEquals("3002", ((RoleModule) module).getRole().getId());
        assertEquals("N", module.getHasSelect());
        assertEquals("Y", module.getHasAdd());
    }

    @Test
    public void get_ShouldReturnCorrectRecordForUser() {
        setPermissionsAgent("USER");
        PermissionModule module = permissionModuleService.get("3");
        assertEquals("3", module.getId());
        assertEquals("1003", ((SystemUserModule) module).getSystemUser().getId());
        assertEquals("N", module.getHasSelect());
        assertEquals("N", module.getHasAdd());
    }
}
