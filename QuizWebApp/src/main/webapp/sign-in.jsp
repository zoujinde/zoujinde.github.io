<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign In</title>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
  <H1> &nbsp &nbsp &nbsp Sign In</H1>
  <hr>
  <label>User name</label><input id="text_user"/>
  <br>
  <label>Password </label><input type="password" id="text_pass"/>
  <br>
  <input type="button" onclick="window.location.href='sign-up.jsp'" value="Sign Up"/>
  <input type="button" onclick="signIn()" value="Sign In"/>
  <hr>
  <label id="result" style="width:1000px;font-size:30px;"/>
</div>
</HTML>

<style>
  div{
    border-style:solid;
    border-width:1px;
    border-color:#999999;
    font-size:50px;
  }

  label{
    cursor: pointer;
    display: inline-block;
    margin: 10px 10px;
    padding: 1px;
    width: 300px;
    font-size:50px;
    text-align: left;
    vertical-align: top;
  }

  input{
    margin: 10px 10px;
    width: 500px;
    font-size:50px;
    vertical-align: top;
  }

  input[type="button"]{
    width: 300px;
  }

</style>

<script type="text/javascript">
  var httpRequest = null;
  var result = document.getElementById("result");

  // Initiate http
  function initHttp() {
    if (httpRequest != null) {
      result.innerText = "*"; // reuse http object
    } else if (window.XMLHttpRequest) { //IE6 above and other browser
      httpRequest = new XMLHttpRequest()
    } else if(window.ActiveXObject) { //IE6 and lower
      httpRequest = new ActiveXObject();
    }
  }

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
    initHttp();
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", "/user", true);
    // Only post method needs to set header
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    // Set callback
    httpRequest.onreadystatechange = signInResult;
    httpRequest.send(json);
  }

  // Sign in result
  function signInResult() {
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        result.innerText = httpRequest.responseText;
      } else {
        result.innerText = httpRequest.status
      }
    }
  }

</script>

