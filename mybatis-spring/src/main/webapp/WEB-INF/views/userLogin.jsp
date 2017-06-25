<%--
  Created by IntelliJ IDEA.
  User: wuhaixu
  Date: 2017/6/17
  Time: 22:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<html>
<head>
	<title>User</title>
</head>
<body>
<c:forEach items="${userList}" var="user">
	<div>欢迎：${user.username} 首次登录时间：${user.logindate} 登录ip：${user.loginip}</div> <br/>
</c:forEach>
</body>
</html>
