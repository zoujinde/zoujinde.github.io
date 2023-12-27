<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign Up</title>
<%@ include file="head.jsp"%>
<div style="width:100%; margin:auto; overflow:auto;">
<form id="form">
  <label id="label_top" style="width:900px;font-weight:bold;">Please enter the new user info : </label><br>
  <label id="label_user_type">User type</label>
  <label>
    <select name="user_type">
      <option value="1">Volunteer</option>
      <option value="2">Guardian</option>
    </select>
  </label><br>
  <label>User name </label><label>
  <input name="user_name"/>
</label><br>
  <label>Password  </label><label>
  <input type="password" name="password"/>
</label><br>
  <label style="width:900px;font-weight:bold;"  id="reference_node">The following items are optional : </label><br>
  <label>Email    </label><label>
  <input name="email"/>
</label><br>
  <label>Nickname  </label><label>
  <input name="nickname"/>
</label><br>
  <label>Birth year</label><label>
  <input type="number" name="birth_year"/>
</label><br>
  <label>Gender    </label>
  <label>
    <select name="gender">
      <option value="1">Male</option>
      <option value="0">Female</option>
    </select>
  </label><br>
  <label>Address  </label><label>
  <input name="address"/>
</label><br>
  <label>City</label><label>
  <input name="city"/>
</label><br>
  <label>State</label><label>
  <input name="state" style="width:360px;"/>
</label>
  <label style="width:70px;">ZIP</label><label>
  <input name="zip" style="width:210px;"/>
</label><br>
  <label>Phone    </label><label style="width:1px;">(</label>
  <label>
    <input name="phone1" maxlength="3" style="width:100px;"/>
  </label><label style="width:1px;">)</label>
  <label>
    <input name="phone2" maxlength="3" style="width:100px;"/>
  </label><label style="width:1px;">-</label>
  <label>
    <input name="phone3" maxlength="5" style="width:150px;"/>
  </label><br>
  <br>
  <input type="button" onclick="save()" value="Save" style="width:910px;"/>
  <hr style="font-size:1px;">
  <label id="result" style="width:910px;"></label>
</form>
</div>
</HTML>

