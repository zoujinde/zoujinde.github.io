<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<HTML>
<title>Quzi Data Management</title>
<label>Quzi Data Management
       &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp
       Please input the URL : </label>
<input type="text" id="url" value="/user" />
<div style="width:100%; margin:auto; overflow:auto;">

  <!-- Remove the float:right; -->
  <div style="width:99%; background:#EEE">
    <label> Please input the query condition : </label>
    <textarea rows="2" cols="80" id="text_query">{"act":"select", "user_id_range":"1-100"}</textarea>
    <input type="button" onclick="queryData()" value="Query Data">
    <label id="state"> * </label><br>
    <!-- The textarea can't wrap line, so use DIV to wrap line
    <textarea rows="16" cols="200" id="result_query"></textarea>
    DIV don't need add white-space:pre-wrap;
    DIV add overfolow:auto; to show scroll bar-->
    <div id="result_query" style="height:36%; overflow:auto;"></div> 
  </div>

  <div style="width:99%; background:#EEE">
    <textarea rows="9" cols="200" id="text_add" >
    {"act":"insert", "data":[
       {"user_id":0, "user_name":"刘备",  "password":"111", "email":"", "phone":"", "address":"", "token":""},
       {"user_id":0, "user_name":"关羽",  "password":"222", "email":"", "phone":"", "address":"", "token":""},
       {"user_id":0, "user_name":"张飞",  "password":"333", "email":"", "phone":"", "address":"", "token":""},
       {"user_id":0, "user_name":"赵云",  "password":"444", "email":"", "phone":"", "address":"", "token":""},
       {"user_id":0, "user_name":"孔明",  "password":"555", "email":"", "phone":"", "address":"", "token":""}
    ]}
    </textarea><br>
    <input type="button" onclick="addData()" value="Add Data">
    <label id="result_add" > * </label>
    <hr>

    <textarea rows="6" cols="200" id="text_update">
    {"act":"update", "data":[
       {"user_id":1, "user_name":"刘备",  "password":"111", "email":"", "phone":"", "address":"", "token":""},
       {"user_id":2, "user_name":"关羽",  "password":"222", "email":"", "phone":"", "address":"", "token":""}
    ]}
    </textarea><br>
    <input type="button" onclick="updateData()"  value="Update Data">
    <label id="result_update" > * </label>
    <hr>

    <label> Please input the delete condition : </label>
    <textarea rows="3" cols="80" id="text_delete">{"act":"delete", "user_id_range":"1-10"}</textarea>
    <input type="button" onclick="deleteData()" value="Delete Data">
    <label id="result_delete" > * </label>
  </div>
</div>
</HTML>

<style>
  div{
    border-style:solid;
    border-width:1px;
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
    width: 300px;
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
  var state = document.getElementById("state");
  var text_query = document.getElementById("text_query");
  var text_add = document.getElementById("text_add");
  var text_update = document.getElementById("text_update");
  var text_delete = document.getElementById("text_delete");
  var result_query = document.getElementById("result_query");
  var result_add = document.getElementById("result_add");
  var result_update = document.getElementById("result_update");
  var result_delete = document.getElementById("result_delete");

  // Initiate http
  function initHttp() {
    if (httpRequest != null) {
      state.innerText = "reuse http object"
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
    httpRequest.onreadystatechange = addResult;
    httpRequest.send(text_add.value);
  }

  // Add Callback
  function addResult() {
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
    result_query.innerText = ""
    initHttp();
    httpRequest.open("POST", url.value, true);
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    httpRequest.onreadystatechange = queryResult;
    httpRequest.send(text_query.value);
  }

  // Querey Callback
  function queryResult() {
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        result_query.innerText = httpRequest.responseText;
        //Don't need call Text.replace('\n','<br/>'), the DIV supports wrap line
      } else {
        result_query.innerText = httpRequest.status
      }
    }
  }

  // Update data
  function updateData() {
    result_update.innerText = ""
    initHttp();
    httpRequest.open("POST", url.value, true);
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    httpRequest.onreadystatechange = updateResult;
    httpRequest.send(text_update.value);
  }

  // Update Callback
  function updateResult() {
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        result_update.innerText = httpRequest.responseText;
      } else {
        result_update.innerText = httpRequest.status
      }
    }
  }

  // Delete data
  function deleteData() {
    result_delete.innerText = ""
    initHttp();
    httpRequest.open("POST", url.value, true);
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    httpRequest.onreadystatechange = deleteResult;
    httpRequest.send(text_delete.value);
  }

  // Delete Callback
  function deleteResult() {
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        result_delete.innerText = httpRequest.responseText;
      } else {
        result_delete.innerText = httpRequest.status
      }
    }
  }

</script>

