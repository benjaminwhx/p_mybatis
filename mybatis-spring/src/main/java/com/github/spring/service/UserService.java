package com.github.spring.service;

import com.github.base.bean.User;
import com.github.base.mapper.UserMapper;
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
public class UserService {

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private PlatformTransactionManager transactionManager;

	public List<User> getUserList() {
		return userMapper.getUserByRowBounds(new RowBounds(0, 10));
	}

	public boolean addUser(User user) {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

		TransactionStatus status = transactionManager.getTransaction(def);
		int result;
		try {
			result = userMapper.insert(user);
		}
		catch (Exception ex) {
			transactionManager.rollback(status);
			return false;
		}
		transactionManager.commit(status);
		return result == 1;
	}
}
