<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Setup for Oskari</title>
</head>
<body>
<div>
    <h1>Geoserver setup result</h1>
    Geoserver setup: ${message}<br>
    These properties need to be added/updated in oskari-ext.properties:
    <ul>
    <c:forEach var="prop" items="${properties}">
        <li>${prop.key} = ${prop.value}</li>
    </c:forEach>
    </ul>
</div>
</body>
</html>
