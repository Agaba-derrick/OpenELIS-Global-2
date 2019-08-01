package org.openelisglobal.qaevent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.qaevent.dao.QaObservationDAO;
import org.openelisglobal.qaevent.valueholder.QaObservation;

@Service
public class QaObservationServiceImpl extends BaseObjectServiceImpl<QaObservation, String>
        implements QaObservationService {
    @Autowired
    protected QaObservationDAO baseObjectDAO;

    QaObservationServiceImpl() {
        super(QaObservation.class);
    }

    @Override
    protected QaObservationDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public QaObservation getQaObservationByTypeAndObserved(String typeName, String observedType, String observedId) {
        return getBaseObjectDAO().getQaObservationByTypeAndObserved(typeName, observedType, observedId);
    }
}
