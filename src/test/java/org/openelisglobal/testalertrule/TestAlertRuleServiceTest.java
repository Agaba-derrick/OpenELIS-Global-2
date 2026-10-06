package org.openelisglobal.testalertrule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openelisglobal.BaseWebContextSensitiveTest;
import org.openelisglobal.testalertrule.service.TestAlertRuleService;
import org.openelisglobal.testalertrule.valueholder.TestAlertRule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for {@link TestAlertRuleService}.
 *
 * <p>
 * Covers: CRUD operations, per-test lookup ({@code getByTestId}), and key field
 * semantics (trigger type, notification channels, acknowledgment flag).
 *
 * <p>
 * Fixture: {@code testdata/test_alert_rule.xml}
 * <ul>
 * <li>rule-001 — ALL trigger, test_id=1 (CBC), email+SMS, enabled</li>
 * <li>rule-002 — CRITICAL trigger, test_id=1 (CBC), email+physician, ack
 * required</li>
 * <li>rule-003 — ABNORMAL trigger, test_id=2 (UA), disabled</li>
 * <li>rule-004 — SPECIFIC_VALUE trigger, test_id=1 (CBC), custom contacts, ack
 * required</li>
 * </ul>
 */
public class TestAlertRuleServiceTest extends BaseWebContextSensitiveTest {

    @Autowired
    private TestAlertRuleService testAlertRuleService;

    @Before
    public void setUp() throws Exception {
        executeDataSetWithStateManagement("testdata/test_alert_rule.xml");
    }

    // ─── getByTestId ──────────────────────────────────────────────────────────

    @Test
    public void getByTestId_withValidTestId_shouldReturnRulesForThatTest() {
        // test_id=1 (CBC) has 3 rules: rule-001, rule-002, rule-004
        List<TestAlertRule> rules = testAlertRuleService.getByTestId("1");

        assertNotNull(rules);
        assertEquals(3, rules.size());
        rules.forEach(r -> assertEquals("1", r.getTestId()));
    }

    @Test
    public void getByTestId_withValidTestId_shouldReturnCorrectRuleForUrinalysis() {
        // test_id=2 (UA) has 1 rule: rule-003
        List<TestAlertRule> rules = testAlertRuleService.getByTestId("2");

        assertNotNull(rules);
        assertEquals(1, rules.size());
        assertEquals("UA Abnormal Alert", rules.get(0).getName());
    }

    @Test
    public void getByTestId_withUnknownTestId_shouldReturnEmptyList() {
        List<TestAlertRule> rules = testAlertRuleService.getByTestId("99999");

        assertNotNull(rules);
        assertTrue(rules.isEmpty());
    }

    // ─── get (by primary key) ─────────────────────────────────────────────────

    @Test
    public void get_withKnownId_shouldReturnCorrectRule() {
        TestAlertRule rule = testAlertRuleService.get("rule-001");

        assertNotNull(rule);
        assertEquals("rule-001", rule.getId());
        assertEquals("CBC All-Results Alert", rule.getName());
        assertEquals("ALL", rule.getTriggerType());
        assertEquals("1", rule.getTestId());
    }

    @Test
    public void get_withUnknownId_shouldReturnNull() {
        TestAlertRule rule = testAlertRuleService.get("non-existent-id");
        assertNull(rule);
    }

    // ─── notification channel flags ───────────────────────────────────────────

    @Test
    public void get_allTriggerRule_shouldHaveEmailAndSmsEnabled() {
        TestAlertRule rule = testAlertRuleService.get("rule-001");

        assertTrue("Expected notifyEmail=true", rule.getNotifyEmail());
        assertTrue("Expected notifySms=true", rule.getNotifySms());
        assertFalse("Expected notifyOrderingPhysician=false", rule.getNotifyOrderingPhysician());
    }

    @Test
    public void get_criticalTriggerRule_shouldHavePhysicianNotificationAndAckRequired() {
        TestAlertRule rule = testAlertRuleService.get("rule-002");

        assertEquals("CRITICAL", rule.getTriggerType());
        assertTrue("Expected notifyOrderingPhysician=true", rule.getNotifyOrderingPhysician());
        assertTrue("Expected acknowledgmentRequired=true", rule.getAcknowledgmentRequired());
    }

