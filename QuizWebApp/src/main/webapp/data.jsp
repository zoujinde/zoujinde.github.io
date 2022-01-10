<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<HTML>
<title>Quzi Data Management</title>
<label>Quzi Data Management &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp &nbsp
       Please select the URL : </label>
<select id="url" onchange="onUrlChange(this)">
  <option value="/data?tab=user">user</option>
  <option value="/data?tab=quiz">quiz</option>
  <option value="/data?tab=quiz_item">quiz_item</option>
  <option value="/data?tab=quiz_result">quiz_result</option>
</select>
<div style="width:100%; margin:auto; overflow:auto;">
  <div style="width:99%; background:#EEE">
    <label> Please input the query condition : </label>
    <textarea rows="2" cols="80" id="text_query" style="resize:none;">{"act":"select", "id_range":"1-500"}</textarea>
    <input type="button" onclick="queryData()" value="Query Data">
    <label id="state"> * </label><br>
    <select id="result_query" size="20" style="width:1500;height=500" onchange="updateData()">
    </select>
  </div>
  <div style="width:99%; background:#EEE">
    <input type="button" onclick="addData()" value="Add New Rows">
    <input type="button" onclick="updateData()"  value="Modify Rows">
    <input type="button" onclick="deleteData()" value="Delete Rows">
    <input type="button" onclick="submitData()" value="Submit Data">
    <label id="result_data" > * </label> <br>
    <textarea rows="12" cols="200" id="text_data" ></textarea><br>
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
    margin: 6px 3px;
    padding: 1px;
    text-align: left;
    vertical-align: top;
  }

  input[type="button"]{
    margin: 6px 6px;
    width: 120px;
    vertical-align: top;
  }

  select[id="url"]{
    margin: 6px 3px;
    width: 200px;
    height: 50px
    vertical-align: top;
  }

  textarea{
    margin: 1px 3px;
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

  // Add new data
  function addData() {
    if (result_query.options.length > 0) {
      var text = result_query.options[0].text;
      // Can't use innerHTML or innerText, which can't work on the 2nd time
      text_data.value = '[\n{"act":"insert", ' + wrap(text) + '},\n]';
    } else {
      text_data.value = 'Can not add data, please query data firstly.';
    }
  }

  // Modify and update data
  function updateData() {
    var index = result_query.selectedIndex;
    if (index >= 0) {
      var text = result_query.options[index].text;
      text_data.value = '[\n{"act":"update", ' + wrap(text) + '},\n]';
    } else {
      text_data.value = 'Can not modify data, please select data row to update.';
    }
  }

  // Delete rows data
  function deleteData() {
    var index = result_query.selectedIndex;
    if (index >= 0) {
      var text = result_query.options[index].text;
      text_data.value = '[\n{"act":"delete", ' + wrap(text) + '},\n]';
    } else {
      text_data.value = 'Can not delete data, please select data row to delete.';
    }
  }

  // Wrap text
  function wrap(text) {
    if (text.length > 200) {
      var p = text.indexOf('", "', 80);
      if (p > 0) {
        text = text.substring(0, p+2) + '\n' + text.substring(p+2);
        p = text.indexOf('", "', p+80)
        if (p > 0) {
          text = text.substring(0, p+2) + '\n' + text.substring(p+2);
        }
      }
    }
    return text;
  }

  // On URL change
  function onUrlChange(obj) {
    //Must clear data, otherwise submit wrong data.
    while(result_query.options.length > 0) {
      result_query.options.remove(0);
    }
    text_data.value = '';
    result_data.innerText = '*';
  }

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

  // Submit data
  function submitData() {
    result_data.innerText = '*';
    // Check the data
    var data = text_data.value.trim();
    if (data.startsWith('{') && data.endsWith('}')) {
      // {...} is OK
    } else if (data.startsWith('[') && data.endsWith(']')) {
      // [...] is OK
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

  // Submit Callback
  function submitResult() {
    // Check 4 : data received
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        result_data.innerText = httpRequest.responseText;
      } else {
        result_data.innerText = httpRequest.status;
      }
    }
  }

  // Query data
  function queryData() {
    initHttp();
    var path = getPath() + select_url.value;
    httpRequest.open("POST", path, true);
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    httpRequest.onreadystatechange = queryResult;
    httpRequest.send(text_query.value);
  }

  // Querey Callback
  function queryResult() {
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        // The text is a JSON string like [{...},{...},{...}]
        var text = httpRequest.responseText.trim();
        if (text.startsWith('[')) {
          text = text.substring(1, text.length - 1); // Remove []
        }
        var array = text.split(',\n');
        // Clear the result
        while(result_query.options.length > 0) {
          //result_query.removeChild(result_query.childNodes[0]);
          //result_query.remove[0];
          result_query.options.remove(0);
        }
        // Add the result
        for (var i = 0; i < array.length; i++) {
          var option = document.createElement("OPTION");
          option.value = i;
          var text = array[i].trim();
          if (text.startsWith('{')) {
            text = text.substring(1, text.length - 1); // Remove {}
          }
          option.text  = text;
          result_query.options.add(option);
        }
        result_query.size = 20;
      } else {
        result_state.innerText = httpRequest.status;
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

