<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page isELIgnored="false" %>
<!DOCTYPE html>
<html>
<head>
<title>demo</title>
<!-- ############# css ################# -->

<script type="text/javascript" src="http://code.jquery.com/jquery-1.7.2.min.js">
</script>

<style type="text/css">
@media screen {
  body {
    margin : 0;
    padding: 0;
  }
  #mapdiv {
    width: 100%;
  }
  #maptools {
    background-color: #333438;
    height: 100%;
    position: absolute;
    top: 0;
    width: 153px;
    z-index: 2;
  }
  #contentMap {
    height: 100%;
    margin-left: 153px;
  }
  #login {
    margin-left: 5px;
  }
  #login a {
    color: #FFF;
  }
}
</style>
<!-- ############# /css ################# -->
</head>
<body>

<nav id="maptools">
<div id="loginbar">
</div>
<div id="menubar">
</div>
<div id="divider">
</div>
<div id="toolbar">
</div>
<div id="login">
<c:choose>
 <c:when test="${!empty loginState}">
    <b>Invalid password or user name!!</b></br>
  </c:when>
</c:choose>
<c:choose>
  <c:when test="${!empty user}">
	<a href="/ajax/?action=logout">Logout</a>
  </c:when>
  <c:otherwise>
   <form id="login" action='/ajax/?action=login' method="post" accept-charset="UTF-8">
	    <input size="16" id="username" name="username" type="text" placeholder="Username" autofocus required>
	    <input size="16" id="password" name="password" type="password" placeholder="Password" required>
	    <input type="submit" id="submit" value="Log in">
	</form>
  </c:otherwise>
</c:choose>
</div>
</nav>
<div id="contentMap">
<div id="mapdiv"></div>
</div>


<!-- ############# Javascript ################# -->

<!--  OSKARI -->
<script type="text/javascript"
	src="Oskari/bundles/bundle.js">
</script>

<!--  OPENLAYERS -->
<script type="text/javascript"
	src="Oskari/packages/openlayers/startup.js">
</script>

<script type="text/javascript" src="index.js">
</script>

<!-- ############# /Javascript ################# -->
</body>
</html>
