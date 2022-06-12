<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign In</title>
<%@ include file="head.jsp"%>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
  <label style="width:520px;">Please fill out the quiz</label>
  <input type="button" onclick="window.location.href='quiz_main.jsp'" value="All Questionnaire"
       style="width:370px;margin:5px 0px;"/>
  <br>
  <label id="title" style="width:900px;">Title : Quiz</label><br>
  <table id="quiz_tab" border="1" style="display:block;width:910px;" >
    <tr> <th id="content" width="850" align="left"></th><th></th></tr>
  </table>
  <input type="button" onclick="toPrev()" value="Previous" style="width:280px;"/>
  <input type="button" onclick="toSave()" value="Submit" style="width:280px;"/>
  <input type="button" onclick="toNext()" value="Next" style="width:280px;"/>
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
  var item_id = 1; // The current item_id
  var item_index = 0;
  var item_count = 0;

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
          var last = quiz_item.length - 1;
          if (last >= 0) {
            item_count = quiz_item[last]['item_id'];
          }
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
    var type = -1;
    for (var i = 0; i < quiz_item.length; i++) {
      var item = quiz_item[i];
      if (item['item_id'] == item_id) {
        if (item['item_row'] == 0) { // The question
          item_index = i;
          var text = item_id + '/' + item_count + ' # ' + item['item_content'];
          quiz_content.innerText = text;
          type = item['item_type'];
        } else { // The answer
          var input = '<input style="width:50; zoom:120%;" type="radio" name="radio_value"';
          if (type == 1) {
            input = '<input style="width:50; zoom:120%;" type="checkbox" name="check_value"';
          }
          var row = quiz_tab.insertRow();
          var c1 = row.insertCell();
          var c2 = row.insertCell();
          c1.innerText = item['item_content'];
          c2.innerHTML = input + ' value="' + i + '"/>';
        }
      }
    }
    // Set answers according to type
    if (item_index >= 0) {
      var answer = quiz_item[item_index]['answer'];
      setInputBox(answer, type);
    } else {
      alert('Invalid item id : ' + item_id);
    }
  }

  // Delete table rows but remain the header
  function deleteTable(table) {
    var rows = table.rows.length;
    for (var i = 1; i < rows; i++) {
      table.deleteRow(1);
    }
  }

  // Set radio or check box
  function setInputBox(answer, type) {
    // Set checked state
    if (type == 0) { // radio
      var radio_value = document.getElementsByName("radio_value");
      for (var i = 0; i < radio_value.length; i++) {
        if (i.toString() == answer) {
          radio_value[i].checked = true;
          break;
        }
      }
    } else if (type == 1) { // check box
      var check_value = document.getElementsByName("check_value");
      // alert('answer=' + answer);
      for (var i = 0; i < check_value.length; i++) {
        if (answer.indexOf(i) >= 0) {
          check_value[i].checked = true;
        }
      }
    } else {
      var input = '<textarea rows="5" cols="36" id="text_value">'
      var row = quiz_tab.insertRow();
      var c1 = row.insertCell();
      c1.innerHTML = input + answer + '</textarea>';
    }
  }

  // To previous item
  function toPrev() {
    if (item_id <= 1) {
      alert('Already to the first item.');
    } else if (checkResult()) {
      item_id -= 1;
      showQuizItem();
    }
  }

  // To next item
  function toNext() {
    if (item_id >= item_count) {
      alert('Already to the last item.');
    } else if (checkResult()) {
      item_id += 1;
      showQuizItem();
    }
  }

  // Check the memory result
  function checkResult() {
    var answer = '';
    var item = quiz_item[item_index];
    var type = item['item_type'];
    if (type == 0) { // radio
      var radio_value = document.getElementsByName("radio_value");
      for (var i = 0; i < radio_value.length; i++) {
        if (radio_value[i].checked) {
          answer += i;
          break;
        }
      }
    } else if (type == 1) { // check box
      var check_value = document.getElementsByName("check_value");
      for (var i = 0; i < check_value.length; i++) {
        if (check_value[i].checked) {
          if (answer.length <= 0) {
            answer += i;
          } else {
            answer += ',' + i;
          }
        }
      }
    } else {
      // getElementById    : return the 1st object
      // getElementsByName : return the array
      var text = document.getElementById("text_value");
      answer = text.value.trim();
    }
    // Check answer
    if (answer.length > 0) {
      item['answer'] = answer;
      return true;
    } else {
      alert('Please answer the question');
      return false;
    }
  }

  // To save data to server
  function toSave() {
    if (!checkResult()) {
      return;
    }
    // Only submit id and answer to server
    var data = [];
    for (var i = 0; i < quiz_item.length; i++) {
      if (quiz_item[i]['item_row'] != 0) {
        continue;
      }
      var itemId = quiz_item[i]['item_id'];
      var answer = quiz_item[i]['answer'];
      if (answer.length <= 0) {
        alert('Please answer the question ' + itemId);
        return;
      }
      data[i] = {};
      data[i]['item_id'] = itemId;
      data[i]['answer']  = answer;
    }
    var json = {'act':'setQuizData', 'quiz_id':quiz_id, 'data':data};
    json = JSON.stringify(json);
    // alert(json);
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", "quiz", true);
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    httpRequest.onreadystatechange = saveResult;
    httpRequest.send(json);
  }

  // Save result
  function saveResult() {
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        var text = httpRequest.responseText.trim();
        alert(text);
      } else {
        alert(httpRequest.status);
      }
    }
  }

</script>
