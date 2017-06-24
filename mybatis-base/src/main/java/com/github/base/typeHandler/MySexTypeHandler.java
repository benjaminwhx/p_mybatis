package com.github.base.typeHandler;

import com.github.base.bean.SEX;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User: 吴海旭
 * Date: 2017-06-13
 * Time: 下午7:53
 */
public class MySexTypeHandler implements TypeHandler<SEX> {
	@Override
	public void setParameter(PreparedStatement ps, int i, SEX sex, JdbcType jdbcType) throws SQLException {
		if (sex == null) {
			ps.setInt(i, SEX.MALE.getCode());
		} else {
			ps.setInt(i, sex.getCode());
		}
	}

	@Override
	public SEX getResult(ResultSet rs, String s) throws SQLException {
		int code = rs.getInt(s);
		return SEX.fromCode(code);
	}

	@Override
	public SEX getResult(ResultSet rs, int i) throws SQLException {
		int code = rs.getInt(i);
		return SEX.fromCode(code);
	}

	@Override
	public SEX getResult(CallableStatement cs, int i) throws SQLException {
		int code = cs.getInt(i);
		return SEX.fromCode(code);
	}
}
