<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign In</title>
<%@ include file="head.jsp"%>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
  <label>User name</label><input id="text_user"/><br>
  <label>Password </label><input type="password" id="text_pass"/><br>
  <input type="button" onclick="signIn()" value="Sign In" style="width:910px;"/>
  <hr style="font-size:1px;">
  <label id="result" style="width:910px;font-size:30px;"/>
</div>
</HTML>

<script type="text/javascript">
  var httpRequest = getHttpRequest();

  // Sign in
  function signIn() {
    var user = document.getElementById("text_user").value.trim();
    var pass = document.getElementById("text_pass").value.trim();
    if (user.length < 3) {
      alert("Please input the user name. (length>=3)");
      return;
    }
    if (pass.length < 3) {
      alert("Please input the password. (length>=3)");
      return;
    }
    var json = {'act':'signIn', 'user_name':user, 'password':pass};
    json = JSON.stringify(json);
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", "user", true);
    // Only post method needs to set header
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    // Set callback
    httpRequest.onreadystatechange = signInResult;
    httpRequest.send(json);
  }

  // Sign in result
  function signInResult() {
    if(httpRequest.readyState==4) {
      var result = document.getElementById("result");
      if(httpRequest.status==200) { // 200 OK
        var text = httpRequest.responseText.trim();
        if (text.startsWith('<')) {
          document.close();
          document.write(text);
        } else {
          result.innerText = text;
        }
      } else {
        result.innerText = httpRequest.status + httpRequest.responseText;
      }
    }
  }

</script>
