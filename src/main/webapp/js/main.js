/**
 *
 * Created by kelei on 2016/9/26.
 */
$(function(){
    addYearOption();
    addMonthOption();
    addCheckbox();
    initRecordTable();
});

function initRecordTable(){
    $.ajax({
        url : "rest/data/month",
        method : "get",
        dataType : "json"
    }).done(function (pageData) {
        fillTable(pageData);
    }).fail(function (xhr, msg) {
        console.log(msg);
    });
}

function fillTable(pageData){
    var status = pageData.status;
    var $tbody = $("#recordTable").find("tbody");
    if(status == "1"){
        $tbody.find("td").html("从精友考勤网站获取数据失败，请刷新页面！");
    }else{
        var recordList = pageData.recordList;
        if(!recordList || recordList.length == 0){
            $tbody.find("td").html("未查找到考勤记录！");
        }else{
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
                html += "<td>" + getStateName(recordList[i].waState) + "</td>";
                html += "</tr>";
            }
            $tbody.html(html);
        }
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

function addYearOption(){
    var date = new Date();
    var option = "";
    for(var i = 4; i >= 0; i--){
        var year = date.getFullYear() - i;
        option += "<option value='" + year + "'>" + year + "年</option>";
    }
    $("#year").html(option).dropdown();
}

function addMonthOption(){
    var option = "";
    for(var i = 1; i < 13; i++){
        option += "<option value='" + i + "'>" + i + "月</option>";
    }
    $("#month").html(option).dropdown();
}

function addCheckbox(){
    $(".ui.checkbox").checkbox();
}
