/**
 *
 * Created by kelei on 2016/9/26.
 */
$(function(){
    addYearOption();
    addMonthOption();
    addCheckbox();
    initData();
});

var isComplete = false;
var intervalNum;
function initData(){
    updateData();
    intervalNum = setInterval("prepareLoadData()", 100);
}

function updateData(){
    $.ajax({
        url : "rest/data/update?_=" + new Date().getTime(),
        method : "get",
        dataType : "json"
    }).done(function (pageData) {
        console.log(pageData);
        isComplete = true;
    }).fail(function (xhr, msg) {
        console.log(msg);
        isComplete = true;
    });
}

function prepareLoadData(){
    if(isComplete){
        clearInterval(intervalNum);
        loadData();
    }
}

function loadData(qdate){
    $.ajax({
        url : "rest/data/month?qdate=" + (qdate ? qdate : "") + "&_=" + new Date().getTime(),
        method : "get",
        dataType : "json"
    }).done(function (pageData) {
        fillHoliday(pageData);
        fillTable(pageData);
        fillAanalysis();
    }).fail(function (xhr, msg) {
        console.log(msg);
    });
}

function fillTable(pageData){
    var status = pageData.status;
    var $tbody = $("#record-table").find("tbody");
    if(status == "1"){
        $tbody.html("").append(tableTipTemplate("从精友考勤网站获取数据失败，请刷新页面！"));
    }else if(status == "2"){
        $tbody.html("").append(tableTipTemplate("未查找到考勤记录！"));
    }else{
        var recordList = pageData.recordList;
        $("input[name='record-size']").val(recordList.length);
        var html = "";
        for(var i = 0; i < recordList.length; i++){
            var waState = recordList[i].waState;
            var cssclass = "";
            if(waState == "0")
                cssclass = "hidden";
            else if(waState == "5")
                cssclass = "positive";
            else if(waState != "1")
                cssclass = "negative";
            html += "<tr class='" + cssclass + "'>";
            html += "<td>" + parseDefaultValue(recordList[i].waDate) + "</td>";
            html += "<td>" + parseDefaultValue(recordList[i].waWeek) + "</td>";
            html += "<td>" + parseDefaultValue(recordList[i].waType) + "</td>";
            html += "<td>" + parseDefaultValue(recordList[i].waValidateWay) + "</td>";
            html += "<td>" + parseDefaultValue(recordList[i].waDevice) + "</td>";
            html += "<td><input type='hidden' name='wastate' value='"+recordList[i].waState+"'>"
                + getStateName(recordList[i].waState) + "</td>";
            html += "</tr>";
        }
        $tbody.html(html);
    }
}

function fillHoliday(pageData){
    $(".wa-holiday .list").html("");
    var holidayList = pageData.holidayList;
    if(holidayList && holidayList.length > 0){
        for(var i = 0; i < holidayList.length; i++){
            $(".wa-holiday .list").append(holidayTemplate(holidayList[i].holidayName, holidayList[i].holidayDesc));
        }
    }
}

function fillAanalysis(){
    var $tbody = $("#record-table").find("tbody");
    var lateCount, earlyCount, forgetCount, absenteeismCount, overtimeCount;
    var lateTip, earlyTip, forgetTip, absenteeismTip, overtimeTip;
    $tbody.find("tr:visible").each(function(){
        var state = $(this).find("input[name='wastate']").val();
        switch(state){
            case "2": lateCount++;break;
            case "3": earlyCount++;break;
            case "4": absenteeismCount++;break;
            case "5": overtimeCount++;break;
            case "6": forgetCount++;break;
        }
    });
    if(lateCount){
        if(lateCount > 3)
            lateTip = "迟到" + lateCount + "次，已经超过3次了，请补" + (lateCount - 3) + "个单子吧！";
        else
            lateTip = "迟到" + lateCount + "次，剩下" + (3 - lateCount) + "次机会要省着点用！";
        $(".list .late").html(lateTip);
    }
    if(earlyCount){
        earlyTip = "早退" + earlyCount + "次，月底前记得补单子。";
        $(".list .early").html(earlyTip);
    }
    if(forgetCount){
        forgetTip = "忘打卡" + forgetCount + "次，1次1小时，1小时1次。";
        $(".list .forget").html(forgetTip);
    }
    if(absenteeismCount){
        absenteeismTip = "旷工" + absenteeismCount + "天，1次1小时，1小时1次。";
        $(".list .absenteeism").html(absenteeismTip);
    }
    if(overtimeCount){
        overtimeTip = "加班" + (overtimeCount/2) + "天，自己算加了多少小时的班。";
        $(".list .overtime").html(overtimeTip);
    }

}

