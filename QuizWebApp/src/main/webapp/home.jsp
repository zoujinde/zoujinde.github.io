<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Home</title>
<%@ include file="head.jsp"%>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
  <label style="width:900px;">Welcome to home page</label><br>
  <a id="a1" style="text-decoration:underline;" href="javascript:void(0);" onclick="showBulletin()" >News Bulletin</a>
  <a id="a2" style="text-decoration:none;"      href="javascript:void(0);" onclick="showActivity()" >My Activities</a>
  <hr style="font-size:1px;">
  <table id="bulletin" border="1" style="display:block;width:930px;height:500px;" >
    <tr> <th width="670">Bulletin</th> <th width="260">Time</th> </tr>
  </table>
  <table id="activity" border="1" style="display:none; width:930px;height:500px;" >
    <tr> <th width="670">Activity</th> <th width="260">Time</th> </tr>
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
  setTimeout("load()", 500);

  // Load
  function load() {
    var json = {
      'bulletin':[
        {'title':'New Art Class', 'time':'2022-03-11'},
        {'title':'New Game', 'time':'2022-02-01'},
        ],
      'activity':[
        {'title':'Drawing', 'time':'2022-02-28'},
        {'title':'Reading', 'time':'2022-01-15'},
        ],
      };
    // Refresh table data
    deleteTable(bulletin);
    deleteTable(activity);
    setTable(bulletin, json['bulletin']);
    setTable(activity, json['activity']);
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
    var label_style = '<label style="width:100%;color:blue;" onclick="alert(123)">';
    for (var i = 0; i < data.length; i++) {
      var row = table.insertRow();
      var c1 = row.insertCell();
      var c2 = row.insertCell();
      c1.innerHTML = label_style + data[i]['title'] + '</label>';
      c2.innerText = data[i]['time'];
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
