<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<HTML>
<title>Quzi Data Management</title>
<label>Quzi Data Management
       &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp
       &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp
       Please input the URL : </label>
<input type="text" id="url" value="/user" />
<div style="width:100%; margin:auto; overflow:auto;">

  <!-- Remove the float:right; -->
  <div style="width:99%; background:#EEE">
    <label> Please input the query condition : </label><br>
    <textarea rows="2" cols="200" id="text_query">
    {"act"="select", "id1"=1, "id2"=100}
    </textarea><br>
    <input type="button" onclick="queryData()" value="Query Data">
    <label> * </label><br>
    <textarea rows="10" cols="200" id="result_query"></textarea>
  </div>

  <div style="width:99%; background:#EEE">
    <textarea rows="9" cols="200" id="text_add" >
    {"act"="insert", "data"=[
       {"user_id"=0, "user_name"="刘备",  "password"="111", "email"="", "phone"="", "address"="", "token"=""},
       {"user_id"=0, "user_name"="关羽",  "password"="222", "email"="", "phone"="", "address"="", "token"=""},
       {"user_id"=0, "user_name"="张飞",  "password"="333", "email"="", "phone"="", "address"="", "token"=""},
       {"user_id"=0, "user_name"="赵云",  "password"="444", "email"="", "phone"="", "address"="", "token"=""},
       {"user_id"=0, "user_name"="诸葛亮", "password"="555", "email"="", "phone"="", "address"="", "token"=""}
    ]}
    </textarea><br>
    <input type="button" onclick="addData()" value="Add Data">
    <label id="result_add" > * </label>
    <hr>

    <textarea rows="6" cols="200">
    {"act"="update", "data"=[
       {"user_id"=1, "user_name"="刘备",  "password"="111", "email"="", "phone"="", "address"="", "token"=""},
       {"user_id"=2, "user_name"="关羽",  "password"="222", "email"="", "phone"="", "address"="", "token"=""}
    ]}
    </textarea><br>
    <input type="button" onclick="updateData()"  value="Update Data">
    <label id="result_update" > * </label>
    <hr>

    <textarea rows="3" cols="200">
    {"act"="delete", "id1"=3, "id2"=10}
    </textarea><br>
    <input type="button" onclick="deleteData()" value="Delete Data">
    <label id="result_delete" > * </label>
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
    margin: 1px 3px;
    padding: 1px;
    text-align: left;
    vertical-align: top;
  }

  input[type="button"]{
    margin: 1px 3px;
    width: 100px;
    vertical-align: top;
  }

  input[id="url"]{
    margin: 1px 3px;
    width: 500px;
    vertical-align: top;
  }

  textarea{
    margin: 1px 3px;
    vertical-align: top;
  }

</style>

<script type="text/javascript">
  var httpRequest = null;
  var url = document.getElementById("url");
  var text_query = document.getElementById("text_query");
  var text_add = document.getElementById("text_add");
  var result_add = document.getElementById("result_add");
  var result_query = document.getElementById("result_query");

  // Initiate http
  function initHttp() {
    if (httpRequest != null) {
      result_add.innerText = "reuse http object"
    } else if (window.XMLHttpRequest) { //IE6 above and other browser
      httpRequest = new XMLHttpRequest()
    } else if(window.ActiveXObject) { //IE6 and lower
      httpRequest = new ActiveXObject();
    }
  }

  // Add data
  function addData() {
    initHttp();
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", url.value, true);
    // Only post method needs to set header
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    // Set callback
    httpRequest.onreadystatechange = response_add;
    httpRequest.send(text_add.value);
  }

  // Callback
  function response_add() {
    // Check 4 : data received
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        result_add.innerText = httpRequest.responseText;
      } else {
        result_add.innerText = httpRequest.status
      }
    }
  }

  // Query data
  function queryData() {
    initHttp();
    httpRequest.open("POST", url.value, true);
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    httpRequest.onreadystatechange = response_query;
    httpRequest.send(text_query.value);
  }

  // Callback
  function response_query() {
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        result_query.innerText = httpRequest.responseText;
      } else {
        result_query.innerText = httpRequest.status
      }
    }
  }

</script>

