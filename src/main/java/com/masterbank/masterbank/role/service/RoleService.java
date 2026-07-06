package com.masterbank.masterbank.role.service;


import com.masterbank.masterbank.response.Response;
import com.masterbank.masterbank.role.entity.Role;

import java.util.List;

public interface RoleService {

    Response<Role> createRole(Role roleRequest);
    Response<Role> updateRole(Role roleRequest);
    Response<?> deleteRole(Long roleId);
    Response<List<Role>> getAllRoles();
}