function parseDefaultValue(value){
    if(!value){
        value = "（空）";
    }
    return value;
}

function getStateName(state){
    switch(state){
        case "0": return "正常";
        case "1": return "正常";
        case "2": return "迟到";
        case "3": return "早退";
        case "4": return "旷工";
        case "5": return "加班";
        case "6": return "忘打卡";
    }
}

var date = new Date();
function addYearOption(){
    var option = "";
    for(var i = 4; i >= 0; i--){
        var year = date.getFullYear() - i;
        option += "<option " + (i == 0 ? "selected" : "") + " value='" + year + "'>" + year + "年</option>";
    }
    $("#year").html(option).dropdown({
        onChange: function(value, text, $selectedItem) {
            dropdownChangeEvent(value, 1);
        }
    });
}

function addMonthOption(){
    var option = "";
    var month = date.getMonth() + 1;
    for(var i = 1; i < 13; i++){
        var formatMonth = i < 10 ? ("0"+i) : i;
        option += "<option " + (month == i ? "selected" : "") + " value='" + formatMonth + "'>" + formatMonth + "月</option>";
    }
    $("#month").html(option).dropdown({
        onChange: function(value, text, $selectedItem) {
            dropdownChangeEvent(value, 2);
        }
    });
}

function dropdownChangeEvent(value, flag){
    var year,month;
    if(flag == 1){
        year = value;
        month = $("#month").closest(".dropdown").find(".selected").attr("data-value");
    }else{
        year = $("#year").closest(".dropdown").find(".selected").attr("data-value");
        month = value;
    }
    $(".ui.checkbox").checkbox("set unchecked");
    loadData(year + "-" + month);
}

function addCheckbox(){
    $(".ui.checkbox").checkbox({
        onChecked:function(){
            var $checkbox = $(this).closest('.checkbox');
            var id = $checkbox.prop("id");
            var recordSize = $("input[name='record-size']").val();
            $(".ui.checkbox").checkbox("set unchecked");
            $checkbox.checkbox("set checked");
            if(recordSize > 0){
                var $tbody = $("#record-table > tbody");
                if(id == "all"){
                    $tbody.find("tr").show();
                }else if(id == "exception"){
                    $tbody.find("tr[class='negative']").show();
                    $tbody.find("tr[class!='negative']").hide();
                }else{
                    $tbody.find("tr[class='positive']").show();
                    $tbody.find("tr[class!='positive']").hide();
                }
                if($tbody.find("tr:visible").length == 0)
                    $tbody.append(tableTipTemplate("没有匹配数据"));
                else
                    $tbody.find("tr[class='no-data']").remove();
            }
        },
        onUnchecked: function() {
            var $tbody = $("#record-table > tbody");
            $tbody.find("tr[class!='hidden']").show();
            $tbody.find("tr[class='hidden']").hide();
            $tbody.find("tr[class='no-data']").remove();
        }
    });
}

function holidayTemplate(holidayName, holidayDesc){
    return "<div class=\"item\">"+
    "<i class=\"big calendar middle aligned icon\"></i>"+
    "<div class=\"content\">"+
    "<a class=\"header\">"+holidayName+"</a>"+
    "<div class=\"description\">" + holidayDesc + "</div>"+
    "</div>"+
    "</div>";
}

function tableTipTemplate(tip){
    return "<tr class='no-data'><td colspan='6'>" + tip + "</td></tr>";
}