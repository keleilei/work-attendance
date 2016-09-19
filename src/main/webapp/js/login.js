$(function(){
    $(".ui.checkbox").checkbox();

    $("#login-btn").click(function () {
        var formData = $("form:eq(0)").serialize();
        console.log(formData);
        $(".ui.dimmer").dimmer({closable: false}).dimmer("show");
        $.ajax({
            url : "/rest/login/validate",
            data : formData,
            dataType : "json"
        }).done(function (data) {
            console.log(data);
        });
    });
});