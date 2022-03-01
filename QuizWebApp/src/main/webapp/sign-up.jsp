<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign Up</title>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
  <H1> &nbsp &nbsp &nbsp Sign Up</H1>
  <hr>
  <label>User name</label><input id="text_user"/>
  <br>
  <label>Password </label><input type="password" id="text_pass"/>
  <br>
  <input type="button" onclick="save()" value="Save"/>
  <hr>
  <label id="result" style="width:600px;font-size:30px;"/>
</div>
</HTML>

<style>
  div{
    border-style:solid;
    border-width:1px;
    border-color:#999999;
    font-size:30px;
  }

  label{
    cursor: pointer;
    display: inline-block;
    margin: 10px 10px;
    padding: 1px;
    width: 300px;
    font-size:30px;
    text-align: left;
    vertical-align: top;
  }

  input{
    margin: 10px 10px;
    width: 500px;
    font-size:30px;
    vertical-align: top;
  }

  input[type="button"]{
    width: 300px;
  }

  select[id="user_type"]{
    margin: 10px 10px;
    width: 200px;
    height: 30px
    vertical-align: top;
  }

  textarea{
    margin: 10px 10px;
    vertical-align: top;
  }

</style>

<script type="text/javascript">
  var httpRequest = null;
  var select_url = document.getElementById("url");
  var text_query = document.getElementById("text_query");
  var text_data = document.getElementById("text_data");
  var result_query = document.getElementById("result_query");
  var result_state = document.getElementById("state");
  var result_data = document.getElementById("result_data");

  // Initiate http
  function initHttp() {
    if (httpRequest != null) {
      result_state.innerText = "reuse http object"
    } else if (window.XMLHttpRequest) { //IE6 above and other browser
      httpRequest = new XMLHttpRequest()
    } else if(window.ActiveXObject) { //IE6 and lower
      httpRequest = new ActiveXObject();
    }
  }

  // Save data
  function save() {
    result_data.innerText = '*';
    // Check the data
    var data = text_data.value.trim();
    if (data.startsWith('{') && data.endsWith('}')) {
      //data is OK
    } else {
      // The result_data is a label, the result_data.value = 'xxx' can't work
      result_data.innerText = 'Invalid Data';
      return;
    }
    // Confirm the request
    var path = getPath() + select_url.value;
    var msg = "Would you submit to " + path + "\n\n" + data;
    if (!confirm(msg)) {
      return;
    }
    initHttp();
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", path, true);
    // Only post method needs to set header
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    // Set callback
    httpRequest.onreadystatechange = submitResult;
    httpRequest.send(data);
  }

  // Save Callback
  function saveResult() {
    // Check 4 : data received
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        result_data.innerText = httpRequest.responseText;
      } else {
        result_data.innerText = httpRequest.status;
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

