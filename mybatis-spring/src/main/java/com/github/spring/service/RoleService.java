package com.github.spring.service;

import com.github.base.bean.Role;
import com.github.base.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: benjamin.wuhaixu
 * Date: 2017-12-28
 * Time: 2:17 pm
 */
@Service
public class RoleService {

    @Autowired
    private RoleMapper roleMapper;

    public Role getRole(Long id) {
        return roleMapper.findRoleById(id);
    }
}
