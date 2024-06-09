<div id='div_head' style="margin:auto;">
<!--input value='<%=request.getAttribute("req_user")%>' type='hidden'></input-->
<table style="width:930px;margin:0px;">
  <tr>
  <td>
    <label style="width:500px;margin:10px 0px;color:blue;font-weight:bold;font-size:50px;"
           onclick="location.href='home.jsp'">Purple Sense Home</label><br>
    <label id="req_user" style="width:500px;margin:20px 0px;color:yellow;font-size:50px;"
           onclick="location.href='sign-up.jsp?act=modify'">${req_user}</label>
  </td>
  <td>
    <input type="button" onclick="window.location.href='sign-in.jsp?act=input'" value="Sign In"
     style="width:200px;height:80px;margin:5px 0px;"/>
  </td>
  <td>
    <input type="button" onclick="window.location.href='sign_form.jsp'" value="Sign Up"
     style="width:200px;height:80px;margin:5px 0px;"/>
  </td>
  </tr>
</table>
<hr style="font-size:1px;">
</div>

<style>
  body {
    background-image: url("purple.jpg");
    background-size: cover;
  }

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
    color:white;
  }

  input{
    margin: 1px 1px 10px 10px;
    padding: 1px;
    width: 680px;
    height: 80px;
    font-size:39px;
    vertical-align: top;
  }

  select{
    margin: 5px 5px;
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

  a {
    margin: 1px 80px;
    padding: 1px;
    font-size:50px;
    vertical-align: top;
    color: yellow;
  }

  table {
    margin: 5px 5px;
    padding: 1px;
    font-size:50px;
    vertical-align: top;
    color: white;
  }

</style>

<script type="text/javascript">
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

  // Get URL argument value
  function getUrlValue(key) {
    var query = "&" + window.location.search.substring(1) + "&";
    var key = "&" + key + "=";
    var value = "";
    var p1 = query.indexOf(key);
    if (p1 >= 0) {
      p1 = p1 + key.length;
      var p2 = query.indexOf("&", p1);
      if (p2 > p1) {
        value = query.substring(p1, p2);
      }
    }
    return value;
  }

</script>