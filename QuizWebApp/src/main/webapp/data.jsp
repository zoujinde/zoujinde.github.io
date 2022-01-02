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
    <select id="result_query" size="20" style="width:1500;height=500">
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
    margin: 1px 3px;
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
  var url = document.getElementById("url");
  var text_query = document.getElementById("text_query");
  var text_data = document.getElementById("text_data");
  var result_query = document.getElementById("result_query");
  var result_state = document.getElementById("state");
  var result_data = document.getElementById("result_data");

  // Add new data
  function addData() {
    var index = result_query.selectedIndex;
    if (index >= 0) {
      var text = result_query.options[index].text;
      // Can't use innerHTML or innerText, which can't work on the 2nd time
      text_data.value = '{"act":"insert", "data":[\n  ' + text + '\n]}';
    } else {
      text_data.value = 'No selected data to insert';
    }
  }

  // Modify and update data
  function updateData() {
    var index = result_query.selectedIndex;
    if (index >= 0) {
      var text = result_query.options[index].text;
      text_data.value = '{"act":"update", "data":[\n  ' + text + '\n]}';
    } else {
      text_data.value = 'No selected data to update';
    }
  }

  // Delete rows data
  function deleteData() {
    var index = result_query.selectedIndex;
    if (index >= 0) {
      var text = result_query.options[index].text;
      // Get the id value, for example {"xxx_id":1,}
      var id_val = null;
      var p1 = text.indexOf(':');
      if (p1 > 0) {
        var p2 = text.indexOf(',', p1);
        if (p2 > 0) {
          id_val = text.substring(p1 + 1, p2)
        }
      }
      if (id_val != null) {
        text_data.value = '{"act":"delete", "id_range":"' + id_val + '-' + id_val + '"}';
      } else {
        text_data.value = 'Can not get the ID value';
      }
    } else {
      text_data.value = 'No selected data to delete';
    }
  }

  // On URL change
  function onUrlChange(obj) {
    var index = obj.selectedIndex;
    var val = obj.options[index].value;
    var txt = obj.options[index].text;
    //result_query.innerText = ''
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
    // Check the data
    var data = text_data.value.trim();
    if (data.startsWith('{') && data.endsWith('}')) {
      //data is OK
    } else {
      result_data.innerText = 'Invalid Data';
      return;
    }
    initHttp();
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", url.value, true);
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
    //result_query.innerText = ""
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
          option.text  = array[i];
          result_query.options.add(option);
        }
        result_query.size = 20;
      } else {
        result_state.innerText = httpRequest.status;
      }
    }
  }

</script>

