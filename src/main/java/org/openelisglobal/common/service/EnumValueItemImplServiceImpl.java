package org.openelisglobal.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.openelisglobal.common.dao.EnumValueItemImplDAO;
import org.openelisglobal.common.valueholder.EnumValueItemImpl;

@Service
public class EnumValueItemImplServiceImpl extends BaseObjectServiceImpl<EnumValueItemImpl, String>
        implements EnumValueItemImplService {
    @Autowired
    protected EnumValueItemImplDAO baseObjectDAO;

    EnumValueItemImplServiceImpl() {
        super(EnumValueItemImpl.class);
    }

    @Override
    protected EnumValueItemImplDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }
}