<script type="text/javascript">
  const httpRequest = getHttpRequest();
  const action = getUrlValue("act");
  const user_type = document.getElementsByName("user_type")[0];
  const user_name = document.getElementsByName("user_name")[0];
  const password  = document.getElementsByName("password")[0];
  const email  = document.getElementsByName("email")[0];
  const result = document.getElementById("result");

  // Delay load
  setTimeout("load()", 1);

  // Load
  function load() {
    const label_top = document.getElementById("label_top");
    if (action === "create") { // create a new user
      label_top.innerText = "Please input the new user info :";
      user_type.value = "3"; // Set participant to avoid wrong user type
    } else { // modify current user
      label_top.innerText = "Modify the current user info :";
      const label_user_type = document.getElementById("label_user_type");
      label_user_type.style.width = "900px";
      label_user_type.style.fontSize = "39px";
      user_type.style.display = "none";
      user_name.disabled = "true";
      // Get current user data when user_id is 0
      const json = '{"act":"getUser", "user_id":0}';
      httpRequest.open("POST", "user", true);
      httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
      httpRequest.onreadystatechange = loadResult;
      httpRequest.send(json);
    }
  }

  // Load result
  function loadResult() {
    if (httpRequest.readyState===4) {
      let text = httpRequest.responseText;
      if(httpRequest.status===200) { // 200 OK
        if (text.startsWith("{")) {
          const json = JSON.parse(text);
          // Set UI data
          let label_user_type;
          label_user_type.innerText = json["token"];
          user_name.value = json["user_name"];
          password.value  = json["password"];
          email.value     = json["email"];
          document.getElementsByName("nickname")[0].value   = json["nickname"];
          document.getElementsByName("birth_year")[0].value = json["birth_year"];
          document.getElementsByName("gender")[0].value     = json["gender"];
          const a = json["address"].split(",");
          //alert("address=" + a[0] + "&" + a[1] + "&" + a[2] + "&" + a[3])
          document.getElementsByName("address")[0].value = a[0];
          document.getElementsByName("city")[0].value    = a[1];
          document.getElementsByName("state")[0].value   = a[2];
          document.getElementsByName("zip")[0].value     = a[3];
          const phone = json["phone"];
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
    const json = {};
    data.forEach((value, key) => {
      json[key] = value.trim(); // or data.getAll(key)
    });
    return json;
  }

  // Save data
  function save() {
    let text;
  // Check the data
    if (user_name.value.trim().length < 6) {
      text = "Please input the user name. (length>=6)";
      result.innerText = text;
      alert(text);
      return;
    }
    if (password.value.trim().length < 6) {
      text = "Please input the password. (length>=6)";
      result.innerText = text;
      alert(text);
      return;
    }
    const data = new FormData(document.getElementById("form"));
    console.log(document.getElementById("form"))
    let json = getJson(data);
    if (action === "create") { // create new
      json = json_format_dealer('signUp', json)
    } else { // modify current user data
      json = json_format_dealer('setUser', json)
    }

    // debug log
    console.log(json)
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
    if(httpRequest.readyState===4) {
      let text = httpRequest.responseText;
      if(httpRequest.status===200) { // 200 OK
        if (text === 'OK') {
          if (action === "create") {
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



  document.addEventListener("DOMContentLoaded", function () {
    // 获取<select>元素
    const pageSelector = document.getElementsByName("user_type")[0]; // By Name返回的DOM List, 从中取出第一个DOM对象

    // 监听选择事件
    pageSelector.addEventListener("change", function () {
      console.log(pageSelector.value);

      if(pageSelector.value === '1'){ // Volunteer value 为 1
        GuardianDealer(true)
      }
      else if(pageSelector.value === '2'){ // Guardian value 为 2
        GuardianDealer(false)
      }
      else {
        console.warn("Received Invalid Input")
      }
    });
  });

  // 处理Guardian页面的DOM操作
  const GuardianDealer = function (isRemoveOperation) {
    const node = document.getElementById("form");
    const reference = document.getElementById("reference_node");

    if (!isRemoveOperation) {
      const newInputElement = document.createElement("div");
      newInputElement.id = "participant_box";

      node.insertBefore(newInputElement, reference);

      const participant_box = document.getElementById("participant_box");

      participant_box.innerHTML = '<div id="initial_box"><label id="label_top" style="width:900px;font-weight:bold;">Please enter the participants\' info : </label>'
              + '<input name="participantUsername" placeholder="participant 1 username"></label> <br>'
              + '<input type="password" name="participantPassword" placeholder="Password (Enter to Add More)"></label></div>';

      let i = 0;
      let password_entry_box = document.getElementsByName("participantPassword")[i];

      // 绑定事件监听器
      password_entry_box.addEventListener("keypress", onPasswordEntryKeypress);

      function onPasswordEntryKeypress(event) {
        if (event.key === "Enter") {
          event.preventDefault();

          const newKidNode = document.createElement("div");
          newKidNode.innerHTML = '<input name="participantUsername" placeholder="participant ' + (i+2) + ' username"></label>'
                  + '<input type="password" name="participantPassword" placeholder="Password (Enter to Add More)"></label></div>';

          participant_box.appendChild(newKidNode);
          i++;
          password_entry_box = document.getElementsByName("participantPassword")[i];

          // 移除旧的事件监听器
          password_entry_box.removeEventListener("keypress", onPasswordEntryKeypress);

          // 为新的password_entry_box绑定新的事件监听器
          password_entry_box.addEventListener("keypress", onPasswordEntryKeypress);
        }
      }
    } else if (isRemoveOperation) {
      const nodeToRemove = document.getElementById("participant_box");

      if (nodeToRemove) {
        nodeToRemove.parentNode.removeChild(nodeToRemove);
      }
    }
  };


  const json_format_dealer = function (act, guardian_user){
    let json = {"act" : act,
    "users" : [
            JSON.stringify(guardian_user)]
    };

    const UsernameContainer = document.getElementsByName("participantUsername")
    const PasswordContainer = document.getElementsByName("participantUsername")

    console.log(UsernameContainer)
    console.log(PasswordContainer)
    let i = 0;

    if(guardian_user['user_type'] === "1"){
      UsernameContainer.forEach( args => {
        json['users'].push(JSON.stringify({"user_type" : "3", "user_name":args.value, "password" : PasswordContainer[i].value}))
        i++;
      })}

    return json;

  }


  /* When users array only has 1 row, the user_type must be 1 : VOLUNTEER
  {
    "act":"signUp",
    "users":[
      {"user_type":"1","user_name":"zoujinde","password":"11111111","email":"","nickname":"",,,,,,"phone":""}
    ]
  }
  */

  /* When users array has 2 or more rows,
   * The 1st user_type must be 2          : Guardian PARENTS
   * The 2nd and more user_type must be 3 : Child PARTICIPANT
  {
    "act":"signUp",
    "users":[
      {"user_type":"2","user_name":"Guardian", "password":"xxx","email":"","nickname":"",,,,,,"phone":""},
      {"user_type":"3","user_name":"Child 1",  "password":"xxx"},
      {"user_type":"3","user_name":"Child 2",  "password":"xxx"},
    ]
  }
  */
  /*
  Previous :

  {"user_type":"1","user_name":"charlie","password":"Vof612791","email":"wan2901@dcds.edu","nickname":"Charlie","birth_year":"20050826","gender":"1","address":"1558 Greenwich,Troy,Michigan,48084","city":"Troy","state":"Michigan","zip":"48084","phone1":"123","phone2":"123","phone3":"1234","act":"signUp","phone":"1231231234"}
   */
  class User {
    constructor(userType, userName, password, email, nickname, birthYear, gender, address, city, state, zip, phone1, phone2, phone3, act, phone) {
      this.user_type = userType;
      this.user_name = userName;
      this.password = password;
      this.email = email;
      this.nickname = nickname;
      this.birth_year = birthYear;
      this.gender = gender;
      this.address = address;
      this.city = city;
      this.state = state;
      this.zip = zip;
      this.phone1 = phone1;
      this.phone2 = phone2;
      this.phone3 = phone3;
      this.act = act;
      this.phone = phone;
    }
  }




</script>
