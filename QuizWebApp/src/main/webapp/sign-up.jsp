<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign Up</title>
<%@ include file="head.jsp"%>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
<form id="form">
  <label id="label_top" style="width:900px;font-weight:bold;">Please input the new user info : </label><br>
  <label id="label_user_type">User type</label>
  <select name="user_type">
    <option value="1">Volunteer</option>
    <option value="2">Parents</option>
    <option value="3">Participant</option>
  </select><br>
  <label>User name </label><input name="user_name"/><br>
  <label>Password  </label><input type="password" name="password"/><br>
  <label style="width:900px;font-weight:bold;">The following items are optional : </label><br>
  <label>Email    </label><input name="email"/><br>
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
</form>
</div>
</HTML>

<script type="text/javascript">
  var httpRequest = getHttpRequest();
  var action = getUrlValue("act");
  var user_type = document.getElementsByName("user_type")[0];
  var user_name = document.getElementsByName("user_name")[0];
  var password  = document.getElementsByName("password")[0];
  var email  = document.getElementsByName("email")[0];

  // Delay load
  setTimeout("load()", 1);

  // Load
  function load() {
    var label_top = document.getElementById("label_top");
    if (action == "create") { // create a new user
      label_top.innerText = "Please input the new user info :";
    } else { // modify current user
      label_top.innerText = "Modify the current user info :";
      var label_user_type = document.getElementById("label_user_type");
      label_user_type.style.width = "900px";
      user_type.style.display = "none";
      user_name.disabled = "true";
      // Get current user data when user_id is 0
      var json = '{"act":"getUser", "user_id":0}';
      httpRequest.open("POST", "user", true);
      httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
      httpRequest.onreadystatechange = loadResult;
      httpRequest.send(json);
    }
  }

  // Load result
  function loadResult() {
    if (httpRequest.readyState==4) {
      var text = httpRequest.responseText;
      if(httpRequest.status==200) { // 200 OK
        if (text.startsWith("{")) {
          var json = JSON.parse(text);
          // Set UI data
          label_user_type.innerText = json["token"];
          user_name.value = json["user_name"];
          password.value  = json["password"];
          email.value     = json["email"];
          document.getElementsByName("nickname")[0].value   = json["nickname"];
          document.getElementsByName("birth_year")[0].value = json["birth_year"];
          document.getElementsByName("gender")[0].value     = json["gender"];
          var a = json["address"].split(",");
          //alert("address=" + a[0] + "&" + a[1] + "&" + a[2] + "&" + a[3])
          document.getElementsByName("address")[0].value = a[0];
          document.getElementsByName("city")[0].value    = a[1];
          document.getElementsByName("state")[0].value   = a[2];
          document.getElementsByName("zip")[0].value     = a[3];
          var phone = json["phone"];
          //alert("phone=" + phone + " : " + phone.substr(100,200))
          document.getElementsByName("phone1")[0].value = phone.substring(0, 3);
          document.getElementsByName("phone2")[0].value = phone.substring(3, 6);
          document.getElementsByName("phone3")[0].value = phone.substring(6, 10);
        } else {
          alert(text);
        }
      } else {
        alert(httpRequest.status + text);
      }
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
    if (user_name.value.trim().length < 6) {
      alert("Please input the user name. (length>=6)");
      return;
    }
    if (password.value.trim().length < 6) {
      alert("Please input the password. (length>=6)");
      return;
    }
    var data = new FormData(document.getElementById("form"));
    var json = getJson(data);
    if (action == "create") { // create new user
      json['act'] = 'signUp';
    } else { // modify current user data
      json['act'] = 'setUser';
    }
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
    if(httpRequest.readyState==4) {
      var text = httpRequest.responseText;
      if(httpRequest.status==200) { // 200 OK
        if (text == 'OK') {
          if (action == "create") {
            text = 'Sign up new user OK. Please Sign In.';
          } else {
            text = "Save user data OK.";
          }
        }
        alert(text);
      } else {
        alert(httpRequest.status + text);
      }
    }
  }

</script>
