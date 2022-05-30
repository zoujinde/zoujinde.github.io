<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign In</title>
<%@ include file="head.jsp"%>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
  <label style="width:520px;">Please fill out the quiz</label>
  <input type="button" onclick="window.location.href='quiz_main.jsp'" value="All Questionnaire"
       style="width:370px;height:60px;margin:5px 0px;"/>
  <br>
  <label id="title" style="width:900px;">Title : Quiz</label><br>
  <table id="quiz_tab" border="1" style="display:block;width:910px;" >
    <tr> <th id="content" width="850" align="left">?</th><th></th></tr>
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
  var quiz_title = document.getElementById("title");
  var quiz_tab = document.getElementById("quiz_tab");
  var quiz_content = document.getElementById("content");
  var quiz_id = ${param.quiz_id};
  var quiz_item = [];
  var quiz_answer = [];
  var index = 0; // The current index of quiz_item

  // Delay load
  setTimeout("load()", 100);

  // Load
  function load() {
    var json = {'act':'getQuizItem', 'quiz_id':quiz_id};
    json = JSON.stringify(json);
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
        if (text.startsWith('{')) {
          var json = JSON.parse(text);
          quiz_title.innerText = ' * ' + json['title'][0]['quiz_name'];
          quiz_item = json['quiz_item'];
          quiz_result = json['quiz_result'];
          showQuizItem();
        } else {
          alert(text);
        }
      } else {
        alert(httpRequest.status);
      }
    }
  }

  // Show quiz item in table
  function showQuizItem() {
    deleteTable(quiz_tab);
    // Set quiz item content
    var item = quiz_item[index];
    var text = '(' + item['item_id'] + '/' + quiz_item.length + ') ' + item['item_content'];
    quiz_content.innerText = text;
    // Set answers according to type
    var type = item['item_type'];
    var answer = item['item_answer'];
    if (type == 0) { // single choice
      setSingleChoice(answer);
    } else if (type == 1) { // multiple choice
      setMultipleChoice(answer);
    } else { // input text
      setInputText(answer);
    }
  }

  // Delete table rows but remain the header
  function deleteTable(table) {
    var rows = table.rows.length;
    for (var i = 1; i < rows; i++) {
      table.deleteRow(1);
    }
  }

  // Set single choice
  function setSingleChoice(answer) {
    var array = answer.split(' # ');
    for (var i = 0; i < array.length; i++) {
      var row = quiz_tab.insertRow();
      var c1 = row.insertCell();
      var c2 = row.insertCell();
      c1.innerText = array[i];
      c2.innerHTML = '<input type="radio" style="width:50" value="' + i + '"/>';
    }
  }

</script>
