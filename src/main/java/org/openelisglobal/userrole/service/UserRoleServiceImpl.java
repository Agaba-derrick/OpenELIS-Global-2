package org.openelisglobal.userrole.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.userrole.dao.UserRoleDAO;
import org.openelisglobal.userrole.valueholder.UserRole;
import org.openelisglobal.userrole.valueholder.UserRolePK;

@Service
public class UserRoleServiceImpl extends BaseObjectServiceImpl<UserRole, UserRolePK> implements UserRoleService {
    @Autowired
    protected UserRoleDAO baseObjectDAO;

    UserRoleServiceImpl() {
        super(UserRole.class);
        defaultSortOrder = new ArrayList<>();
    }

    @Override
    protected UserRoleDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getRoleIdsForUser(String userId) {
        return baseObjectDAO.getRoleIdsForUser(userId);
    }

    @Override
    @Transactional
    public boolean userInRole(String sysUserId, Collection<String> ableToCancelRoleNames) {
        return baseObjectDAO.userInRole(sysUserId, ableToCancelRoleNames);
    }

    @Override
    public boolean userInRole(String userId, String roleName) {
        return getBaseObjectDAO().userInRole(userId, roleName);
    }
}
