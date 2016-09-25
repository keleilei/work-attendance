package me.kelei.wa.entities;

/**
 * 考勤用户
 * Created by kelei on 2016/9/21.
 */
public class WaUser {
    //姓名
    private String waUserName;

    //密码
    private String waUserPwd;

    //员工唯一码
    private String waUid;

    //考勤号
    private String waPid;

    public String getWaUserName() {
        return waUserName;
    }

    public void setWaUserName(String waUserName) {
        this.waUserName = waUserName;
    }

    public String getWaUserPwd() {
        return waUserPwd;
    }

    public void setWaUserPwd(String waUserPwd) {
        this.waUserPwd = waUserPwd;
    }

    public String getWaUid() {
        return waUid;
    }

    public void setWaUid(String waUid) {
        this.waUid = waUid;
    }

    public String getWaPid() {
        return waPid;
    }

    public void setWaPid(String waPid) {
        this.waPid = waPid;
    }
}
