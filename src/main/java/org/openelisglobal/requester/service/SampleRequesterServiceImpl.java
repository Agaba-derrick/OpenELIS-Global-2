package org.openelisglobal.requester.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.requester.dao.SampleRequesterDAO;
import org.openelisglobal.requester.valueholder.SampleRequester;

@Service
public class SampleRequesterServiceImpl extends BaseObjectServiceImpl<SampleRequester, String>
        implements SampleRequesterService {
    @Autowired
    protected SampleRequesterDAO baseObjectDAO;

    SampleRequesterServiceImpl() {
        super(SampleRequester.class);
    }

    @Override
    protected SampleRequesterDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SampleRequester> getRequestersForSampleId(String id) {
        return baseObjectDAO.getRequestersForSampleId(id);
    }
}
