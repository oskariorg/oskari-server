<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<!DOCTYPE html>
<html>
<head>
    <title>${viewName}</title>
    <link rel="icon" href="favicon.ico" type="image/x-icon">
    <!-- Light mode favicon -->
    <link rel="icon" href="favicon-light.ico" type="image/x-icon" media="(prefers-color-scheme: light)">
    <!-- Dark mode favicon -->
    <link rel="icon" href="favicon-dark.ico" type="image/x-icon" media="(prefers-color-scheme: dark)">
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
    <meta name="format-detection" content="telephone=no" />

    <!-- ############# css ################# -->
    <link
            rel="stylesheet"
            type="text/css"
            href="${clientDomain}/Oskari${path}/icons.css"/>

    <link
            rel="stylesheet"
            type="text/css"
            href="${clientDomain}/Oskari${path}/oskari.min.css"/>

    <link href="https://fonts.googleapis.com/css?family=Noto+Sans" rel="stylesheet">
    <style type="text/css">
        @media screen {
            /* Make the map application fill all available space */
            body {
                margin : 0;
                padding : 0;
                width: 100vw;
                height: 100vh;
            }
        }
    </style>
    <!-- ############# /css ################# -->
</head>
<body>
<div id="oskari">
<%-- The embedded map will be rendered here --%>
</div>


<!-- ############# Javascript ################# -->

<!--  OSKARI -->
<script type="text/javascript">
    var ajaxUrl = '${ajaxUrl}';
    var controlParams = ${controlParams};
</script>
<%-- Pre-compiled application JS, empty unless created by build job --%>
<script type="text/javascript"
        src="${clientDomain}/Oskari${path}/oskari.min.js">
</script>

<%-- language files --%>
<script type="text/javascript"
        src="${clientDomain}/Oskari${path}/oskari_lang_${language}.js">
</script>

<script type="text/javascript"
        src="${clientDomain}/Oskari${path}/index.js">
</script>


<!-- ############# /Javascript ################# -->
</body>
</html>
