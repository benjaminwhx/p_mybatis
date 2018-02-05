package com.github.base.mapper;

import com.github.base.bean.MyPage;
import com.github.base.bean.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * User: 吴海旭
 * Date: 2017-06-24
 * Time: 下午1:01
 */
public interface UserMapper {

	/**
	 * 插入
	 * @param user
	 * @return
	 */
	int insert(User user);

	/**
	 * 更新
	 * @param user
	 * @return
	 */
	int update(User user);

	/**
	 * 删除
	 * @param id
	 * @return
	 */
	int delete(Long id);

	/**
	 * 通过id获取user
	 * @param uid
	 * @return
	 */
	User getUserByUserId(Long uid);

	void getUserByUserIdAndResultHandler(ResultHandler resultHandler);

	/**
	 * 使用@Param来定义参数名获取user
	 * @param userName
	 * @return
	 */
	List<User> getUsersByName(@Param("name") String userName);

	/**
	 * 获取所有user
	 * @return
	 */
	List<User> getAllUsers();

	/**
	 * 使用mybatis自带的RowBounds来分页（这个不推荐使用，因为是查询出所有的后在内存中进行分页）
	 * @param rowBounds
	 * @return
	 */
	List<User> getUserByRowBounds(RowBounds rowBounds);

	/**
	 * 使用自定义插件PagePlugin来分页获取user
	 * @return
	 */
	List<User> getUserByPage(MyPage<User> myPage);

	List<User> getUserByPageParam(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("countSql") boolean countSql);
}
