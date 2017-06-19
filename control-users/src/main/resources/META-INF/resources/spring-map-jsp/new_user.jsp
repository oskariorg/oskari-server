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
            <form role="form">
                <h1><spring:message code="user.registration.new_user.title"/></h1>
                <hr class="colorgraph"/>
                <span id="errorMsg" class="alert alert-danger hidden col-xs-12" role="alert"></span>
                <div class="row">
                    <div class="col-xs-12 col-sm-12 col-md-12">
                        <div class="form-group">
                            <spring:message code="user.registration.password.requirements"/>
                            <ul>
                                <c:forEach items="${requirements}" var="entry">
                                    <c:choose>
                                        <c:when test="${entry.value['class'].simpleName eq 'Boolean'}">
                                            <%-- if boolean AND true -> show value --%>
                                            <c:if test="${entry.value}">
                                                <li><spring:message code="user.passwd.req.${entry.key}" /></li>
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <li><spring:message code="user.passwd.req.${entry.key}" />: <spring:message code="${entry.value}" /></li>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </ul>
                        </div>
                    </div>
                </div>
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

                    <span class="content-column">
                        <div class="form-group">
                            <input type="text" name="display_name" id="username" class="form-control input-lg"
                                   placeholder="Username" tabindex="3"/>
                        </div>
                    </span>
                    <div class="form-group">
                        <input class="form-control input-lg" tabindex="4" size="16" id="password" name="password" type="password"
                               placeholder="<spring:message code="user.password" htmlEscape="true"/>"
                               required>
                    </div>
                    <div class="form-group">
                        <input class="form-control input-lg" tabindex="5" size="16" id="confirmPassword" name="confirmPassword"
                               placeholder="<spring:message code="user.password.confirm" htmlEscape="true"/>"
                               type="password" required>
                    </div>
                    <br/>

                <div class="row">
                    <div class="col-xs-12 col-md-6"><input type="button" value="Register"
                                                           class="btn btn-primary btn-block btn-lg"
                                                           id="registerBtn" tabindex="7"></div>
                    <br><br>
                </div>
                <hr class="colorgraph">
                <div class="row">
                    <div class="col-md-5"><a href="/"><spring:message code="oskari.backToFrontpage"/></a></div>
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
                <h4 class="modal-title" id="generalModalLabel"><spring:message code="user.registration"/></h4>
            </div>
            <div class="modal-body registration-success"></div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><spring:message code="btn.close" htmlEscape="true"/></button>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(document).ready(function () {
        var saving = false;
        // guest
        $('#username').on("blur", function () {
            var username = jQuery('#username').val();
            if(!username) {
                return;
            }
            clearErrorMessage();
            jQuery.ajax({
                url: "/action?action_route=UserRegistration",
                type: 'GET',
                data: {
                    username: username
                },
                error: function () {
                    errorMsg("#username", '<spring:message javaScriptEscape="true" code="user.registration.error.usernameExists"/>');
                }
            });
        });

        $('#username').on("focus", function () {
            clearErrorMessage('#username');
        });

        // guest
        $('#registerBtn').click(function () {
            if (saving || !validate()) {
                return false;
            }
            saving = true;
            jQuery.ajax({
                url: "/action?action_route=UserRegistration",
                type: 'POST',
                data: {
                    firstname: jQuery('#firstname').val(),
                    lastname: jQuery('#lastname').val(),
                    username: jQuery('#username').val(),
                    password: jQuery('#password').val(),
                    uuid: '${uuid}'
                },
                success: function () {
                    jQuery('#generalModal').on('hidden.bs.modal', function () {
                        window.location.href = '/';
                    });
                    showModal('<spring:message javaScriptEscape="true" code="user.registration.new_user.success"/>', true);
                    // don't reset saving flag so user can't click the button
                },
                error: function (jqXHR) {
                    saving = false;
                    var errorResponse = jqXHR.responseText;
                    if (errorResponse.toLowerCase().indexOf("username") >= 0) {
                        errorMsg("#username", '<spring:message javaScriptEscape="true" code="user.registration.error.usernameExists"/>');
                    } else if (errorResponse.toLowerCase().indexOf("too weak") >= 0) {
                        errorMsg("#password", '<spring:message javaScriptEscape="true" code="user.registration.error.password.requirements"/>');
                    } else {
                        errorMsg("#errorGeneral", '<spring:message javaScriptEscape="true" code="user.registration.error.generic"/>');
                    }
                }
            });
        });

    //Validates the form values
    function validate() {
        var firstname = $('#firstname').val();
        var lastname = $('#lastname').val();
        var flag = true;
        clearErrorMessage();

        if (!firstname.trim()) {
            errorMsg('#firstname', '<spring:message javaScriptEscape="true" code="user.registration.error.fieldIsRequired"/>');
            flag = false;
        }

        if (!lastname.trim()) {
            errorMsg('#lastname', '<spring:message javaScriptEscape="true" code="user.registration.error.fieldIsRequired"/>');
            flag = false;
        }

        var username = $('#username').val();
        if ($('#username').length && !username.trim()) {
            errorMsg('#username', '<spring:message javaScriptEscape="true" code="user.registration.error.fieldIsRequired"/>');
            flag = false;
        }

        var password = jQuery('#password').val();
        var confirmPassword = jQuery('#confirmPassword').val();

        if (!password.trim()) {
            errorMsg('#password', '<spring:message javaScriptEscape="true" code="user.registration.error.fieldIsRequired"/>');
            flag = false;
        }
        if (password != confirmPassword) {
            errorMsg('#confirmPassword', '<spring:message javaScriptEscape="true" code="user.registration.error.passwordDoesNotMatch"/>');
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
});
</script>
</body>
</html>
