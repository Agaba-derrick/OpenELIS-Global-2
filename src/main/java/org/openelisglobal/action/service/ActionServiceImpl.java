package org.openelisglobal.action.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.openelisglobal.action.dao.ActionDAO;
import org.openelisglobal.action.valueholder.Action;
import org.openelisglobal.common.service.BaseObjectServiceImpl;

@Service
public class ActionServiceImpl extends BaseObjectServiceImpl<Action, String> implements ActionService {
    @Autowired
    protected ActionDAO baseObjectDAO;

    ActionServiceImpl() {
        super(Action.class);
    }

    @Override
    protected ActionDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }
}
