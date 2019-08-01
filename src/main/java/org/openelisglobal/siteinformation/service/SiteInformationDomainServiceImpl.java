package org.openelisglobal.siteinformation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.siteinformation.dao.SiteInformationDomainDAO;
import org.openelisglobal.siteinformation.valueholder.SiteInformationDomain;

@Service
public class SiteInformationDomainServiceImpl extends BaseObjectServiceImpl<SiteInformationDomain, String>
        implements SiteInformationDomainService {
    @Autowired
    protected SiteInformationDomainDAO baseObjectDAO;

    SiteInformationDomainServiceImpl() {
        super(SiteInformationDomain.class);
    }

    @Override
    protected SiteInformationDomainDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public SiteInformationDomain getByName(String name) {
        return getMatch("name", name).orElse(null);
    }
}
