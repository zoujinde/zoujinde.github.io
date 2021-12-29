<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<HTML>
<title>User Data Management</title>
<H3>User Data Management</H3>
<div style="width:100%; margin:auto; overflow:auto;">
  <div style="width:50%; float:right; background:#EEE">
    <label> Please input the condition to query user data : </label><br>
    <textarea rows="2" cols="80">{"id_begin"=1, "id_end"=100}
    </textarea>
    <input type="button" onclick="addUser()" value="Query Data">
    <br>
    <label> Result : </label><br>
    <textarea rows="28" cols="80"></textarea>
    <hr>
  </div>

  <div style="width:50%; background:#EEE">
    <label>Add user data : </label><br>
    <textarea rows="10" cols="80">[{"name"="张飞"},{"name"="赵云"}]
    </textarea>
    <input type="button" onclick="addUser()" value="Add Data">
    <br>
    <label> Result : </label>
    <hr>

    <label>Update user data : </label><br>
    <textarea rows="5" cols="80">{"id"=1, "name"="刘备"}
    </textarea>
    <input type="button" onclick="addUser()"  value="Update Data">
    <br>
    <label> Result : </label>
    <hr>

    <label>Delete user data : </label><br>
    <textarea rows="5" cols="80">{"id"=2, "name"="关羽"}
    </textarea>
    <input type="button" onclick="addUser()" value="Delete Data">
    <br>
    <label> Result : </label>
    <hr>

  </div>

</div>
  
</HTML>

<style>
  div{
    border-style:solid;
    border-width:3px;
    border-color:#999999
  }

  label{
    cursor: pointer;
    display: inline-block;
    margin: 3px 3px;
    padding: 1px;
    text-align: left;
    vertical-align: top;
  }

  input{
    margin: 3px 3px;
    width: 100px;
    vertical-align: top;
  }

  textarea{
    margin: 3px 3px;
    vertical-align: top;
  }

</style>

<script type="text/javascript">
  var httpRequest = null;
  var div = document.getElementById("result");

  // Add a new user
  function addUser() {
    if (httpRequest != null) {
      //div.innerText = "reuse the request object"
    } else if (window.XMLHttpRequest) { //IE6 above and other browser
      httpRequest = new XMLHttpRequest()
    } else if(window.ActiveXObject) { //IE6 and lower
      httpRequest = new ActiveXObject();
    }

    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", "/user", true);
    // Only post method needs to set header
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    // Set callback
    httpRequest.onreadystatechange = response;
    var name = document.getElementById("username").value;
    httpRequest.send("username=" + name);
  }

  // Callback
  function response() {
    // Check 4 : data received
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        var text = httpRequest.responseText;
        div.innerText = text;
      } else {
        div.innerText = httpRequest.status
      }
    }
  }
</script>

