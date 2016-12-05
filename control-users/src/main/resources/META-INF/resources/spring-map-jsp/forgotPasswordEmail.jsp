<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html>
<head>
    <title><spring:message code="user.registration.passwordReset.title"/></title>
	<link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.ico" type="image/x-icon" />
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

			.column-field-label {
				font-size: 20px;
				line-height: 2;
			}

			.column-field-button {
				-moz-box-shadow: inset 0px 1px 0px 0px #ffffff;
				-webkit-box-shadow: inset 0px 1px 0px 0px #ffffff;
				box-shadow: inset 0px 1px 0px 0px #ffffff;
				background: -webkit-gradient(linear, left top, left bottom, color-stop(0.05, #ededed), color-stop(1, #dfdfdf));
				background: -moz-linear-gradient(top, #ededed 5%, #dfdfdf 100%);
				background: -webkit-linear-gradient(top, #ededed 5%, #dfdfdf 100%);
				background: -o-linear-gradient(top, #ededed 5%, #dfdfdf 100%);
				background: -ms-linear-gradient(top, #ededed 5%, #dfdfdf 100%);
				background: linear-gradient(to bottom, #ededed 5%, #dfdfdf 100%);
				filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#ededed', endColorstr='#dfdfdf', GradientType=0);
				background-color: #ededed;
				-moz-border-radius: 6px;
				-webkit-border-radius: 6px;
				border-radius: 6px;
				border: 1px solid #dcdcdc;
				display: inline-block;
				cursor: pointer;
				color: #777777;
				font-family: Arial;
				font-size: 15px;
				font-weight: bold;
				padding: 6px 24px;
				text-decoration: none;
				text-shadow: 0px 1px 0px #ffffff;
				position: relative;
				top: 20px;
			}

			.column-field-button:hover {
				background: -webkit-gradient(linear, left top, left bottom, color-stop(0.05, #dfdfdf), color-stop(1, #ededed));
				background: -moz-linear-gradient(top, #dfdfdf 5%, #ededed 100%);
				background: -webkit-linear-gradient(top, #dfdfdf 5%, #ededed 100%);
				background: -o-linear-gradient(top, #dfdfdf 5%, #ededed 100%);
				background: -ms-linear-gradient(top, #dfdfdf 5%, #ededed 100%);
				background: linear-gradient(to bottom, #dfdfdf 5%, #ededed 100%);
				filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#dfdfdf', endColorstr='#ededed', GradientType=0);
				background-color: #dfdfdf;
			}

			.column-field-button:active {
				position: relative;
				top: 20px;
			}

			#etusivu {
				padding-top: 20px;
				text-align: center;
			}

			#frontpage, #frontpage:visited {
				color: #3399FF;
			}
      #passwordResetForm{
        width:400px;
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
    	<a href="#" id="frontpage"><spring:message code="oskari.backToFrontpage"/></a>
    </div>
</nav>

<div id="container">
  <div class="row">
     <div class="col-xs-12 col-sm-8 col-md-6 col-sm-offset-2 col-md-offset-5">
         <form role="form" id="passwordResetForm">
    	    <h1><spring:message code="user.registration.passwordReset.title"/></h1>
          <hr class="colorgraph">
            <div class="form-group">
              <input class="form-control input-lg" size="25" id="email" name="email" type="email" placeholder="Email" autofocus required>
            </div>
    			<label id="error"></label>
    		<br/>
        <div class="row">
          <div class="col-xs-2">
    			  <input class="btn btn-primary" size="16" id="submit" type="button" value='<spring:message code="btn.send"/>'>
          </div>
          <div class="col-xs-2">
    			  <input class="btn btn-default" size="16" id="cancel" type="button" value='<spring:message code="btn.cancel"/>'>
          </div>
        </div>
        <hr class="colorgraph">
    </form>
  </div>
</div>
</div>
<!-- forgot pass modal -->
<div class="modal fade" id="passwordModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Email adress</h4>
      </div>
      <div class="modal-body password-alert"></div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      </div>
    </div>
  </div>
</div>

<script type="text/javascript">
$(document).ready(function () {
	$('#frontpage, #cancel').click(function () {
		var host = window.location.protocol + "//" + window.location.host;
		window.location.replace(host);
	});

	$('#submit').click(function () {
		var email = jQuery('#email').val();
		var host = window.location.protocol + "//" + window.location.host;
		if (isEmailValid(email)) {
			jQuery.ajax({
				url: host + "/action?action_route=UserPasswordReset&email=" + email,
				type: 'POST',
				success: function(data) {
					// FIXME: show confirmation about mail being sent
					var url = window.location.protocol + "//" + window.location.host + "/user/emailSent";
					window.location.replace(url);
				},
				error: function(jqXHR, textStatus, errorThrown) {
					//TODO: error handling
					showModal(jqXHR.responseText, true);
				}
			});
		} else
			jQuery("#error").html('<spring:message code="user.registration.error.invalidEmail"/>');
	});
});

function isEmailValid(email) {
	var pattern =/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
	return pattern.test(email);  // returns a boolean
}
function showModal(msg, error) {
  error === true ? $('.password-alert').html(msg).addClass('alert-danger') : $('.password-alert').html(msg).addClass('alert-success');
  $('#passwordModal').modal('show');
  setTimeout(function() {$('#passwordModal').modal('hide');}, 2000);
}

</script>
</body>
</html>
