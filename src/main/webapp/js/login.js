$(function(){
    $(".ui.checkbox").checkbox();

    $("#login-btn").click(function () {
        var formData = $("form:eq(0)").serialize();
        console.log(formData);
        $(".ui.dimmer").dimmer({closable: false}).dimmer("show");
        $.ajax({
            url : "rest/login/validate",
            data : formData,
            method : "post",
            dataType : "json"
        }).done(function (data) {
            console.log(data);
            if(data.isValidate){

            }else
                $(".ui.dimmer").dimmer("hide");
        }).fail(function () {
            $(".ui.dimmer").dimmer("hide");
        });
    });
});