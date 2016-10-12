<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html>
<head>
    <title><spring:message code="user.registration.title"/></title>
    <link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon"/>
    <script type="text/javascript" src="${pageContext.request.contextPath}/Oskari/libraries/jquery/jquery-1.7.1.min.js">
    </script>
    <!--script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script -->
    <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">

    <!-- ############# css ################# -->
    <style type="text/css">
        @media screen {
            body {
                margin: 0;
                padding: 0;
            }

            #content {
                height: 100%;
                margin-left: 153px;
            }

            #maptools {
                background-color: #333438;
                height: 100%;
                position: absolute;
                top: 0;
                width: 153px;
                z-index: 2;
            }

            #register {
                padding-left: 25px;
            }

            .content-column {
                display: block;
            }

            .column-field-label {
                font-size: 20px;
                line-height: 2;
            }

            .column-field-input {
                border-radius: 5px;
                font-size: 14px;
                height: 30px;
                padding-left: 10px;
                padding-right: 10px;
            }

            .column-field-input:focus {
                background-color: #ECF9EC;
            }

            #etusivu {
                padding-top: 20px;
                text-align: center;
            }

            #frontpage, #frontpage:visited, #deleteUser {
                color: #3399FF;
            }

            #forgotPassword {
                padding-top: 25px;
                font-size: 20px;
                display: block;
            }

            .error {
                color: red;
            }
        }
    </style>
    <!-- ############# /css ################# -->
</head>
<body>

<nav id="maptools">
    <div id="etusivu">
        <a href="#" id="frontpage"><spring:message code="oskari.backToFrontpage"/></a><br><br>
        <c:if test="${!empty id}">
            <a href="#" id="deleteUser"><spring:message code="btn.user.delete"/></a>
        </c:if>
    </div>
</nav>

