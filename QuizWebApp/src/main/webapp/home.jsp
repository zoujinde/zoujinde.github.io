<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Home</title>
<%@ include file="head.jsp"%>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
  <label style="width:520px;">Welcome to home page</label>
  <input type="button" onclick="window.location.href='quiz_main.jsp'" value="Fill Questionnaire"
       style="width:370px;margin:5px 0px 50px 0px;"/>
  <br>
  <a id="a1" style="text-decoration:underline;" href="javascript:void(0);" onclick="showBulletin()" >News Bulletin</a>
  <a id="a2" style="text-decoration:none;"      href="javascript:void(0);" onclick="showActivity()" >My Activities</a>
  <hr style="font-size:1px;">
  <table id="bulletin" border="1" style="display:block;width:930px;" >
    <tr> <th width="670" >Bulletin</th> <th width="260" >Time</th> </tr>
  </table>
  <table id="activity" border="1" style="display:none; width:930px;" >
    <tr> <th width="670" >Activity</th> <th width="260">Time</th> </tr>
  </table>
  <hr style="font-size:1px;">
</div>
</HTML>

<script type="text/javascript">
  var httpRequest = getHttpRequest();
  var bulletin = document.getElementById("bulletin");
  var activity = document.getElementById("activity");
  var a1 = document.getElementById("a1");
  var a2 = document.getElementById("a2");

  // Delay load
  setTimeout("load()", 100);

  // Load
  function load() {
    var json = {'act':'getData'};
    json = JSON.stringify(json);
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", "home", true);
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    httpRequest.onreadystatechange = loadResult;
    httpRequest.send(json);
  }

  // Load result
  function loadResult() {
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        var text = httpRequest.responseText.trim();
        if (text.startsWith('{')) {
          var json = JSON.parse(text);
          // Refresh table data
          deleteTable(bulletin);
          deleteTable(activity);
          setTable(bulletin, json['events']);
          setTable(activity, json['activities']);
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
    var label_style = '<label style="width:90%;color:blue;" onclick="alert(123)">';
    for (var i = 0; i < data.length; i++) {
      var row = table.insertRow();
      var c1 = row.insertCell();
      var c2 = row.insertCell();
      c1.innerHTML = label_style + data[i]['title'] + '</label>';
      c2.innerText = data[i]['create_time'].substring(0, 10);
    }
  }

  // Show Bulletin
  function showBulletin() {
    bulletin.style.display = 'block';
    activity.style.display = 'none';
    a1.style.textDecoration='underline';
    a2.style.textDecoration='none';
  }

  // Show Activity
  function showActivity() {
    bulletin.style.display = 'none';
    activity.style.display = 'block';
    a1.style.textDecoration='none';
    a2.style.textDecoration='underline';
  }

</script>
