<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<HTML>
<title>Sign In</title>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
  <H1> &nbsp &nbsp &nbsp Quiz Web App</H1>
  <hr>
  <label>User name</label><input id="text_user"/><br>
  <label>Password </label><input type="password" id="text_pass"/><br>
  <input type="button" onclick="signUp()" value="Sign Up"/>
  <input type="button" onclick="signIn()" value="Sign In"/>
  <hr>
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
  var text_user = document.getElementById("text_user");
  var text_pass = document.getElementById("text_pass");

  // Initiate http
  function initHttp() {
    if (httpRequest != null) {
      //state.innerText = "reuse http object"
    } else if (window.XMLHttpRequest) { //IE6 above and other browser
      httpRequest = new XMLHttpRequest()
    } else if(window.ActiveXObject) { //IE6 and lower
      httpRequest = new ActiveXObject();
    }
  }

  // Sign in
  function singIn() {
    initHttp();
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", url.value, true);
    // Only post method needs to set header
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    // Set callback
    httpRequest.onreadystatechange = signInResult;
    httpRequest.send(text_user.value);
  }

  // Sign in result
  function signInResult() {
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        result_add.innerText = httpRequest.responseText;
      } else {
        result_add.innerText = httpRequest.status
      }
    }
  }

</script>

