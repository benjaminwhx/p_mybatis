package com.github.spring.service;

import com.github.base.bean.User;
import com.github.base.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.spring.mapper.UserLoginMapper;
import com.github.spring.model.UserLogin;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

/**
 * User: 吴海旭
 * Date: 2017-06-18
 * Time: 下午5:29
 */
@Service
public class UserLoginService {

	@Autowired
	private UserLoginMapper userLoginMapper;

	public List<UserLogin> getAllUserLogins() {
		return userLoginMapper.selectAll();
	}

	public List<UserLogin> getUsersByPage(int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize, false);
	    return userLoginMapper.selectAll();
    }
}
