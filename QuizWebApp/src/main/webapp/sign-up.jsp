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
      <th width="350" >student name</th>
      <th width="350" >password</th>
      <th width="220"><input type="button" value="add student" onclick="addChild(null)" style="width:260px;"/></th>
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
  <input type="button" onclick="save()" value="Save" style="width:600px;"/>
  <input type="button" onclick="deleteCurrentUser()" value="Delete" style="width:280px;"/>
  <hr style="font-size:1px;">
  <label id="result" style="width:910px;"/>
</form>
</div>
</HTML>

<script type="text/javascript">
  const m_http = getHttpRequest();
  const m_action = getUrlValue("act");
  const m_label_type = document.getElementById("label_user_type");
  const m_user_type = document.getElementsByName("user_type")[0];
  const m_user_name = document.getElementsByName("user_name")[0];
  const m_password  = document.getElementsByName("password")[0];
  const m_email  = document.getElementsByName("email")[0];
  const m_result = document.getElementById("result");
  const m_table = document.getElementById("children");
  var m_child_index = 0;

  // Delay load
  setTimeout("load()", 1);

  // Load
  function load() {
    var label_top = document.getElementById("label_top");
    if (m_action == "create") { // create a new user
      label_top.innerText = "Please input the new user info :";
      m_user_type.value = "2"; // Set the default type as 2 : PARENTS
      addChild(null);
    } else { // modify current user
      label_top.innerText = "Modify the current user info :";
      m_label_type.style.width = "900px";
      m_user_type.style.display = "none";
      m_user_name.disabled = "true";
      // Get current user data when user_id is 0
      var json = '{"act":"getUser", "user_id":0}';
      m_http.open("POST", "user", true);
      m_http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
      m_http.onreadystatechange = loadResult;
      m_http.send(json);
    }
  }

  // Load result
  function loadResult() {
    if (m_http.readyState==4) {
      var text = m_http.responseText;
      if(m_http.status==200) { // 200 OK
        if (text.startsWith("{")) {
          var users = JSON.parse(text)["users"];
          // Set UI data
          var json = users[0];
          m_label_type.innerText = json["token"];
          m_user_type.value = json["user_type"];
          m_user_name.value = json["user_name"];
          m_password.value  = json["password"];
          m_email.value     = json["email"];
          document.getElementsByName("nickname")[0].value   = json["nickname"];
          document.getElementsByName("birth_year")[0].value = json["birth_year"];
          document.getElementsByName("gender")[0].value     = json["gender"];
          var a = json["address"].split(",");
          document.getElementsByName("address")[0].value = a[0];
          if (a.length == 4) {
            document.getElementsByName("city")[0].value    = a[1];
            document.getElementsByName("state")[0].value   = a[2];
            document.getElementsByName("zip")[0].value     = a[3];
          }
          var phone = json["phone"];
          //alert("phone=" + phone + " : " + phone.substr(100,200))
          document.getElementsByName("phone1")[0].value = phone.substring(0, 3);
          document.getElementsByName("phone2")[0].value = phone.substring(3, 6);
          document.getElementsByName("phone3")[0].value = phone.substring(6, 10);
          // Show children table when user_type is 2 : PARENT
          if (json["user_type"] == 2) {
            for (var i = 1; i < users.length; i++) {
              addChild(users[i]);
            }
          } else { // Hide children table
            m_table.style.display = "none";
          }
        } else {
          m_result.innerText = text;
          alert(text);
        }
      } else {
        text = m_http.status + text;
        m_result.innerText = text;
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
    if (m_user_name.value.includes(" ")) {
      alert("User name can't include space");
      return;
    }
    if (m_user_name.value.trim().length < 6) {
      alert("User name length should >= 6");
      return;
    }
    if (m_password.value.trim().length < 6) {
      alert("Password length should >= 6");
      return;
    }
    var data = new FormData(document.getElementById("form"));
    var json = getJson(data);
    json['phone'] = json['phone1'] + json['phone2'] + json['phone3']
    json['address'] = json['address'] + ',' + json['city'] + ',' + json['state'] + ',' + json['zip']

    // Set the request data
    var request = {"act":"addUser", "users":[json]};
    if (m_action == "modify") {
        request = {"act":"setUser", "users":[json]};
    }

    // If user type is parents, then add children data
    if (m_user_type.value == "2") { // parents
      var child_name = document.getElementsByClassName("child_name");
      var child_pass = document.getElementsByClassName("child_pass");
      var size = child_name.length;
      for (var i = 0; i < size; i++) {
        // alert("child_name[i].disabled=" + child_name[i].disabled);
        if (child_name[i].disabled) {
          // Can't modify the used data
        } else {
          var name = child_name[i].value.trim();
          var pass = child_pass[i].value.trim();
          var user_id = child_name[i].parentNode.parentNode.alt;
          if (name.includes(" ")) {
            alert("Student name can't include space");
            return;
          }
          if (name.length < 6 || pass.length < 6) {
            alert("Student name and password length should >= 6");
            return;
          }
          request["users"][i + 1] = {"user_id":user_id, "user_type":3, "user_name":name, "password":pass};
        }
      }
    }

    request = JSON.stringify(request);
    // alert(request);
    m_http.open("POST", "user", true);
    m_http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    m_http.onreadystatechange = saveResult;
    m_http.send(request);
  }

  // Save Callback
  function saveResult() {
    if(m_http.readyState==4) {
      var text = m_http.responseText;
      if(m_http.status==200) { // 200 OK
        if (text == 'OK') {
            text = "Save user data OK.";
            if (m_action == "create") { // create a new user
              window.location.href = "sign-in.jsp?act=input&user=" + m_user_name.value.trim();
            }
        }
        m_result.innerText = text;
        alert(text);
      } else {
        text = m_http.status + text;
        m_result.innerText = text;
        alert(text);
      }
    }
  }

  // On user type change
  function onUserTypeChange() {
    if (m_user_type.value == '1') { // volunteer
      m_table.style.display = "none";
    } else { // parents
      m_table.style.display = "block";
    }
  }

  // Add child row
  function addChild(json) {
    var row = m_table.insertRow();
    var c1 = row.insertCell();
    var c2 = row.insertCell();
    var c3 = row.insertCell();
    c1.innerHTML = '<input class="child_name" style="width:300px;"/>';
    c2.innerHTML = '<input type="password" class="child_pass" style="width:300px;"/>';
    c3.innerHTML = '<input type="button" value="delete" onclick="deleteChild(this)" style="width:260px;"/>';
    var name = c1.children[0];
    var pass = c2.children[0];
    var button = c3.children[0];
    row.alt = 0; // Set new user_id = 0
    if (json != null) {
      row.alt    = json["user_id"];
      name.value = json["user_name"];
      pass.value = json["password"];
      // If user set data, then can't modify or delete the used data 
      if (json["email"]!="" || json["phone"]!="") {
        name.disabled = "true";
        pass.disabled = "true";
        button.value  = "used";
      }
    }
  }

  // Delete child row
  function deleteChild(button) {
    if (m_table.rows.length <= 2) {
      alert("Can't delete the only 1 student data.");
    } else if (button.value == "delete") {
      var row = button.parentNode.parentNode;
      var index = row.rowIndex;
      var user_id = row.alt;
      if (confirm("Would you like to delete the student data?")) {
        // If it is a new child data, delete it directly.
        if (user_id == "0") {
          m_table.deleteRow(index);
        } else { // Old data, send request to server.
          m_child_index = index;
          deleteUser(user_id);
        }
      }
    } else {
      alert("Can't delete or modify the used data.");
    }
  }

  // Delete user data
  function deleteUser(user_id) {
    var json = {"act":"deleteUser", "user_id":user_id};
    json = JSON.stringify(json);
    m_http.open("POST", "user", true);
    m_http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    m_http.onreadystatechange = deleteResult;
    m_http.send(json);
  }

  // Delete result
  function deleteResult() {
    if (m_http.readyState==4) {
      var text = m_http.responseText;
      if (m_http.status==200) { // 200 OK
        if (text == 'OK') {
          m_table.deleteRow(m_child_index);
        }
        m_result.innerText = text;
        alert(text);
      } else {
        text = m_http.status + text;
        m_result.innerText = text;
        alert(text);
      }
    }
  }

  // Delete current user
  function deleteCurrentUser() {
    if (m_action == "modify") {
      if (confirm("Would you like to delete the current user : " + m_user_name.value)) {
        var json = {"act":"deleteUser", "user_id":0}; // 0 means to delete current user
        json = JSON.stringify(json);
        m_http.open("POST", "user", true);
        m_http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        m_http.onreadystatechange = deleteCurrentUserResult;
        m_http.send(json);
      }
    } else {
      alert("No need to delete the unsaved user.");
    }
  }

  // Delete current user result
  function deleteCurrentUserResult() {
    if (m_http.readyState==4) {
      var text = m_http.responseText;
      if (m_http.status==200) { // 200 OK
        if (text == 'OK') {
            text = "User is deleted";
            window.location.href = "sign-up.jsp?act=create";
        }
        m_result.innerText = text;
        alert(text);
      } else {
        text = m_http.status + text;
        m_result.innerText = text;
        alert(text);
      }
    }
  }

</script>
