/**
 *
 * Created by kelei on 2016/9/26.
 */
$(function(){
    getUser();
    initTip();
    addYearOption();
    addMonthOption();
    addCheckbox();
    initData();
});

function getUser() {
    $.ajax({
        url : "rest/login/user?_=" + new Date().getTime(),
        method : "get",
        dataType : "json"
    }).done(function (user) {
        $(".user-name").text(user.waUserName);
    });
}

function initTip(){
    $(".wa-user-area .sign.icon").popup().click(function(){
        $.ajax({
            url : "rest/login/logout?_=" + new Date().getTime(),
            method : "get"
        }).done(function () {
            window.location.reload();
        });
    });

    $(".wa-user-area .share.icon").popup().click(function(){
        window.open("http://124.42.1.13:8000/iclock/accounts/login/");
    });

    $(".orange.icon").popup();

    $('#record-table').tablesort();
}

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
    var lateCount = 0, earlyCount = 0, forgetCount = 0, absenteeismCount = 0, overtimeCount = 0;
    var lateTip, earlyTip, forgetTip, absenteeismTip, overtimeTip;
    $tbody.find("tr:visible").each(function(){
        var state = $(this).find("input[name='wastate']").val();
        switch(state){
            case "2":
                var dateStr = $(this).find("td:first").text();
                dateStr = dateStr.replace(/-/g,"/");
                var date = new Date(dateStr);
                var hourPlusMin = date.getHours() + date.getMinutes();
                if(hourPlusMin < 40){
                    lateCount++;
                }
                break;
            case "3": earlyCount++;break;
            case "4": absenteeismCount++;break;
            case "5": overtimeCount++;break;
            case "6": forgetCount++;break;
        }
    });
    if(lateCount > 0){
        if(lateCount > 3)
            lateTip = "迟到<a class=\"ui mini red circular label\">" + lateCount + "</a>次，已经超过3次了，请补" + (lateCount - 3) + "个单子吧";
        else if(lateCount == 3)
            lateTip = "迟到<a class=\"ui mini red circular label\">" + lateCount + "</a>次，你已经没有迟到机会了";
        else
            lateTip = "迟到<a class=\"ui mini red circular label\">" + lateCount + "</a>次，剩下" + (3 - lateCount) + "次机会要省着点用";
        $(".list .late").html(lateTip);
    }else{
        $(".list .late").html("迟到有3条命");
    }
    if(earlyCount > 0){
        earlyTip = "早退<a class=\"ui mini red circular label\">" + earlyCount + "</a>次，月底前记得补单子";
        $(".list .early").html(earlyTip);
    }else{
        $(".list .early").html("对你的调休说bye bye");
    }
    if(forgetCount > 0){
        forgetTip = "忘打卡<a class=\"ui mini red circular label\">" + forgetCount + "</a>次，1次1小时，1小时1次";
        $(".list .forget").html(forgetTip);
    }else{
        $(".list .forget").html("没的说，调休1小时");
    }
    if(absenteeismCount > 0){
        absenteeismTip = "旷工<a class=\"ui mini red circular label\">" + absenteeismCount + "</a>天，不管是请假还是外出都得填单子";
        $(".list .absenteeism").html(absenteeismTip);
    }else{
        $(".list .absenteeism").html("外出？请假？");
    }
    if(overtimeCount > 0){
        overtimeTip = "加班<a class=\"ui mini green circular label\">" + (overtimeCount/2) + "</a>天，加了多少小时的班自己算";
        $(".list .overtime").html(overtimeTip);
    }else{
        $(".list .overtime").html("又可以愉快的请请请了");
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