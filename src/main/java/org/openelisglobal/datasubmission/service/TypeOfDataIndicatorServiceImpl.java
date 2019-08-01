package org.openelisglobal.datasubmission.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.datasubmission.dao.TypeOfDataIndicatorDAO;
import org.openelisglobal.datasubmission.valueholder.TypeOfDataIndicator;

@Service
public class TypeOfDataIndicatorServiceImpl extends BaseObjectServiceImpl<TypeOfDataIndicator, String>
        implements TypeOfDataIndicatorService {
    @Autowired
    protected TypeOfDataIndicatorDAO baseObjectDAO;

    TypeOfDataIndicatorServiceImpl() {
        super(TypeOfDataIndicator.class);
    }

    @Override
    protected TypeOfDataIndicatorDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public void getData(TypeOfDataIndicator typeOfIndicator) {
        getBaseObjectDAO().getData(typeOfIndicator);

    }

    @Override
    @Transactional(readOnly = true)
    public TypeOfDataIndicator getTypeOfDataIndicator(String id) {
        return getBaseObjectDAO().getTypeOfDataIndicator(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TypeOfDataIndicator> getAllTypeOfDataIndicator() {
        return getBaseObjectDAO().getAllTypeOfDataIndicator();
    }
}
