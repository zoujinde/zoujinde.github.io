<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign Up</title>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
  <H1> &nbsp &nbsp &nbsp Sign Up</H1>
  <hr>
  <label>User type</label>
  <select id="user_type">
    <option value="1">Volunteer</option>
    <option value="2">Parents</option>
    <option value="3">Participant</option>
  </select><br>
  <label>User name </label><input id="user_name"/><br>
  <label>Password  </label><input type="password" id="password"/><br>
  <label>Nickname  </label><input id="nickname"/><br>
  <label>Birth year</label><input id="birth_year"/><br>
  <label>Gender    </label>
  <select id="gender">
    <option value="1">Male</option>
    <option value="0">Female</option>
  </select><br>
  <label>Address  </label><input id="address"/><br>
  <label>Email    </label><input id="email"/><br>
  <label>Phone    </label><input id="phone"/><br>
  <input type="button" onclick="save()" value="Save" style="width:900px;margin:20px 30px;"/>
  <hr>
  <label id="result" style="width:1000px;font-size:30px;"/>
</div>
</HTML>

<style>
  div{
    border-style:solid;
    border-width:1px;
    border-color:#999999;
    font-size:36px;
  }

  label{
    cursor: pointer;
    display: inline-block;
    margin: 10px 10px;
    padding: 1px;
    width: 220px;
    font-size:50px;
    text-align: left;
    vertical-align: top;
  }

  input{
    margin: 10px 10px;
    width: 680px;
    font-size:50px;
    vertical-align: top;
  }

  select{
    margin: 10px 10px;
    width: 670px;
    font-size:50px;
    vertical-align: top;
  }

</style>

<script type="text/javascript">
  var httpRequest = null;
  var result    = document.getElementById("result");

  // Initiate http
  function initHttp() {
    if (httpRequest != null) {
      result.innerText = "*";
    } else if (window.XMLHttpRequest) { //IE6 above and other browser
      httpRequest = new XMLHttpRequest();
    } else if(window.ActiveXObject) { //IE6 and lower
      httpRequest = new ActiveXObject();
    }
  }

  // Save data
  function save() {
    // Check the data
    var user_type = document.getElementById("user_type").value.trim();
    var user_name = document.getElementById("user_name").value.trim();
    if (user_name.length < 3) {
      alert("Please input the user name. (length>=3)");
      return;
    }
    var password  = document.getElementById("password").value.trim();
    if (password.length < 3) {
      alert("Please input the password. (length>=3)");
      return;
    }
    var nickname  = document.getElementById("nickname").value.trim();
    if (nickname.length < 3) {
      alert("Please input the nickname. (length>=3)");
      return;
    }
    var birth_year  = document.getElementById("birth_year").value.trim();
    if (user_type == 3 && birth_year.length < 4) {
      alert("Please input the birth year. (length>=4)");
      return;
    }
    var gender    = document.getElementById("gender").value.trim();
    var address   = document.getElementById("address").value.trim();
    var email     = document.getElementById("email").value.trim();
    var phone     = document.getElementById("phone").value.trim();
    // Confirm the request
    var json = {'act':'signUp', 'user_type':user_type, 'user_name':user_name,
      'password':password, 'nickname':nickname, 'birth_year':birth_year,
      'gender':gender, 'address':address, 'email':email, 'phone':phone};
    json = JSON.stringify(json);
    var msg = "Would you sign up the new user as below : \n\n" + json;
    if (!confirm(msg)) {
      return;
    }
    initHttp();
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", "/user", true);
    // Only post method needs to set header
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    // Set callback
    httpRequest.onreadystatechange = saveResult;
    httpRequest.send(json);
  }

  // Save Callback
  function saveResult() {
    // Check 4 : data received
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        result.innerText = httpRequest.responseText;
      } else {
        result.innerText = httpRequest.status;
      }
    }
  }

  // Get path
  function getPath(){
    var path = document.location.pathname;
    var index = path.indexOf("/", 1);
    return path.substring(0, index);
  }

</script>

