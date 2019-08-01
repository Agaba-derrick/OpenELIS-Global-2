package org.openelisglobal.county.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.county.dao.CountyDAO;
import org.openelisglobal.county.valueholder.County;

@Service
public class CountyServiceImpl extends BaseObjectServiceImpl<County, String> implements CountyService {
    @Autowired
    protected CountyDAO baseObjectDAO;

    CountyServiceImpl() {
        super(County.class);
    }

    @Override
    protected CountyDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }
}
