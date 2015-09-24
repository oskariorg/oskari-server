<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Setup for Oskari</title>
</head>
<body>
<div>
    <h1>Populate Geoserver</h1>
    Using database/geoserver configurations from oskari-ext.properties:<br>
    <ul>
    <c:forEach var="prop" items="${properties}">
        <li>${prop.key} = ${prop.value}</li>
    </c:forEach>
    </ul>
    <form action="setup">
        <input type="text" name="srs" value="EPSG:4326" />
        <input type="submit" />
    </form>
</div>
</body>
</html>
