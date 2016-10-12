/**
 *
 * Created by kelei on 2016/9/26.
 */
$(function(){
    initRecordTable();
});

function initRecordTable(){
    $.ajax({
        url : "rest/data/month",
        method : "get",
        dataType : "json"
    }).done(function (recordList) {
        fillTable(recordList);
    }).fail(function (xhr, msg) {
        console.log(msg);
    });
}

function fillTable(recordList){
    var $tbody = $("#recordTable").find("tbody");
    if(!recordList || recordList.length == 0){
        $tbody.find("td").html("未查找到考勤记录");
    }else{
        var html = "";
        for(var i = 0; i < recordList.length; i++){
            html += "<tr>";
            html += "<td>" + recordList[i].waDate + "</td>";
            html += "<td>" + recordList[i].waWeek + "</td>";
            html += "<td>" + recordList[i].waType + "</td>";
            html += "<td>" + recordList[i].waValidateWay + "</td>";
            html += "<td>" + recordList[i].waDevice + "</td>";
            html += "<td>" + recordList[i].waState + "</td>";
            html += "</tr>";
        }
        $tbody.html(html);
    }
}
