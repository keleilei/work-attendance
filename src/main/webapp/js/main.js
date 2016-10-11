/**
 * Created by kelei on 2016/9/26.
 */
$(function(){
    getRecord();
});

function getRecord(){
    $.ajax({
        url : "rest/data/month",
        method : "get",
        dataType : "json"
    }).done(function (data) {

        console.log(data);

    }).fail(function (xhr, msg) {

        console.log(msg);

    });
}
