package com.github.base.mapper;

import com.github.base.bean.Role;
import org.apache.ibatis.annotations.Param;

/**
 * User: 吴海旭
 * Date: 2017-06-24
 * Time: 下午2:05
 */
public interface RoleMapper {

	Role findRoleById(@Param("id") Long id);
}
