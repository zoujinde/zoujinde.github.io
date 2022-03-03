<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign Up</title>
<script type="text/javascript" src="head.jsp"></script>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
<form id="form">
  <label>User type</label>
  <select name="user_type">
    <option value="1">Volunteer</option>
    <option value="2">Parents</option>
    <option value="3">Participant</option>
  </select><br>
  <label>User name </label><input name="user_name"/><br>
  <label>Password  </label><input type="password" name="password"/><br>
  <label>Nickname  </label><input name="nickname"/><br>
  <label>Birth year</label><input type="number" name="birth_year"/><br>
  <label>Gender    </label>
  <select name="gender">
    <option value="1">Male</option>
    <option value="0">Female</option>
  </select><br>
  <label>Address  </label><input name="address"/><br>
  <label>Email    </label><input name="email"/><br>
  <label>Phone    </label><input name="phone"/>
  <hr>
  <input type="button" onclick="save()" value="Save" style="width:900px;"/>
  <label id="result" style="width:900px;font-size:30px;"/>
</form>
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
    margin: 5px 5px;
    padding: 1px;
    width: 220px;
    font-size:50px;
    text-align: left;
    vertical-align: top;
  }

  input{
    margin: 5px 10px;
    width: 680px;
    font-size:50px;
    vertical-align: top;
  }

  select{
    margin: 5px 1px;
    width: 675px;
    font-size:50px;
    vertical-align: top;
  }

</style>

<script type="text/javascript">
  var httpRequest = null;

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

  // Get JSON from form data
  function getJson(data) {
    var json = {};
    data.forEach((value, key) => {
      json[key] = value.trim(); // or data.getAll(key)
    });
    return json;
  }

  // Save data
  function save() {
    // Check the data
    var user_name = document.getElementsByName("user_name")[0].value.trim();
    if (user_name.length < 3) {
      alert("Please input the user name. (length>=3)");
      return;
    }
    var password  = document.getElementsByName("password")[0].value.trim();
    if (password.length < 3) {
      alert("Please input the password. (length>=3)");
      return;
    }
    var page_user_id = document.getElementById('page_user_id').value;
    var data = new FormData(document.getElementById("form"));
    var json = getJson(data);
    json['act'] = 'signUp';
    json['parent_id'] = page_user_id;
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
      var result = document.getElementById("result");
      if(httpRequest.status==200) { // 200 OK
        result.innerText = httpRequest.responseText;
      } else {
        result.innerText = httpRequest.status;
      }
    }
  }

</script>

