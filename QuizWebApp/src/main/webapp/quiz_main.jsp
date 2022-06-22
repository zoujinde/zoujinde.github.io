<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign In</title>
<%@ include file="head.jsp"%>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
  <label style="width:900px;">Please select a questionnaire</label><br>
  <table id="quiz_list" border="1" style="display:block;width:930px;" >
    <tr> <th width="670" >Title</th> <th width="260" >Time</th> </tr>
  </table>
</div>
</HTML>

<script type="text/javascript">
  var httpRequest = getHttpRequest();
  var quiz_list = document.getElementById("quiz_list");

  // Delay load
  setTimeout("load()", 100);

  // Load
  function load() {
    var json = '{"act":"getQuizMain"}';
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", "quiz", true);
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    httpRequest.onreadystatechange = loadResult;
    httpRequest.send(json);
  }

  // Load result
  function loadResult() {
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        var text = httpRequest.responseText.trim();
        if (text.startsWith('[')) {
          var json = JSON.parse(text);
          deleteTable(quiz_list);
          setTable(quiz_list, json);
        } else {
          alert(text);
        }
      } else {
        alert(httpRequest.status);
      }
    }
  }


  // Delete table rows but remain the header
  function deleteTable(table) {
    var rows = table.rows.length;
    for (var i = 1; i < rows; i++) {
      table.deleteRow(1);
    }
  }

  // Set table data
  function setTable(table, data) {
    var style1 = '<a style="margin:10px;color:blue;" href="quiz_item.jsp?quiz_id=';
    var style2 = '">';
    for (var i = 0; i < data.length; i++) {
      var row = table.insertRow();
      var c1 = row.insertCell();
      var c2 = row.insertCell();
      var href = style1 + data[i]['quiz_id'] + style2;
      c1.innerHTML = href + data[i]['quiz_name'] + '</a>';
      c2.innerText = data[i]['create_time'].substring(0, 10);
      c2.height = 60;
    }
  }

</script>
