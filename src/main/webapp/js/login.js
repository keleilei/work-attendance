$(function(){
    $(".ui.message").hide();
    $(".ui.checkbox").checkbox();

    $("#login-btn").click(function () {

        if(!validateFields()){
            tada(LOGIN_MESSAGE_NULLABLE);
            return;
        }

        var formData = $("form:eq(0)").serialize();
        $(".ui.dimmer").dimmer({closable: false}).dimmer("show");
        $.ajax({
            url : "rest/login/validate",
            data : formData,
            method : "post",
            dataType : "json"
        }).done(function (data) {
            console.log(data);
            if(data.isValidate){
                addAutoLogin();
            }else{
                removeAutoLogin();
                tada(LOGIN_MESSAGE_USER_ERROR);
            }
        }).fail(function () {
            removeAutoLogin();
        });

    });
});

function validateFields(){
    var $wapid = $("input[name='wapid']");
    var $wapwd = $("input[name='wapwd']");
    var flag = true;
    if(!$wapid.val()){
        flag = false;
        $wapid.closest(".field").addClass("error");
    }else{
        $wapid.closest(".field").removeClass("error");
    }
    if(!$wapwd.val()){
        flag = false;
        $wapwd.closest(".field").addClass("error");
    }else{
        $wapwd.closest(".field").removeClass("error");
    }
    return flag;
}

function removeAutoLogin(){
    $.removeCookie("wapid");
    $.removeCookie("rememberme");
}

function addAutoLogin(){
    var wapid = $("input[name='wapid']").val();
    var rememberme = $("input[name='rememberme']").val();
    if(rememberme == "on"){
        $.cookie("wapid", wapid,{expires: 365});
        $.cookie("rememberme", rememberme,{expires: 365});
    }
}

function tada(message){
    $(".ui.dimmer").dimmer("hide");
    $(".ui.segment").transition('tada');
    $(".ui.message").show().find("p").text(message);
}














var LOGIN_MESSAGE_NULLABLE = "请输入考勤号和密码！";
var LOGIN_MESSAGE_USER_ERROR = "考勤号或密码有误，请重新输入！";
var LOGIN_MESSAGE_NETWORK_ERROR = "网络问题，请重新登录！";