<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <title>Footprints</title>
  <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/stylesheets/login.css">
</head>
<body>
<div class="loginContainer">
  <div class="upperContainer">
    <div class="title">
      <span class="agency">Tyrell</span>
      <div class="text">
        Replicant Example Site
      </div>
    </div>
  </div>
  <div class="lowerContainer">
    <div class="logout">
      <div class="message">
        You are now logged out
      </div>
      <div>
        <a id="login-link" href="${pageContext.request.contextPath}/">Return to login page</a>
      </div>
    </div>
  </div>
  <div class="footer">
    <img src="${pageContext.request.contextPath}/images/help.png" alt="Help"/>

    <div class="text">
      For help with accounts please contact support at
      <a href="mailto:support@example.com">support@example.com</a>
    </div>
  </div>
</div>
</body>
</html>