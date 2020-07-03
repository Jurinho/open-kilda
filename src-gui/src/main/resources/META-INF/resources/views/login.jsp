<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>OPEN KILDA</title>	
<link href="<%=request.getContextPath()%>/lib/css/bootstrap.min.css" rel="stylesheet"></link>
<link href="<%=request.getContextPath()%>/ui/css/custom.css" rel="stylesheet"></link>
<link href="<%=request.getContextPath()%>/ui/images/kilda.png" rel="shortcut icon" type="image/png"></link>
<script src="<%=request.getContextPath()%>/lib/javascript/jquery-3.5.1.min.js"></script>
<style>
html, body {
	height: 100%;
	width: 100%;
}
body {
	pointer-events: all;
}
</style>
</head>
<body>
<div class="login-wrapper">
  <div class="login">
  	<div class="col-lg-12">
      <form role="form" method="POST" action="authenticate">
        <h1 class="text-center">OPEN KILDA</h1>
        <fieldset>
            <div class="form-group">
                <input type="name" autocomplete="false" name="username" id="username" class="form-control input-lg" placeholder="Email" autofocus="true" required="true"></input>
            </div>
            <div class="form-group">
                <input type="password" autocomplete="off" name="password" id="password" class="form-control input-lg" placeholder="Password" required="true"></input>
            </div>
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <span class="button-checkbox">
                <input type="submit" class="btn btn-md btn-primary btn-submit" value="Login"></input>
            </span>
            <span class="login_error">${error}</span>
        </fieldset>
    </form>
    </div>
  </div>
</div>
</body>
</html>