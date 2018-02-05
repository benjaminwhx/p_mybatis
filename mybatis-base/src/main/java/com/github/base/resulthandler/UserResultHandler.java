package com.github.base.resulthandler;

import com.github.base.bean.User;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

/**
 * User: benjamin.wuhaixu
 * Date: 2018-02-05
 * Time: 5:29 pm
 */
public class UserResultHandler implements ResultHandler<User> {
    @Override
    public void handleResult(ResultContext<? extends User> resultContext) {
        System.out.println(resultContext.getResultObject());
        System.out.println(resultContext.getResultCount());
    }
}
