package org.openelisglobal.datasubmission.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.datasubmission.dao.DataValueDAO;
import org.openelisglobal.datasubmission.valueholder.DataValue;

@Service
public class DataValueServiceImpl extends BaseObjectServiceImpl<DataValue, String> implements DataValueService {
    @Autowired
    protected DataValueDAO baseObjectDAO;

    DataValueServiceImpl() {
        super(DataValue.class);
    }

    @Override
    protected DataValueDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

}