<div id="content">
    <div id="register">
        <div id="errorGeneral" class="alert alert-danger hidden" role="alert"></div>
        <c:choose>
            <c:when test="${editExisting}">
                <h1><spring:message code="user.registration.edit.title"/></h1>
				<span class="content-column">
					<label class="column-field-label"><spring:message code="user.firstname"/></label> <br>
					<input class="column-field-input" size="20" id="firstname" name="firstname" type="text" required>
					<span id="errorFirstname" class="alert alert-danger hidden" role="alert"></span>
				</span>
				<span class="content-column">
					<label class="column-field-label"><spring:message code="user.lastname"/></label> <br>
					<input class="column-field-input" size="20" id="lastname" name="lastname" type="text" required>
					<span id="errorLastname" class="alert alert-danger hidden" role="alert"></span>
				</span>
				<span class="content-column">
					<label class="column-field-label"><spring:message code="user.email"/></label> <br>
					<input class="column-field-input" size="20" id="email" name="email" type="email" required>
					<span id="errorEmail" class="alert alert-danger hidden" role="alert"></span>
				</span>
                <br/>
				<span>				
					<button class="btn btn-primary" id="saveBtn"><spring:message code="btn.save"/></button>
				</span>			
				<span>				
					<button class="btn btn-default" id="cancelBtn"><spring:message code="btn.cancel"/></button>
				</span>

                <br><br><br>
                <span class="content-column"> <a href="#" id="changePassword"><spring:message
                        code="btn.newPassword"/></a> </span>
                (<spring:message code="user.help.passwordReset"/>)
            </c:when>
            <c:otherwise>
                <h1><spring:message code="user.registration.new.title"/></h1>
				<span class="content-column">
					<label class="column-field-label"><spring:message code="user.firstname"/></label> <br>
					<input class="column-field-input" size="20" id="firstname" name="firstname" type="text" required>
					<span id="errorFirstname" class="alert alert-danger hidden" role="alert"></span>
				</span>
				<span class="content-column">
					<label class="column-field-label"><spring:message code="user.lastname"/></label> <br>
					<input class="column-field-input" size="20" id="lastname" name="lastname" type="text" required>
					<span id="errorLastname" class="alert alert-danger hidden" role="alert"></span>
				</span>
				<span class="content-column">
					<label class="column-field-label"><spring:message code="user.username"/></label> <br>
					<input class="column-field-input" size="20" id="username" name="username" type="text" required>
					<span id="errorUsername" class="alert alert-danger hidden" role="alert"></span>
				</span>
				<span class="content-column">
					<label class="column-field-label"><spring:message code="user.email"/></label> <br>
					<input class="column-field-input" size="20" id="email" name="email" type="email" required>
					<span id="errorEmail" class="alert alert-danger hidden" role="alert"></span>
				</span>
                <br/>
				<span>				
					<button id="registerBtn" class="btn btn-primary"><spring:message code="btn.register"/></button>
				</span>			
				<span>				
					<button id="cancelBtn" class="btn btn-default"><spring:message code="btn.cancel"/></button>
				</span>

                <br><br>
                <a href="#" id="forgotPassword"><spring:message code="btn.forgotPassword"/></a>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<div id="deleteDialog" class="modal fade">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-body">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <spring:message code="user.delete.account.confirm"/>
            </div>
            <div class="modal-footer">
                <button id="deleteOk" class="btn btn-primary"><spring:message code="btn.yes"/></button>
                <button class="btn btn-default" data-dismiss="modal"><spring:message code="btn.no"/></button>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function () {
        <c:if test="${editExisting}">
        $("body").hide();
        $.ajax({
            url: "/action?action_route=UserRegistration&edit=yes",
            type: 'POST',
            success: function (data) {
                $("#firstname").val(data.firstName);
                $("#lastname").val(data.lastName);
                $("#email").val(data.email);
                $("body").show();
                // FIXME: get rid of alert
                alert('<spring:message code="user.registration.edit.success"/>')
            },
            error: function (jqXHR, textStatus, errorThrown) {
                //TODO: error handling
                alert(jqXHR.responseText);
            }
        });
        </c:if>
        $('#frontpage, #cancelBtn').click(function () {
            var host = window.location.protocol + "//" + window.location.host;
            window.location.replace(host);
        });

        $('#forgotPassword').click(function () {
            var host = window.location.protocol + "//" + window.location.host + "/user/reset";
            window.location.replace(host);
        });

        $('#changePassword').click(function () {
            var host = window.location.protocol + "//" + window.location.host;
            jQuery.ajax({
                url: host + "/action?action_route=UserPasswordReset&email=${email}",
                type: 'POST',
                success: function (data) {
                    // FIXME: show confirmation about mail being sent
                    alert('<spring:message code="user.registration.email.sent"/>')
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    //TODO: error handling
                    errorMsg("#errorGeneral", "SERVER ERROR");
                }
            });
        });

        $('#registerBtn').click(function () {
            var data = {
                firstname: jQuery('#firstname').val(),
                lastname: jQuery('#lastname').val(),
                username: jQuery('#username').val(),
                email: jQuery('#email').val()
            };
            var host = window.location.protocol + "//" + window.location.host;
            if (validate()) {
                jQuery.ajax({
                    url: host + "/action?action_route=UserRegistration&register",
                    type: 'POST',
                    data: data,
                    success: function (data) {
                        // FIXME: show confirmation about mail being sent
                        alert('<spring:message code="user.registration.success"/>')
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        var errorResponse = jqXHR.responseText;
                        if (errorResponse.toLowerCase().indexOf("email") >= 0) {
                            errorMsg("#errorEmail", '<spring:message code="user.registration.error.emailExists"/>');
                        } else if (errorResponse.toLowerCase().indexOf("username") >= 0) {
                            errorMsg("#errorUsername", '<spring:message code="user.registration.error.usernameExists"/>');
                        } else {
                            //TODO: error handling
                            errorMsg("#errorGeneral", jqXHR.responseText);
                        }
                    }
                });
            }
        });

        $('#saveBtn').click(function () {
            var data = {
                id: "${id}",
                firstname: jQuery('#firstname').val(),
                lastname: jQuery('#lastname').val(),
                email: jQuery('#email').val()
            };
            var host = window.location.protocol + "//" + window.location.host;
            if (validate()) {
                jQuery.ajax({
                    url: host + "/action?action_route=UserRegistration&update=yes",
                    type: 'POST',
                    data: data,
                    success: function (data) {
                        // FIXME: show confirmation about mail being sent
                        alert('<spring:message code="user.registration.edit..success"/>');
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        //TODO: error handling
                        errorMsg("#errorGeneral", jqXHR.responseText);
                    }
                });
            }
        });

        $('#deleteUser').click(function () {
            var data = {id: '${id}'},
                    host = window.location.protocol + "//" + window.location.host;

            $("#deleteDialog").on("show.bs.modal", function () {
                $("#deleteOk").on("click", function (e) {
                    jQuery.ajax({
                        url: host + "/action?action_route=UserRegistration&delete",
                        type: 'POST',
                        data: data,
                        success: function (data) {
                            window.location.href = '/logout';
                        },
                        error: function (jqXHR, textStatus, errorThrown) {
                            errorMsg("#errorGeneral", jqXHR.responseText);
                        }
                    });
                    $("#deleteDialog").modal('hide');
                });
            });

            $("#deleteDialog").on("hide.bs.modal", function () {
                $("#deleteDialog .btn").off("click");
            });

            $("#deleteDialog").modal({"backdrop": "static", "keyboard": true, "show": true});
        });

    });

    function isEmailValid(email) {
        var pattern = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
        return pattern.test(email);  // returns a boolean
    }

    //Validates the form values
    function validate() {
        var firstname = $('#firstname').val();
        var lastname = $('#lastname').val();
        var username = $('#username').val();
        var email = $('#email').val();
        var flag = true;
        clearErrorMessage();

        if (!firstname.trim()) {
            errorMsg('#errorFirstname', '<spring:message code="user.registration.error.fieldIsRequired"/>');
            flag = false;
        }

        if (!lastname.trim()) {
            errorMsg('#errorLastname', '<spring:message code="user.registration.error.fieldIsRequired"/>');
            flag = false;
        }

        if ($('#username').length && !username.trim()) {
            errorMsg('#errorUsername', '<spring:message code="user.registration.error.fieldIsRequired"/>');
            flag = false;
        }

        if (!isEmailValid(email)) {
            errorMsg('#errorEmail', '<spring:message code="user.registration.error.invalidEmailError"/>');
            flag = false;
        }
        return flag;
    }

    function errorMsg(selector, str) {
        $(selector).text(str).removeClass("hidden");
    }

    function clearErrorMessage() {
        $('.alert').text("").addClass("hidden");
    }
</script>
</body>
</html>
