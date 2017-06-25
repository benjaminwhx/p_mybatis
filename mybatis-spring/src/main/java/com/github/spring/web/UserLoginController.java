package com.github.spring.web;

import com.github.base.bean.User;
import com.github.spring.service.UserLoginService;
import com.github.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * User: 吴海旭
 * Date: 2017-06-17
 * Time: 下午10:19
 */
@Controller
@RequestMapping("/userLogin")
public class UserLoginController {

	@Autowired
	private UserLoginService userLoginService;

	@RequestMapping("/show")
	public ModelAndView getAllUsers(ModelAndView modelAndView) {
		modelAndView.setViewName("userLogin");
		modelAndView.addObject("userList", userLoginService.getAllUserLogins());
		return modelAndView;
	}

	@RequestMapping("/page/{pageNo}/{pageSize}")
	public ModelAndView getUserByPage(ModelAndView modelAndView, @PathVariable("pageNo") int pageNo,@PathVariable("pageSize") int pageSize) {
		modelAndView.setViewName("userLogin");
		modelAndView.addObject("userList", userLoginService.getUsersByPage(pageNo, pageSize));
		return modelAndView;
	}
}
