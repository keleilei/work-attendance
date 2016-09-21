package me.kelei.wa.entities;

/**
 * 用户实体
 * Created by kelei on 2016/9/21.
 */
public class WaUser {
    //姓名
    private String userName;

    //密码
    private String userPwd;

    //员工唯一码
    private String uid;

    //考勤号
    private String pid;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}
