package com.github.spring.model;

import tk.mybatis.mapper.entity.IDynamicTableName;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;

@Table(name = "user_login")
public class UserLogin implements Serializable, IDynamicTableName {
    private static final long serialVersionUID = -445441931554119345L;

    @Id
    @GeneratedValue(generator = "JDBC")
//    @OrderBy("desc")
    private Long logid;

    private String username;

    private Date logindate;

    private String loginip;

    @Transient
    private String dynamicTableName;

    public Long getLogid() {
        return logid;
    }

    public void setLogid(Long logid) {
        this.logid = logid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public Date getLogindate() {
        return logindate;
    }

    public void setLogindate(Date logindate) {
        this.logindate = logindate;
    }

    public String getLoginip() {
        return loginip;
    }

    public void setLoginip(String loginip) {
        this.loginip = loginip == null ? null : loginip.trim();
    }

    @Override
    public String toString() {
        return "UserLogin{" +
                "logid=" + logid +
                ", username='" + username + '\'' +
                ", logindate=" + logindate +
                ", loginip='" + loginip + '\'' +
                '}';
    }

    @Override
    public String getDynamicTableName() {
        return dynamicTableName;
    }

    public void setDynamicTableName(String dynamicTableName) {
        this.dynamicTableName = dynamicTableName;
    }
}