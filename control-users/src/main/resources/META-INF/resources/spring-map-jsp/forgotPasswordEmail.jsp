<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html>
<head>
    <title><spring:message code="user.registration.passwordReset.title"/></title>
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

            #error {
                color: red;
                font-size: 16px;
            }

            .colorgraph {
                height: 5px;
                border-top: 0;
                background: #c4e17f;
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
            <form role="form" id="passwordResetForm" method="post" onSubmit="return resetPassword();">
                <h1><spring:message code="user.registration.passwordReset.title"/></h1>
                <hr class="colorgraph">
                <div class="form-group">
                    <c:choose>
                        <c:when test="${empty email}">
                            <input class="form-control input-lg" size="25" id="email" name="email" type="email"
                                   placeholder="<spring:message code="user.email" htmlEscape="true"/>" autofocus required>
                        </c:when>
                        <c:otherwise>
                            <spring:message code="user.email" htmlEscape="true"/>: ${email}
                        </c:otherwise>
                    </c:choose>
                </div>
                <label id="error"></label>
                <br/>
                <div class="row">
                    <div class="col-xs-12 col-sm-6 col-md-6">
                        <button class="btn btn-primary" type="submit"><spring:message code="btn.send"  htmlEscape="true"/></button>
                        <a href="/" class="btn btn-default"><spring:message code="btn.cancel"/></a>
                    </div>
                </div>
                <hr class="colorgraph">
                <div class="row">
                    <div class="col-md-5"><a href="/"><spring:message code="oskari.backToFrontpage"/></a></div>
                </div>
            </form>
        </div>
    </div>
</div>
<!-- forgot pass modal -->
<div class="modal fade" id="passwordModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel"><spring:message code="user.email" htmlEscape="true"/></h4>
            </div>
            <div class="modal-body password-alert"></div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><spring:message code="btn.close" htmlEscape="true"/></button>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    var inProgress = false;
    function resetPassword() {
        inProgress = true;
        var email='';
        <c:if test="${empty email}">
            email = jQuery('#email').val();
            if (!isEmailValid(email)) {
                jQuery("#error").html('<spring:message code="user.registration.error.invalidEmail"/>');
                return false;
            }
        </c:if>
        jQuery.ajax({
            url: "/action?action_route=UserPasswordReset",
            type: 'POST',
            data: {
                email: email
            },
            success: function () {
                inProgress = false;
                showModal('<spring:message javaScriptEscape="true" code="user.registration.passwordrecovery.sent"/>');
            },
            error: function (jqXHR) {
                inProgress = false;
                var errorResponse = jqXHR.responseText;
                try {
                    var json = JSON.parse(errorResponse);
                    if(json.error) {
                        showModal(json.error, true);
                    } else {
                        throw "show generic error";
                    }
                } catch (e) {
                    showModal('<spring:message javaScriptEscape="true" code="user.registration.error.generic"/>', true);
                }
            }
        });
        return false;
    }

    function isEmailValid(email) {
        var pattern = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
        return pattern.test(email);  // returns a boolean
    }
    function showModal(msg, error) {
        var notification = jQuery('.password-alert');
        notification.removeClass('alert-danger');
        notification.removeClass('alert-success');
        notification.html(msg);
        if(error) {
            notification.addClass('alert-danger');
        } else {
            notification.addClass('alert-success');
        }
        jQuery('#passwordModal').modal('show');
    }

    <c:if test="${!empty error}">
        showModal('<spring:message javaScriptEscape="true" code="${error}"/>', true);
    </c:if>
</script>
</body>
</html>
