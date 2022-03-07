<div class='head' style="margin:auto;background:#CCC">
<!-- label id='title' style="width:330px;margin:1px 5px;">Welcome</label -->
<input id='req_id' value='<%=request.getAttribute("req_id")%>' type='hidden'></input>
<label id='req_user' style="width:530px;font-size:39px;margin:1px 10px;"><%=request.getAttribute("req_user")%></label>
<input type="button" onclick="window.location.href='sign-in.jsp'" value="Sign In"
       style="width:180px;height:70px;font-size:39px;margin:5px 0px;"/>
<input type="button" onclick="openSignUp()" value="Sign Up"
       style="width:180px;height:70px;font-size:39px;margin:5px 0px;"/>
</div>

<style>
  div{
    border-style:solid;
    border-width:1px;
    border-color:#999999;
    font-size:50px;
  }

  label{
    cursor: pointer;
    display: inline-block;
    margin: 5px 5px;
    padding: 1px;
    width: 220px;
    font-size:50px;
    text-align: left;
    vertical-align: top;
  }

  input{
    margin: 5px 10px;
    padding: 5px;
    width: 680px;
    font-size:50px;
    vertical-align: top;
  }

  select{
    margin: 5px 1px;
    padding: 5px;
    width: 675px;
    font-size:50px;
    vertical-align: top;
  }

  textarea{
    margin: 5px 1px;
    padding: 5px;
    font-size:50px;
    vertical-align: top;
  }

</style>

<script type="text/javascript">
  var user_name = document.getElementById("req_user");
  if (user_name.innerText == 'null') {
    user_name.innerText = '';
  }

  // The member http object
  var mHttpRequest = null;

  // Get http request
  function getHttpRequest() {
    if (mHttpRequest != null) {
      // reuse http object
    } else if (window.XMLHttpRequest) { //IE6 above and other browser
      mHttpRequest = new XMLHttpRequest()
    } else if(window.ActiveXObject) { //IE6 and lower
      mHttpRequest = new ActiveXObject();
    }
    return mHttpRequest;
  }

  // Open sign up page
  function openSignUp() {
    var req_user = document.getElementById("req_user").innerText.trim();
    var json = {'act':'openSignUp', 'req_user':req_user};
    json = JSON.stringify(json);
    // alert(json);
    // Post URL is Servlet, the sync is true
    httpRequest.open("POST", "/user", true);
    // Only post method needs to set header
    httpRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    httpRequest.setRequestHeader("req_id", document.getElementById("req_id").value);
    httpRequest.onreadystatechange = openResult;
    httpRequest.send(json);
  }

  // Open Callback
  function openResult() {
    // Check 4 : data received
    if(httpRequest.readyState==4) {
      if(httpRequest.status==200) { // 200 OK
        var text = httpRequest.responseText;
        if (text.startsWith('Invalid')) {
          alert(text);
        } else {
          document.close();
          document.write(text);
        }
      } else {
        alert(httpRequest.status + httpRequest.responseText);
      }
    }
  }

</script>