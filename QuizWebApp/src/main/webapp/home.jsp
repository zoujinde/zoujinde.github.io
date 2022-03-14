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
    <tr> <td>New Art Classes</td> <td>2022-03-12</td> </tr>
    <tr> <td>New Games</td> <td>2022-02-15</td> </tr>
    <tr> <td>Happy new year</td> <td>2022-01-01</td> </tr>
  </table>
  <table id="activity" border="1" style="display:none; width:930px;height:500px;" >
    <tr> <th width="670">Activity</th> <th width="260">Time</th> </tr>
    <tr> <td>Reading</td> <td>2022-03-01</td> </tr>
    <tr> <td>Drawing</td> <td>2022-02-01</td> </tr>
    <tr> <td>Skating</td> <td>2022-01-11</td> </tr>
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
