package com.github.base.mapper;

import com.github.base.bean.Student;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

/**
 * User: 吴海旭
 * Date: 2017-06-28
 * Time: 下午6:12
 */
public interface StudentMapper {

	@Insert("insert into student(id, name) values(#{id}, #{name})")
	int insert(Student student);

	@Delete("delete from student")
	int deleteAll();
}
