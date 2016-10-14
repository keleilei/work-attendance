package me.kelei.wa.utils;

/**
 * 字典信息
 * Created by kelei on 2016/10/13.
 */
public interface WaDict {

    //考勤记录状态
    String RECORD_STATE_INVALID = "0";
    String RECORD_STATE_NORMAL = "1";
    String RECORD_STATE_LATE = "2";
    String RECORD_STATE_EARLY = "3";
    String RECORD_STATE_ABSENTEEISM = "4";
    String RECORD_STATE_OVERTIME = "5";
    String RECORD_STATE_FORGET = "6";

    //请求数据状态
    String REQUEST_STATE_SUCCESS = "0";
    String REQUEST_STATE_FAIL = "1";

}
