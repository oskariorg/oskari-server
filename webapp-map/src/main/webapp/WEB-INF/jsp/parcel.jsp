<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>${viewName}</title>

    <script type="text/javascript" src="http://code.jquery.com/jquery-1.7.2.min.js">
    </script>

    <!-- ############# css ################# -->
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/icons.css"/>
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/css/forms.css"/>
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/css/portal.css"/>

    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/css/overwritten.css"/>

    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/css/parcel.css"/>

    <style type="text/css">
        @media screen {
            body {
                margin: 0;
                padding: 0;
            }

            #loginbar {
                padding: 10px 10px 0 16px;
                color: #CCC;
                vertical-align: bottom;
                margin-bottom: 8px;
                margin-top: 20px;
                font-size: 12px;
                line-height: 12.6px;
            }
            #loginbar a {
                color: #FFDE00;
                font-size: 8pt;
                line-height: 150%;
            }
            #mapdiv {
                width: 100%;
                background : white;
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
        }
    </style>
    <!-- ############# /css ################# -->
</head>
<body>


<!--  CLIPPER -->
<script type="text/javascript"
        src="/Oskari/libraries/clipper/clipper.js">
</script>

<!--  JSTS -->
<script type="text/javascript"
        src="/Oskari/libraries/jsts/javascript.util.js">
</script>

<script type="text/javascript"
        src="/Oskari/libraries/jsts/jsts.js">
</script>

<!-- ############# css ################# -->
<link
        rel="stylesheet"
        type="text/css"
        href="/Oskari${path}/css/icons.css" />
<link
        rel="stylesheet"
        type="text/css"
        href="/Oskari${path}/css/forms.css" />
<link
        rel="stylesheet"
        type="text/css"
        href="/Oskari${path}/css/portal.css" />

<link
        rel="stylesheet"
        type="text/css"
        href="/Oskari${path}/css/overwritten.css"/>

<link
        rel="stylesheet"
        type="text/css"
        href="/Oskari${path}/css/parcel.css"/>


<style type="text/css">
</style>
<!-- ############# /css ################# -->
<nav id="maptools">
    <div id="logoarea">
        <img src="/Oskari/applications/parcel/resources/images/logo.png" alt="Maanmittauslaitos" width="153" height="76">
    </div>
    <div id="loginbar">
    </div>
    <div id="menubar">
    </div>
    <div id="divider">
    </div>
    <div id="toolbar">
    </div>
</nav>
<div id="contentMap" class="oskariui container-fluid">
    <div id="menutoolbar" class="container-fluid"></div>
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
    var ajaxUrl = '${ajaxUrl}&';
    var controlParams = ${controlParams};
</script>
<script type="text/javascript"
        src="/Oskari/bundles/bundle.js">
</script>

<c:if test="${preloaded}">
    <!-- Pre-compiled application JS, empty unless created by build job -->
    <script type="text/javascript"
            src="/Oskari${path}/oskari.min.js">
    </script>
    <!-- Minified CSS for preload -->
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/oskari.min.css"
            />
    <%--language files --%>
    <script type="text/javascript"
            src="/Oskari${path}/oskari_lang_${language}.js">
    </script>
</c:if>

<script type="text/javascript"
        src="/Oskari${path}/index.js">
</script>

<!-- ############# /Javascript ################# -->

</body>
</html>
