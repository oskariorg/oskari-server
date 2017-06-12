<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

            .error {
                color: red;
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
    <c:choose>
        <c:when test="${empty uuid}">
            <span class="error"><h2>${error}</h2></span>
        </c:when>
        <c:otherwise>
            <div class="row">
                <div class="col-xs-12 col-sm-8 col-md-6 col-sm-offset-2 col-md-offset-4">
                    <form role="form" id="requestPassword">
                        <h1><spring:message code="user.registration.passwordReset.title"/></h1>
                        <hr class="colorgraph">

                        <div class="form-group">
                            <spring:message code="user.registration.password.requirements"/>
                            <ul>
                            <c:forEach items="${requirements}" var="entry">
                                <li><spring:message code="user.passwd.req.${entry.key}" />: <spring:message code="${entry.value}" /></li>
                            </c:forEach>
                            </ul>
                        </div>
                        <div class="form-group">
                            <input class="form-control input-lg" size="16" id="password" name="password" type="password"
                                   placeholder="<spring:message code="user.password" htmlEscape="true"/>"
                                   autofocus required>
                        </div>
                        <div class="form-group">
                            <input class="form-control input-lg" size="16" id="confirmPassword" name="confirmPassword"
                                   placeholder="<spring:message code="user.password.confirm" htmlEscape="true"/>"
                                   type="password" required>
                        </div>
                        <label id="unmatchedPassword" class="error">"<spring:message
                                code="user.registration.error.passwordDoesNotMatch"/>"</label>
                        <br/>
                        <hr class="colorgraph">
					<span>
						<span><input class="btn btn-primary" size="16" id="reset" type="button"
                                     value="<spring:message code="btn.password.reset"/>"></span>
					</span>
                    </form>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- forgot pass modal -->
<div class="modal fade" id="passwordResetModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel"><spring:message code="user.registration.passwordReset.title"/></h4>
            </div>
            <div class="modal-body password-reset"></div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><spring:message code="btn.close" htmlEscape="true"/></button>
            </div>
        </div>
    </div>
</div>


<script type="text/javascript">
    $(document).ready(function () {
        jQuery('#unmatchedPassword').hide();

        $('#frontpage, #cancel').click(function () {
            var host = window.location.protocol + "//" + window.location.host;
            window.location.replace(host);
        });

        $('#reset').click(function () {
            var password = jQuery('#password').val();
            var confirmPassword = jQuery('#confirmPassword').val();

            if (password != confirmPassword) {
                jQuery('#unmatchedPassword').show();
                return;
            }
            else {
                jQuery('#unmatchedPassword').hide();
            }

            var uuid = '${uuid}';
            var host = window.location.protocol + "//" + window.location.host;
            jQuery.ajax({
                url: host + "/action?action_route=UserPasswordReset",
                type: 'PUT',
                contentType: "application/json; charset=UTF-8",
                data: JSON.stringify({
                    password: password,
                    uuid: uuid
                }),
                success: function () {
                    showModal('<spring:message javaScriptEscape="true" code="oskari.password.changed"/>')
                },
                error: function (jqXHR) {
                    var errorResponse = jqXHR.responseText;
                    if (errorResponse.toLowerCase().indexOf("too weak") >= 0) {
                        showModal('<spring:message javaScriptEscape="true" code="user.registration.error.password.requirements"/>');
                    } else {
                        showModal('<spring:message javaScriptEscape="true" code="user.registration.error.generic"/>');
                    }
                }
            });
        });
        function showModal(msg) {
            $('.password-reset').html(msg);
            $('#passwordResetModal').modal('show');
            setTimeout(function () {
                $('#passwordResetModal').modal('hide');
            }, 2000);
        }
    });

</script>
</body>
</html>
