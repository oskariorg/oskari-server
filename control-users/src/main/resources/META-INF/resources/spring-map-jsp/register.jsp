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
    <%-- <script type="text/javascript" src="${pageContext.request.contextPath}/Oskari/libraries/jquery/jquery-1.7.1.min.js">
    </script> --%>
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

            #content {
                height: 100%;
                /*margin-left: 153px;*/
                margin: auto;
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
                /*padding-left: 10px;*/
                /*padding-right: 10px;*/
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
            #requestPassword{
              width:400px;
            }

            .error {
                color: red;
            }
            .colorgraph {
              height: 5px;
              border-top: 0;
              background: #191970;
              border-radius: 5px;
              background-image: -webkit-linear-gradient(left, #62c2e4, #62c2e4 12.5%, #62c2e4 12.5%, #669ae1 25%, #1E90FF 25%, #1E90FF 37.5%,#191970 37.5%, #191970 50%, #191970 50%, #191970 62.5%, #1E90FF 62.5%, #1E90FF 75%, #669ae1 75%, #669ae1 87.5%, #62c2e4 87.5%, #62c2e4);
              background-image: -moz-linear-gradient(left, #62c2e4, #62c2e4 12.5%, #62c2e4 12.5%, #669ae1 25%, #1E90FF 25%, #1E90FF 37.5%, #191970 37.5%,#191970 50%,#191970 50%, #191970 62.5%, #1E90FF 62.5%, #1E90FF 75%, #669ae1 75%, #669ae1 87.5%, #62c2e4 87.5%, #62c2e4);
              background-image: -o-linear-gradient(left, #62c2e4, #62c2e4 12.5%,#62c2e4 12.5%, #669ae1 25%, #1E90FF 25%, #1E90FF 37.5%, #191970 37.5%, #191970 50%,#191970 50%, #191970 62.5%, #1E90FF 62.5%, #1E90FF 75%, #669ae1 75%, #669ae1 87.5%, #62c2e4 87.5%, #62c2e4);
              background-image: linear-gradient(to right, #62c2e4, #62c2e4 12.5%, #62c2e4 12.5%, #669ae1 25%,#1E90FF 25%, #1E90FF 37.5%, #191970 37.5%, #191970 50%, #191970 50%,#191970 62.5%, #1E90FF 62.5%, #1E90FF 75%, #669ae1 75%, #669ae1 87.5%, #62c2e4 87.5%, #62c2e4);
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

<div class="container">
  <%-- Signed in --%>
  <c:choose>
      <c:when test="${editExisting}">
      <div class="row">
          <div class="col-xs-12 col-sm-8 col-md-6 col-sm-offset-2 col-md-offset-4">
          <form role="form">
            <h1><spring:message code="user.registration.edit.title"/></h1>
            <hr class="colorgraph">
            <span id="errorMsgExisting" class="alert alert-danger hidden col-xs-12" role="alert"></span>
            <div class="row">
              <span class="content-column">
                				<div class="col-xs-12 col-sm-6 col-md-6">
                    					<div class="form-group">
                                 <input type="text" name="first_name" id="firstname" class="form-control input-lg" placeholder="First Name" tabindex="1">
                    					</div>
                    				</div>
                          </span>
                          <span class="content-column">
                    				<div class="col-xs-12 col-sm-6 col-md-6">
                    					<div class="form-group">
                    						<input type="text" name="last_name" id="lastname" class="form-control input-lg" placeholder="Last Name" tabindex="2">
                    					</div>
                    				</div>
                          </span>
                        </div>
              <span class="content-column">
                <div class="form-group">
                  <input type="email" name="email" id="email" class="form-control input-lg" placeholder="Email Address" tabindex="4">
                </div>
              </span>
                      <br/>
              <span>
                <button class="btn btn-primary" id="saveBtn"><spring:message code="btn.save"/></button>
              </span>
              <span>
                <button class="btn btn-default" id="cancelBtn"><spring:message code="btn.cancel"/></button>
              </span>
              <hr class="colorgraph">
                      <br><br><br>
                      <span class="content-column"> <a href="#" id="changePassword"><spring:message
                              code="btn.newPassword"/></a> </span>
                      (<spring:message code="user.help.passwordReset"/>)
            </form>
          </div>
        </div>
      </c:when>
      <%-- Not signed in --%>
     <c:otherwise>
          <div class="col-xs-12 col-sm-8 col-md-6 col-sm-offset-2 col-md-offset-4">
      		<form role="form">
      			<h2>Register</h2>
            <hr class="colorgraph">
            <span id="errorMsg" class="alert alert-danger hidden col-xs-12" role="alert"></span>
      			<div class="row">
      				<div class="col-xs-12 col-sm-6 col-md-6">
      					<div class="form-group">
                   <input type="text" name="first_name" id="firstname" class="form-control input-lg" placeholder="First Name" tabindex="1">
      					</div>
      				</div>
      				<div class="col-xs-12 col-sm-6 col-md-6">
      					<div class="form-group">
      						<input type="text" name="last_name" id="lastname" class="form-control input-lg" placeholder="Last Name" tabindex="2">
      					</div>
      				</div>
      			</div>
      			<div class="form-group">
      				<input type="text" name="display_name" id="username" class="form-control input-lg" placeholder="Username" tabindex="3">
      			</div>
      			<div class="form-group">
      				<input type="email" name="email" id="email" class="form-control input-lg" placeholder="Email Address" tabindex="4">
      			</div>
                <hr class="colorgraph">
                <div class="row">
                    <div class="col-xs-12 col-md-6"><input type="button" value="Register" class="btn btn-primary btn-block btn-lg" id="registerBtn" tabindex="7"></div>
            <br><br>
            <a class="col-xs-12" href="#" id="forgotPassword"><spring:message code="btn.forgotPassword"/></a>
                </div>
            </form>
        </div>
        </c:otherwise>
      </c:choose>
</div>

<!-- RegistrationModal -->
<div class="modal fade" id="generalModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="generalModalLabel">Registration</h4>
      </div>
      <div class="modal-body registration-success"></div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
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
            },
            error: function (jqXHR, textStatus, errorThrown) {
                //TODO: error handling
                showModal(jqXHR.responseText);
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
                    showModal('<spring:message code="user.registration.email.sent"/>');
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
                        showModal('<spring:message code="user.registration.success"/>', true);
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        var errorResponse = jqXHR.responseText;
                        if (errorResponse.toLowerCase().indexOf("email") >= 0) {
                            errorMsg("#email", '<spring:message javaScriptEscape="true" code="user.registration.error.emailExists"/>');
                        } else if (errorResponse.toLowerCase().indexOf("username") >= 0) {
                            errorMsg("#username", '<spring:message javaScriptEscape="true" code="user.registration.error.usernameExists"/>');
                        } else if (errorResponse.toLowerCase().indexOf("address") >= 0) {
                            errorMsg("#email", '<spring:message javaScriptEscape="true" code="user.registration.error.sendingFailed"/>');
                        } else if (errorResponse.toLowerCase().indexOf("properties") >= 0) {
                            errorMsg("#username", '<spring:message javaScriptEscape="true" code="user.registration.error.emailConfigurationError"/>');
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
                        showModal('<spring:message code="user.registration.edit.success"/>', true);
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
            errorMsg('#errorEmail', '<spring:message code="user.registration.error.invalidEmail"/>');
            flag = false;
        }
        return flag;
    }
    function showModal(msg, success) {
      $('.registration-success').removeClass("hidden");
      $('.registration-success').html(msg).addClass("alert-success");
      $('#generalModal').modal('show');
      if(success){
        $('.container').find('.alert-danger').removeClass('alert-danger');
      }
      setTimeout(function() {$('#generalModal').modal('hide');}, 2000);
    }

    function errorMsg(selector, str) {
        $('.container').find('.alert-danger').removeClass('alert-danger');
        $('#errorMsg').removeClass("hidden");
        $('#errorMsg').html('<strong>'+str+'</strong>').addClass("alert-danger");
        $(selector).addClass("alert-danger");
    }

    function clearErrorMessage() {
        $('.alert').text("").addClass("hidden");
    }
</script>
</body>
</html>
