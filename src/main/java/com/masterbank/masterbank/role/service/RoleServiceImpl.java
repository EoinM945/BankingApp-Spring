package com.masterbank.masterbank.role.service;

import com.masterbank.masterbank.exceptions.BadRequestException;
import com.masterbank.masterbank.exceptions.NotFoundException;
import com.masterbank.masterbank.response.Response;
import com.masterbank.masterbank.role.entity.Role;
import com.masterbank.masterbank.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Response<Role> createRole(Role roleRequest) {
        if (roleRepository.findByName(roleRequest.getName()).isPresent()) {
            throw new BadRequestException("Role with name " + roleRequest.getName() + " already exists");
        }
        Role savedRole = roleRepository.save(roleRequest);
        return Response.<Role>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role created successfully")
                .data(savedRole)
                .build();
    }

    @Override
    public Response<Role> updateRole(Role roleRequest) {
        Role savedRole = roleRepository.findById(roleRequest.getId())
                .orElseThrow(() -> new NotFoundException("Role with id " + roleRequest.getId() + " not found"));
        // Update the role properties
        savedRole.setName(roleRequest.getName());
        Role updatedRole = roleRepository.save(savedRole);
        return Response.<Role>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role updated successfully")
                .data(updatedRole)
                .build();
    }

    @Override
    public Response<?> deleteRole(Long roleId) {
        if (roleRepository.findById(roleId).isPresent()) {
            throw new NotFoundException("Role with id " + roleId + " not found");
        }
        roleRepository.deleteById(roleId);

        return Response.<Role>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Role deleted successfully")
                .build();
    }

    @Override
    public Response<List<Role>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return Response.<List<Role>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Roles retrieved successfully")
                .data(roles)
                .build();
    }
}
