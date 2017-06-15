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

            .reg-description {
                margin-bottom: 15px;
            }

            a.reg-link {
                font-size: 18px;
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
            <form role="form" id="form" method="post" action="/user" onSubmit="return validate();">
                <h1><spring:message code="user.registration"/></h1>
                <hr class="colorgraph"/>
                <span id="errorMsg" class="alert alert-danger hidden col-xs-12" role="alert"></span>

                <div class="row reg-description">
                    <div class="col-xs-12 col-md-12">
                        <spring:message code="user.registration.description"/>
                    </div>
                </div>
                <span class="content-column">
                    <div class="form-group">
                        <input type="email" name="email" id="email" class="form-control input-lg" autofocus="autofocus"
                               placeholder="Email Address" tabindex="4">
                    </div>
                </span>
                <br/>

                <div class="row">
                    <div class="col-xs-12 col-md-6"><input type="submit" value="Register"
                                                           class="btn btn-primary btn-block btn-lg"
                                                           id="registerBtn" tabindex="7"></div>
                    <br><br>
                </div>
                <hr class="colorgraph"/>
                <div class="row">
                        <span class="content-column">
                            <div class="col-xs-6 col-sm-6 col-md-6">
                                <a href="/user/reset" class="reg-link"><spring:message
                                        code="btn.forgotPassword"/></a>
                            </div>
                        </span>
                        <span class="content-column">
                            <div class="col-xs-6 col-sm-6 col-md-6">
                                <a href="/" class="reg-link"><spring:message
                                        code="oskari.backToFrontpage"/></a>
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
    function isEmailValid(email) {
        var pattern = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
        return pattern.test(email);  // returns a boolean
    }

    //Validates the form values
    function validate() {
        var email = $('#email').val();
        var flag = true;
        clearErrorMessage();

        if (!isEmailValid(email)) {
            errorMsg('<spring:message javaScriptEscape="true" code="user.registration.error.invalidEmail"/>');
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

    function errorMsg(str) {
        $('.container').find('.alert-danger').removeClass('alert-danger');
        $('#errorMsg').removeClass("hidden");
        $('#errorMsg').html('<strong>' + str + '</strong>').addClass("alert-danger");
    }

    function clearErrorMessage() {
        $('.alert').text("").addClass("hidden");
    }

    <c:if test="${!empty error}">
    errorMsg('<spring:message javaScriptEscape="true" code="${error}"/>');
    </c:if>
    <c:if test="${!empty msg}">
    showModal('<spring:message javaScriptEscape="true" code="${msg}"/>', true);
    </c:if>
</script>
</body>
</html>
