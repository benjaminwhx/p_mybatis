package com.github.spring.web;

import com.github.base.bean.Role;
import com.github.base.bean.User;
import com.github.spring.service.RoleService;
import com.github.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * User: 吴海旭
 * Date: 2017-06-17
 * Time: 下午10:19
 */
@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private RoleService roleService;

	@RequestMapping("/show")
	public ModelAndView getUserByPage(ModelAndView modelAndView) {
		modelAndView.setViewName("user");
		modelAndView.addObject("userList", userService.getUserList());
		Role role = roleService.getRole(1L);
		modelAndView.addObject("role", role);
		return modelAndView;
	}

	@RequestMapping("/add/{userName}")
	@ResponseBody
	public String add(@PathVariable("userName") String userName) {
		User user = new User();
		user.setUserName(userName);
		boolean addResult = userService.addUser(user);
		if (addResult) {
			return "新增成功";
		} else {
			return "新增失败";
		}
	}
}
