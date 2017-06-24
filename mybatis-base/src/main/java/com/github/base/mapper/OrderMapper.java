package com.github.base.mapper;

import com.github.base.bean.Order;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;

/**
 * User: 吴海旭
 * Date: 2017-06-24
 * Time: 下午2:10
 */
public interface OrderMapper {

	@Results(id = "orderResult", value = {
			@Result(property = "orderId", column = "order_id"),
			@Result(property = "orderName", column = "order_name")
	})
	@SelectProvider(type = selectSqlBuilder.class, method = "getOrdersByIds")
	List<Order> findOrderByIds(@Param("ids") List<String> ids);

	class selectSqlBuilder {
		public String getOrdersByIds(Map<String, Object> parameters) {
			final List<String> ids = (List<String>) parameters.get("ids");
			return new SQL(){{
				SELECT("*");
				FROM("cf_order");
				if (ids != null && ids.size() > 0) {
					StringBuilder idIn = new StringBuilder();
					for (String id : ids) {
						idIn.append(id).append(",");
					}
					String idStr = idIn.substring(0, idIn.length() - 1);
					WHERE("id in (" + idStr + ")");
				}
			}}.toString();
		}
	}

	@Results(id = "orderResult2", value = {
			@Result(property = "orderId", column = "order_id"),
			@Result(property = "orderName", column = "order_name"),
//			@Result(property = "user", column = "uid",
//					one = @One(
//							select = "com.jd.jr.bt.mybatis.mapper.xml.UserMapper.getUserByUserId", fetchType = FetchType.LAZY
//					))
	})
	@Select("select * from cf_order a where a.uid = #{uid}")
	List<Order> getOrdersByUid(@Param("uid") Long uid);
}