    @Test
    public void get_specificValueRule_shouldHaveCustomContactsPopulated() {
        TestAlertRule rule = testAlertRuleService.get("rule-004");

        assertEquals("SPECIFIC_VALUE", rule.getTriggerType());
        assertEquals("+1234567890", rule.getNotifyCustomPhone());
        assertEquals("lab-alerts@hospital.org", rule.getNotifyCustomEmail());
        assertTrue("Expected acknowledgmentRequired=true", rule.getAcknowledgmentRequired());
    }

    // ─── enabled flag ─────────────────────────────────────────────────────────

    @Test
    public void get_disabledRule_shouldReturnEnabledFalse() {
        TestAlertRule rule = testAlertRuleService.get("rule-003");

        assertNotNull(rule);
        assertFalse("Expected is_enabled=false for rule-003", rule.getEnabled());
    }

    // ─── insert ───────────────────────────────────────────────────────────────

    @Test
    public void insert_newRule_shouldPersistAndBeRetrievable() {
        TestAlertRule newRule = new TestAlertRule();
        newRule.setTestId("1");
        newRule.setName("New Compliance Alert");
        newRule.setTriggerType("COMPLIANCE_BREACH");
        newRule.setEnabled(true);
        newRule.setNotifyEmail(true);
        newRule.setNotifySms(false);
        newRule.setNotifyOrderingPhysician(false);
        newRule.setNotifyPatient(false);
        newRule.setNotifyReferringFacility(false);
        newRule.setAcknowledgmentRequired(false);

        String insertedId = testAlertRuleService.insert(newRule);

        assertNotNull(insertedId);
        TestAlertRule fetched = testAlertRuleService.get(insertedId);
        assertNotNull(fetched);
        assertEquals("New Compliance Alert", fetched.getName());
        assertEquals("COMPLIANCE_BREACH", fetched.getTriggerType());
        assertEquals("1", fetched.getTestId());
        assertTrue(fetched.getEnabled());
        assertTrue(fetched.getNotifyEmail());
    }

    @Test
    public void insert_newRule_shouldAppearInGetByTestId() {
        int before = testAlertRuleService.getByTestId("2").size();

        TestAlertRule extraRule = new TestAlertRule();
        extraRule.setTestId("2");
        extraRule.setName("UA Extra Alert");
        extraRule.setTriggerType("ALL");
        extraRule.setEnabled(true);
        extraRule.setNotifyEmail(false);
        extraRule.setNotifySms(false);
        extraRule.setNotifyOrderingPhysician(false);
        extraRule.setNotifyPatient(false);
        extraRule.setNotifyReferringFacility(false);
        extraRule.setAcknowledgmentRequired(false);
        testAlertRuleService.insert(extraRule);

        List<TestAlertRule> after = testAlertRuleService.getByTestId("2");
        assertEquals(before + 1, after.size());
    }

    // ─── update ───────────────────────────────────────────────────────────────

    @Test
    public void update_existingRule_shouldPersistChanges() {
        TestAlertRule rule = testAlertRuleService.get("rule-003");
        assertFalse("Pre-condition: rule-003 should be disabled", rule.getEnabled());

        rule.setEnabled(true);
        rule.setNotifySms(true);
        testAlertRuleService.update(rule);

        TestAlertRule updated = testAlertRuleService.get("rule-003");
        assertTrue("Expected enabled=true after update", updated.getEnabled());
        assertTrue("Expected notifySms=true after update", updated.getNotifySms());
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    public void delete_existingRule_shouldRemoveItFromDatabase() {
        // Confirm rule exists first
        TestAlertRule rule = testAlertRuleService.get("rule-003");
        assertNotNull("Pre-condition: rule-003 should exist", rule);

        testAlertRuleService.delete(rule);

        TestAlertRule afterDelete = testAlertRuleService.get("rule-003");
        assertNull("Expected rule-003 to be null after deletion", afterDelete);
    }

    @Test
    public void delete_existingRule_shouldNotAffectRulesForOtherTests() {
        TestAlertRule rule = testAlertRuleService.get("rule-003");
        testAlertRuleService.delete(rule);

        // CBC rules (test_id=1) must remain untouched
        List<TestAlertRule> cbcRules = testAlertRuleService.getByTestId("1");
        assertEquals(3, cbcRules.size());
    }

    // ─── getAll ───────────────────────────────────────────────────────────────

    @Test
    public void getAll_shouldReturnAllFourFixtureRules() {
        List<TestAlertRule> all = testAlertRuleService.getAll();

        assertNotNull(all);
        assertEquals(4, all.size());
    }
}
