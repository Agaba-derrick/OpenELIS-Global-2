package org.openelisglobal.samplehuman.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.patient.valueholder.Patient;
import org.openelisglobal.provider.valueholder.Provider;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.samplehuman.dao.SampleHumanDAO;
import org.openelisglobal.samplehuman.valueholder.SampleHuman;

@Service
public class SampleHumanServiceImpl extends BaseObjectServiceImpl<SampleHuman, String> implements SampleHumanService {
    @Autowired
    protected SampleHumanDAO baseObjectDAO;

    SampleHumanServiceImpl() {
        super(SampleHuman.class);
    }

    @Override
    protected SampleHumanDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public SampleHuman getDataBySample(SampleHuman sampleHuman) {
        return getMatch("sampleId", sampleHuman.getSampleId()).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Patient getPatientForSample(Sample sample) {
        return baseObjectDAO.getPatientForSample(sample);
    }

    @Override
    @Transactional(readOnly = true)
    public void getData(SampleHuman sampleHuman) {
        getBaseObjectDAO().getData(sampleHuman);

    }

    @Override
    @Transactional(readOnly = true)
    public Provider getProviderForSample(Sample sample) {
        return getBaseObjectDAO().getProviderForSample(sample);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sample> getSamplesForPatient(String patientID) {
        return getBaseObjectDAO().getSamplesForPatient(patientID);
    }

}
