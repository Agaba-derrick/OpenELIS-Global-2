package org.openelisglobal.scheduler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.scheduler.dao.CronSchedulerDAO;
import org.openelisglobal.scheduler.valueholder.CronScheduler;

@Service
public class CronSchedulerServiceImpl extends BaseObjectServiceImpl<CronScheduler, String>
        implements CronSchedulerService {
    @Autowired
    protected CronSchedulerDAO baseObjectDAO;

    CronSchedulerServiceImpl() {
        super(CronScheduler.class);
    }

    @Override
    protected CronSchedulerDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public CronScheduler getCronScheduleByJobName(String jobName) {
        return getMatch("jobName", jobName).orElse(null);
    }

}
