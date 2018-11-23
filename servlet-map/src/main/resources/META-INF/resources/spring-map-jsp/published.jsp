<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<!DOCTYPE html>
<html>
<head>
    <title>${viewName}</title>
    <link rel="shortcut icon" href="/Oskari${path}/logo.png" type="image/png" />
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
    <!-- IE 9 polyfill for openlayers 3 - https://github.com/openlayers/ol3/issues/4865 -->
    <!--[if lte IE 9]> <script src="https://cdn.polyfill.io/v2/polyfill.min.js?features=fetch,requestAnimationFrame,Element.prototype.classList"></script> <![endif]-->

    <!-- ############# css ################# -->
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/icons.css"/>
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/css/overwritten.css"/>
    <style type="text/css">
        @media screen {
            body {
                margin : 0;
                padding : 0;
            }
            #mapdiv {
                width: 100%;
            }
            #contentMap {
                height: 100%;
            }
        }
    </style>
    <!-- ############# /css ################# -->
</head>
<body>
<div id="contentMap" class="oskariui container-fluid published">
    <div class="row-fluid" style="height: 100%; background-color:white;">
        <div class="oskariui-left"></div>
        <div class="span12 oskariui-center" style="height: 100%; margin: 0;">
            <div id="mapdiv"></div>
        </div>
        <div class="oskari-closed oskariui-right">
            <div id="mapdivB"></div>
        </div>
    </div>
</div>


<!-- ############# Javascript ################# -->

<!--  OSKARI -->

<script type="text/javascript">
    var ajaxUrl = '${ajaxUrl}';
    var controlParams = ${controlParams};
</script>
<%-- Pre-compiled application JS, empty unless created by build job --%>
<script type="text/javascript"
        src="/Oskari${path}/oskari.min.js">
</script>

<%-- language files --%>
<script type="text/javascript"
        src="/Oskari${path}/oskari_lang_${language}.js">
</script>

<script type="text/javascript"
        src="/Oskari${path}/index.js">
</script>


<!-- ############# /Javascript ################# -->
</body>
</html>
