<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign In</title>
<%@ include file="head.jsp"%>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
  <label style="width:520px;">Please fill in the quiz</label>
  <input type="button" onclick="window.location.href='quiz_main.jsp'" value="All Questionnaire"
       style="width:370px;height:60px;margin:5px 0px;"/>
  <br>
  <label id="title" style="width:900px;">Title : Quiz</label><br>
  <table id="quiz_item" border="1" style="display:block;width:910px;" >
    <tr> <th id="content" width="910" >(1/1) What is your name?</th> </tr>
  </table>
  <input type="button" onclick="" value="Previous" style="width:280px;"/>
  <input type="button" onclick="" value="Submit" style="width:280px;"/>
  <input type="button" onclick="" value="Next" style="width:280px;"/>
  <hr style="font-size:1px;">
  <label id="result" style="width:910px;"/>
</div>
</HTML>

<script type="text/javascript">
  var httpRequest = getHttpRequest();
  var quiz_item = document.getElementById("quiz_item");

  // Delay load
  setTimeout("load()", 100);

  // Load
  function load() {
    var json = '{"act":"getQuizItem"}';
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
          //deleteTable(quiz_ite);
          //setTable(quiz_list, json);
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
    var style1 = '<a style="width:90%;color:blue;" href="quiz_item.jsp?quiz_id=';
    var style2 = '">';
    for (var i = 0; i < data.length; i++) {
      var row = table.insertRow();
      var c1 = row.insertCell();
      var c2 = row.insertCell();
      var href = style1 + data[i]['quiz_id'] + style2;
      c1.innerHTML = href + data[i]['quiz_name'] + '</a>';
      c2.innerText = data[i]['create_time'].substring(0, 10);
    }
  }

</script>
