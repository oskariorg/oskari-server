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
    <script
            src="https://code.jquery.com/jquery-1.12.4.min.js"
            integrity="sha256-ZosEbRLbNQzLpnKIkEdrPv7lOy9C27hHQ+Xp8a4MxAQ="
            crossorigin="anonymous"></script>
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">

    <!-- ############# css ################# -->
    <style type="text/css">
        @media screen {
            body {
                margin: 0;
                padding: 0;
            }

            .content-column {
                display: block;
            }

            #deleteUser {
                color: #3399FF;
            }

            .colorgraph {
                height: 5px;
                border-top: 0;
                background: #191970;
                border-radius: 5px;
                background-image: -webkit-linear-gradient(left, #62c2e4, #62c2e4 12.5%, #62c2e4 12.5%, #669ae1 25%, #1E90FF 25%, #1E90FF 37.5%, #191970 37.5%, #191970 50%, #191970 50%, #191970 62.5%, #1E90FF 62.5%, #1E90FF 75%, #669ae1 75%, #669ae1 87.5%, #62c2e4 87.5%, #62c2e4);
                background-image: -moz-linear-gradient(left, #62c2e4, #62c2e4 12.5%, #62c2e4 12.5%, #669ae1 25%, #1E90FF 25%, #1E90FF 37.5%, #191970 37.5%, #191970 50%, #191970 50%, #191970 62.5%, #1E90FF 62.5%, #1E90FF 75%, #669ae1 75%, #669ae1 87.5%, #62c2e4 87.5%, #62c2e4);
                background-image: -o-linear-gradient(left, #62c2e4, #62c2e4 12.5%, #62c2e4 12.5%, #669ae1 25%, #1E90FF 25%, #1E90FF 37.5%, #191970 37.5%, #191970 50%, #191970 50%, #191970 62.5%, #1E90FF 62.5%, #1E90FF 75%, #669ae1 75%, #669ae1 87.5%, #62c2e4 87.5%, #62c2e4);
                background-image: linear-gradient(to right, #62c2e4, #62c2e4 12.5%, #62c2e4 12.5%, #669ae1 25%, #1E90FF 25%, #1E90FF 37.5%, #191970 37.5%, #191970 50%, #191970 50%, #191970 62.5%, #1E90FF 62.5%, #1E90FF 75%, #669ae1 75%, #669ae1 87.5%, #62c2e4 87.5%, #62c2e4);
            }
        }
    </style>
    <!-- ############# /css ################# -->
</head>
<body>

<div class="container">
    <div class="row">
        <div class="col-xs-12 col-sm-8 col-md-6 col-sm-offset-2 col-md-offset-4">
            <form role="form" onSubmit="return false;">
                <h1><spring:message code="user.registration.edit.title"/></h1>
                <hr class="colorgraph"/>
                <span id="errorMsg" class="alert alert-danger hidden col-xs-12" role="alert"></span>
                <div class="row">
                    <span class="content-column">
                        <div class="col-xs-12 col-sm-6 col-md-6">
                            <div class="form-group">
                                <input type="text" name="first_name" id="firstname" class="form-control input-lg"
                                       placeholder="First Name" value="${firstname}" tabindex="1" autofocus />
                            </div>
                        </div>
                    </span>
                    <span class="content-column">
                        <div class="col-xs-12 col-sm-6 col-md-6">
                            <div class="form-group">
                                <input type="text" name="last_name" id="lastname"
                                       class="form-control input-lg" value="${lastname}" placeholder="Last Name" tabindex="2"/>
                            </div>
                        </div>
                    </span>
                </div>

                <div class="row">
                    <div class="col-xs-12 col-sm-6 col-md-6">
                        <div class="form-group">
                            <spring:message code="user.email" htmlEscape="true"/>: ${email}<br/>
                            <spring:message code="user.username" htmlEscape="true"/>: ${username}
                        </div>
                      </div>
                </div>

                    <div class="row">
                        <div class="col-xs-12 col-sm-6 col-md-6">
                            <button class="btn btn-primary" id="saveBtn"><spring:message code="btn.save"/></button>
                            <a href="/" class="btn btn-default"><spring:message code="btn.cancel"/></a>
                        </div>
                    </div>
                    <hr class="colorgraph"/>
                    <div class="row">
                        <span class="content-column">
                            <div class="col-xs-12 col-sm-6 col-md-6">
                            <a href="#" id="changePassword"><spring:message code="btn.newPassword"/></a><br/>
                                (<spring:message code="user.help.passwordReset"/>)
                            </div>
                        </span>
                        <span class="content-column">
                            <div class="col-xs-12 col-sm-6 col-md-6">
                                <a href="#" id="deleteUser"><spring:message code="btn.user.delete"/></a>
                            </div>
                        </span>
                    </div>
            </form>
        </div>
    </div>
</div>

<!-- RegistrationModal -->
<div class="modal fade" id="generalModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="generalModalLabel"><spring:message code="user.registration.edit.title"/></h4>
            </div>
            <div class="modal-body registration-success"></div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><spring:message code="btn.close" htmlEscape="true"/></button>
            </div>
        </div>
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
        var saving = false;
        //logged in
        $('#changePassword').click(function () {
            jQuery.ajax({
                url: "/action?action_route=UserPasswordReset",
                type: 'POST',
                data: {
                    email: '${email}'
                },
                success: function () {
                    showModal('<spring:message javaScriptEscape="true" code="user.registration.passwordrecovery.sent"/>');
                },
                error: function (jqXHR) {
                    var errorResponse = jqXHR.responseText;
                    try {
                        var json = JSON.parse(errorResponse);
                        if(json.error) {
                            errorMsg("#errorGeneral", json.error);
                        } else {
                            throw "show generic error";
                        }
                    } catch (e) {
                        errorMsg("#errorGeneral", '<spring:message javaScriptEscape="true" code="user.registration.error.generic"/>');
                    }
                }
            });
        });
        // logged in
        $('#saveBtn').click(function () {
            if (saving || !validate()) {
                return false;
            }
            saving = true;
            jQuery.ajax({
                url: "/action?action_route=UserRegistration",
                type: 'PUT',
                data: {
                    firstname: jQuery('#firstname').val(),
                    lastname: jQuery('#lastname').val()
                },
                success: function () {
                    showModal('<spring:message javaScriptEscape="true" code="user.registration.edit.success"/>', true);
                    saving = false;
                },
                error: function () {
                    saving = false;
                    errorMsg("#errorGeneral", '<spring:message javaScriptEscape="true" code="user.registration.error.generic"/>');
                }
            });
        });

        // logged in
        $('#deleteUser').click(function () {

            $("#deleteDialog").on("show.bs.modal", function () {
                $("#deleteOk").on("click", function (e) {
                    jQuery.ajax({
                        url: "/action?action_route=UserRegistration",
                        type: 'DELETE',
                        success: function () {
                            window.location.href = '/logout';
                        },
                        error: function () {
                            errorMsg("#errorGeneral", '<spring:message javaScriptEscape="true" code="user.registration.error.generic"/>');
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

    //Validates the form values
    function validate() {
        var firstname = $('#firstname').val();
        var lastname = $('#lastname').val();
        var flag = true;
        clearErrorMessage();

        if (!firstname.trim()) {
            errorMsg('#errorFirstname', '<spring:message javaScriptEscape="true" code="user.registration.error.fieldIsRequired"/>');
            flag = false;
        }

        if (!lastname.trim()) {
            errorMsg('#errorLastname', '<spring:message javaScriptEscape="true" code="user.registration.error.fieldIsRequired"/>');
            flag = false;
        }

        return flag;
    }
    function showModal(msg, success) {
        $('.registration-success').removeClass("hidden");
        $('.registration-success').html(msg).addClass("alert-success");
        $('#generalModal').modal('show');
        if (success) {
            $('.container').find('.alert-danger').removeClass('alert-danger');
        }
    }

    function errorMsg(selector, str) {
        $('.container').find('.alert-danger').removeClass('alert-danger');
        $('#errorMsg').removeClass("hidden");
        $('#errorMsg').html('<strong>' + str + '</strong>').addClass("alert-danger");
        $(selector).addClass("alert-danger");
    }

    function clearErrorMessage(selector) {
        $('.alert').text("").addClass("hidden");
        if(selector) {
            $(selector).removeClass("alert-danger");
        }
    }
</script>
</body>
</html>
