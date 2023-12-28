<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign Up</title>
<%@ include file="head.jsp"%>
<div style="width:100%; margin:auto; overflow:auto;">
<form id="form">
  <label id="label_top" style="width:900px;font-weight:bold;">Please input the new user info : </label><br>
  <label id="label_user_type">User type</label>
  <select name="user_type" onchange="onUserTypeChange()">
    <option value="1">Volunteer</option>
    <option value="2">Parents</option>
  </select><br>
  <label>User name </label><input name="user_name"/><br>
  <label>Password  </label><input type="password" name="password"/><br>

  <table id="children" border="1" style="display:block;width:920px;" >
    <tr>
      <th width="350" >Child Name</th>
      <th width="350" >Password</th>
      <th width="220"><input type="button" value="add child" onclick="addChild()" style="width:220px;"/></th>
    </tr>
  </table>

  <label style="width:900px;font-weight:bold;">The following items are optional : </label><br>
  <label>Email    </label><input name="email"/><br>
  <label>Nickname  </label><input name="nickname"/><br>
  <label>Birth year</label><input type="number" name="birth_year" style="width:200px;"/>
  <label style="width:160px;" >Gender    </label>
  <select name="gender" style="width:280px;">
    <option value="1">Male</option>
    <option value="0">Female</option>
  </select><br>
  <label>Address  </label><input name="address"/><br>
  <label>City</label><input name="city"/><br>
  <label>State</label><input name="state" style="width:360px;"/>
  <label style="width:70px;">ZIP</label><input name="zip" style="width:210px;"/><br>
  <label>Phone    </label><label style="width:1px;">(</label>
  <input name="phone1" maxlength="3" style="width:100px;"/><label style="width:1px;">)</label>
  <input name="phone2" maxlength="3" style="width:100px;"/><label style="width:1px;">-</label>
  <input name="phone3" maxlength="5" style="width:150px;"/><br>
  <br>
  <input type="button" onclick="save()" value="Save" style="width:910px;"/>
  <hr style="font-size:1px;">
  <label id="result" style="width:910px;"/>
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
  var result = document.getElementById("result");

  // Delay load
  setTimeout("load()", 1);

  // Load
  function load() {
    var label_top = document.getElementById("label_top");
    if (action == "create") { // create a new user
      label_top.innerText = "Please input the new user info :";
      user_type.value = "2"; // set parents type
      addChild();
    } else { // modify current user
      label_top.innerText = "Modify the current user info :";
      var label_user_type = document.getElementById("label_user_type");
      label_user_type.style.width = "900px";
      label_user_type.style.fontSize = "39px";
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
      var text = "Please input the user name. (length>=6)";
      result.innerText = text;
      alert(text);
      return;
    }
    if (password.value.trim().length < 6) {
      var text = "Please input the password. (length>=6)";
      result.innerText = text;
      alert(text);
      return;
    }
    var data = new FormData(document.getElementById("form"));
    var json = getJson(data);
    json['phone'] = json['phone1'] + json['phone2'] + json['phone3']
    json['address'] = json['address'] + ',' + json['city'] + ',' + json['state'] + ',' + json['zip']

    // Set the request data
    var request = {"act":"addUser", "users":[json]};
    if (action == "modify") {
        request = {"act":"setUser", "users":[json]};
    }

    // If user type is parents, then add children data
    if (user_type.value == "2") { // parents
      var child_name = document.getElementsByName("child_name");
      var child_pass = document.getElementsByName("child_pass");
      var size = child_name.length;
      for (var i = 0; i < size; i++) {
        var name = child_name[i].value.trim();
        var pass = child_pass[i].value.trim();
        if (name.length < 6 || pass.length < 6) {
          alert("Please input child name and password : length >= 6");
          return;
        }
        request["users"][i + 1] = {"user_type":"3", "user_name":name, "password":pass};
      }
    }

    request = JSON.stringify(request);
    alert(request);
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", "user", true);
    // Only post method needs to set header
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    // Set callback
    httpRequest.onreadystatechange = saveResult;
    httpRequest.send(request);
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
        result.innerText = text;
        alert(text);
      } else {
        text = httpRequest.status + text;
        result.innerText = text;
        alert(text);
      }
    }
  }

  // On user type change
  function onUserTypeChange() {
    var table = document.getElementById("children");
    if (user_type.value == '1') { // volunteer
      table.style.display = "none";
    } else { // parents
      table.style.display = "block";
    }
  }

  // Add child row
  function addChild() {
    var table = document.getElementById("children");
    var row = table.insertRow();
    var c1 = row.insertCell();
    var c2 = row.insertCell();
    var c3 = row.insertCell();
    c1.innerHTML = '<input name="child_name" style="width:300px;"/>';
    c2.innerHTML = '<input type="password" name="child_pass" style="width:300px;"/>';
    c3.innerHTML = '<input type="button" value="delete" onclick="deleteChild(this)" style="width:220px;"/>';
  }

  // Delete child row
  function deleteChild(button) {
    var table = document.getElementById("children");
    if (table.rows.length <= 2) {
      alert("Can't delete the only 1 child.");
    } else {
      var index = button.parentNode.parentNode.rowIndex;
      if (confirm("Would you delete the child data?")) {
        // If it is a old child data, send request to server.
        // If it is a new child data, delete it directly.
        table.deleteRow(index);
      }
    }
  }

</script>
