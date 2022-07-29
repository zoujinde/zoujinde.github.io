<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<HTML>
<title>Sign Up - Consent Form</title>
<%@ include file="head.jsp"%>
<div style="width:100%; margin:auto; overflow:auto; background:#AAA">
<form id="form">
  <label style="width:910px;text-align:center;font-size:50px;">Terms of Personal Information</label><br>
  <textarea rows="15" cols="50" id="text" style="resize:none;font-size:30px;">
Any personal information provided by the user to the application will be treated as confidential, our group shall hold personal Information in the strictest confidential and shall not disclose or use Personal Information, except under any regulatory or legal proceedings. In case such disclosure is required to be made by law or any regulatory authority, it will be made on a ‘need-to-know’ basis, unless otherwise instructed by the regulatory authority. 

Our group understand that there are laws in the United States and other countries that protect Personal Information, and that we must not use Personal Information other than for the purposes which was originally used or make any disclosures of personal Information to any third party or from one country to another without prior approval of an authorized representative of the Parent. 
  </textarea>
  <br><br>
  <input style="width:80px;" type="checkbox" id="check"/>
  <label style="width:800px;font-size:44px;">I agree to the Terms of Personal Information</label>
  <br><br>
  <input type="button" onclick="agree()" value="Agree to Sign Up" style="width:910px;height:80px"/>
  <hr style="font-size:1px;">
  <label id="result" style="width:910px;"/>
</form>
</div>
</HTML>

<script type="text/javascript">

  // Check
  function agree() {
    var check = document.getElementById("check");
    if (check.checked) {
      window.location.href = "sign-up.jsp";
    } else {
      alert("Please agree to the Terms of Personal Information");
    }
  }

</script>
