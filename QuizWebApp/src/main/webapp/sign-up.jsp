<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign Up</title>
<%@ include file="head.jsp"%>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
<form id="form">
  <label style="width:900px;font-weight:bold;">Please input the new user info : </label><br>
  <label>User type</label>
  <select name="user_type">
    <option value="1">Volunteer</option>
    <option value="2">Parents</option>
    <option value="3">Participant</option>
  </select><br>
  <label>User name </label><input name="user_name"/><br>
  <label>Password  </label><input type="password" name="password"/><br>
  <label>Email    </label><input name="email"/><br>
  <label style="width:900px;font-weight:bold;">The following items are optional : </label><br>
  <label>Nickname  </label><input name="nickname"/><br>
  <label>Birth year</label><input type="number" name="birth_year"/><br>
  <label>Gender    </label>
  <select name="gender">
    <option value="1">Male</option>
    <option value="0">Female</option>
  </select><br>
  <label>Address  </label><input name="address"/><br>
  <label>City</label><input name="city"/><br>
  <label>State</label><input name="state" style="width:360px;"/>
  <label style="width:50px;">ZIP</label><input name="zip" style="width:230px;"/><br>
  <label>Phone    </label><label style="width:1px;">(</label>
  <input name="phone1" maxlength="3" style="width:100px;"/><label style="width:1px;">)</label>
  <input name="phone2" maxlength="3" style="width:100px;"/><label style="width:1px;">-</label>
  <input name="phone3" maxlength="5" style="width:150px;"/><br>
  <br>
  <input type="button" onclick="save()" value="Save" style="width:910px;"/>
  <hr style="font-size:1px;">
  <label id="result" style="width:900px;"/>
</form>
</div>
</HTML>

<script type="text/javascript">
  var httpRequest = getHttpRequest();

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
    if (user_name.length < 6) {
      alert("Please input the user name. (length>=6)");
      return;
    }
    var password  = document.getElementsByName("password")[0].value.trim();
    if (password.length < 6) {
      alert("Please input the password. (length>=6)");
      return;
    }
    var email  = document.getElementsByName("email")[0].value.trim();
    if (email.indexOf("@") < 1 || email.indexOf(".com") < 5) {
      alert("Please input the valid email such as xxx@xxx.com");
      return;
    }
    var data = new FormData(document.getElementById("form"));
    var json = getJson(data);
    json['act'] = 'signUp';
    json['phone'] = json['phone1'] + json['phone2'] + json['phone3']
    json['address'] = json['address'] + ',' + json['city'] + ',' + json['state'] + ',' + json['zip']
    json = JSON.stringify(json);
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", "user", true);
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
        var text = httpRequest.responseText;
        if (text == 'OK') {
          text = 'Sign up OK. Please click "Sign In" button to sign in.';
        }
        result.innerText = text;
      } else {
        result.innerText = httpRequest.status;
      }
    }
  }

</script>
