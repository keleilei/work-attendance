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
    var $tbody = $("#record-table").find("tbody");
    if(status == "1"){
        $tbody.find("td").html("从精友考勤网站获取数据失败，请刷新页面！");
    }else{
        var recordList = pageData.recordList;
        $("input[name='record-size']").val(recordList.length);
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

var date = new Date();
function addYearOption(){
    var option = "";
    for(var i = 4; i >= 0; i--){
        var year = date.getFullYear() - i;
        option += "<option " + (i == 0 ? "selected" : "") + " value='" + year + "'>" + year + "年</option>";
    }
    $("#year").html(option).dropdown({
        onChange: function(value, text, $selectedItem) {
            console.log(value + "     " + text);
            dropdownChangeEvent();
        }
    });
}

function addMonthOption(){
    var option = "";
    var month = date.getMonth() + 1;
    for(var i = 1; i < 13; i++){
        option += "<option " + (month == i ? "selected" : "") + " value='" + i + "'>" + i + "月</option>";
    }
    $("#month").html(option).dropdown({
        onChange: function(value, text, $selectedItem) {
            dropdownChangeEvent();
        }
    });
}

function dropdownChangeEvent(){

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
                var noDataTr = "<tr class='no-data'><td colspan='6'>没有匹配数据</td></tr>";
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
                    $tbody.append(noDataTr);
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
