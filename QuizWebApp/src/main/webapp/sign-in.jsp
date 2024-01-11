<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign In</title>
<%@ include file="head.jsp"%>
<div id="div_user" style="width:100%; margin:auto; overflow:auto;">
  <br><br><br><br><br>
  <label>User name</label><input id="text_user"/><br><br>
  <label>Password </label><input type="password" id="text_pass"/><br>
  <br><br>
  <input type="button" onclick="signIn()" value="Sign In" style="width:910px;"/>
  <hr style="font-size:1px;">
  <label id="result" style="width:910px;"/>
</div>

<div id="div_home" style="width:100%; margin:auto; overflow:auto;">
  <br><br><br>
  <label style="width:910px;text-align:center;font-weight:bold;font-size:60px;">Welcome to Purple Sense Home</label>
  <br><br>
  <textarea rows="15" cols="50" id="text" readonly="true" style="margin:10px 10px;resize:none;font-size:30px;">
Any personal information provided by the user to the application will be treated as confidential, our group shall hold personal Information in the strictest confidential and shall not disclose or use Personal Information, except under any regulatory or legal proceedings. In case such disclosure is required to be made by law or any regulatory authority, it will be made on a ‘need-to-know’ basis, unless otherwise instructed by the regulatory authority. 

Our group understand that there are laws in the United States and other countries that protect Personal Information, and that we must not use Personal Information other than for the purposes which was originally used or make any disclosures of personal Information to any third party or from one country to another without prior approval of an authorized representative of the Parent. 
  </textarea>
  <br><br>
  <input type="button" onclick="window.location.href='sign-in.jsp?act=input'" value="Sign In"
     style="width:300px;height:80px;margin:5px 100px;"/>
  <input type="button" onclick="window.location.href='sign_form.jsp'" value="Sign Up"
     style="width:300px;height:80px;margin:5px 5px;"/>
</div>
</HTML>

<script type="text/javascript">
  var httpRequest = getHttpRequest();
  var result = document.getElementById("result");
  var action = getUrlValue("act");

  // Delay load
  setTimeout("load()", 1);

  // Load
  function load() {
    if (action == "input") { // hide div_home
      var div_home = document.getElementById("div_home");
      div_home.style.display = "none";
      var user = getUrlValue("user");
      if (user.length >= 6) {
          document.getElementById("text_user").value = user;
      }
    } else { // show div_home
      var div_home = document.getElementById("div_home");
      var div_head = document.getElementById("div_head");
      var div_user = document.getElementById("div_user");
      div_home.style.display = "inline";
      div_head.style.display = "none";
      div_user.style.display = "none";
    }
  }

  // Sign in
  function signIn() {
    var user = document.getElementById("text_user").value.trim();
    var pass = document.getElementById("text_pass").value.trim();
    if (user.length < 3) {
      var text = "Please input the user name. (length>=3)";
      result.innerText = text;
      alert(text);
      return;
    }
    if (pass.length < 3) {
      var text = "Please input the password. (length>=3)";
      result.innerText = text;
      alert(text);
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
      var text = httpRequest.responseText.trim();
      if(httpRequest.status==200) { // 200 OK
        if (text.endsWith('.jsp')) {
          window.location.href = text;
        } else {
          result.innerText = text;
          alert(text);
        }
      } else {
        text = httpRequest.status + text;
        result.innerText = text;
        alert(text);
      }
    }
  }

</script>
