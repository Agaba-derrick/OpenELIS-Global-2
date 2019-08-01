package org.openelisglobal.dataexchange.orderresult.dao;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.openelisglobal.common.daoimpl.BaseDAOImpl;
import org.openelisglobal.dataexchange.orderresult.valueholder.HL7MessageOut;

@Component
@Transactional
public class HL7MessageOutDAOImpl extends BaseDAOImpl<HL7MessageOut, String> implements HL7MessageOutDAO {
    public HL7MessageOutDAOImpl() {
        super(HL7MessageOut.class);
    }
}
