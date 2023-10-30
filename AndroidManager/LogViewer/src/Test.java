import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Test { 
	public static void main(String[] args) {
		/*
		double data = 123456789.195;
		System.out.println("double = " + data);
		System.out.println("round2 = " + round2(data));
	    //String s = "06-15 20:20:22.000             D StatisticsDataImpl.getStatDataXML:LSF_DEVICE_PUSH:stat is :<StaData><DynaData><DataAccInfo><NetMode></NetMode><IP></IP><APN></APN><OperCode></OperCode><IMSI /></DataAccInfo></DynaData><FBData><DisMessages>rinter2_ff80808145fa95470145fa987bd40002,rinter2_ff80808145fa95470145fa987bd40002_d,rinter2_ff80808145fa95470145fa987bd40002_INS_e</DisMessages><ClicMessages>rinter2_ff80808145fa95470145fa987bd40002,rinter2_ff80808145fa95470145fa987bd40002_c,rinter2_ff80808145fa95470145fa987bd40002_ATI,rinter2_ff80808145fa95470145fa987bd40002_ATI,</ClicMessages><AppInstalls><App><MessageFBID>rinter2_ff80808145fa95470145fa987bd40002</MessageFBID><PackName>com.baidu.appsearch</PackName><CurrVer>null</CurrVer><TargetVer>16782103</TargetVer><Result>Success</Result><ErrCode>null</ErrCode></App></AppInstalls><AppDownloads><DownloadApp><MessageFBID>rinter2_ff80808145fa95470145fa987bd40002</MessageFBID><PackName>com.baidu.appsearch</PackName><CurrVer>16782103</CurrVer><Result>Success</Result></DownloadApp></AppDownloads><EngUpgrades /><NacDataList /></FBData></StaData>";
        String s = "<StaData><DynaData><DataAccInfo><NetMode></NetMode><IP></IP><APN></APN><OperCode></OperCode><IMSI /></DataAccInfo></DynaData><FBData><DisMessages>rinter2_ff80808145fa95470145fa987bd40002,rinter2_ff80808145fa95470145fa987bd40002_d,rinter2_ff80808145fa95470145fa987bd40002_INS_e</DisMessages><ClicMessages>rinter2_ff80808145fa95470145fa987bd40002,rinter2_ff80808145fa95470145fa987bd40002_c,rinter2_ff80808145fa95470145fa987bd40002_ATI,rinter2_ff80808145fa95470145fa987bd40002_ATI,</ClicMessages><AppInstalls><App><MessageFBID>rinter2_ff80808145fa95470145fa987bd40002</MessageFBID><PackName>com.baidu.appsearch</PackName><CurrVer>null</CurrVer><TargetVer>16782103</TargetVer><Result>Success</Result><ErrCode>null</ErrCode></App></AppInstalls><AppDownloads><DownloadApp><MessageFBID>rinter2_ff80808145fa95470145fa987bd40002</MessageFBID><PackName>com.baidu.appsearch</PackName><CurrVer>16782103</CurrVer><Result>Success</Result></DownloadApp></AppDownloads><EngUpgrades /><NacDataList /></FBData></StaData>";
	    Pattern p = Pattern.compile("_ff80808145fa95470145fa987bd40002_INS_e");
	    System.out.println(p.matcher(s).find());
	    //testJdbcExcel();
        */
	    String value = null;
	    String result = String.format("(%s\n)", value);
	    System.out.println("result=" + result);
	}

	//JDBC excel test
	public static void testJdbcExcel(){
	    Connection con = null;   
	    // Driver={Microsoft Excel Driver (*.xls)};DriverId=790;Dbq=C:\MyExcel.xls;DefaultDir=c:\mypath;   
	    String url = "jdbc:odbc:Driver={Microsoft Excel Driver (*.xls)};READONLY=FALSE;DBQ=";
	    String excel = "D:\\PushTeam\\201404_task\\test.xls";
	    try {
	        //String DRIVERCLASSNAME = "sun.jdbc.odbc.JdbcOdbcDriver";   
            //Class.forName(DRIVERCLASSNAME);
            con = DriverManager.getConnection(url + excel);
            Statement statement = con.createStatement();   
            /**  
             * Excel SQL syntax "SELECT * FROM [sheet1$]". I.e.  
             * excel worksheet name followed by a "$" and wrapped in "[" "]" brackets.  
             * */
            String sql = "SELECT * FROM [downloadfailure$]";
            sql = "select test, count(*) from [downloadfailure$] group by test";
            ResultSet result = statement.executeQuery(sql);
            while (result.next()) {
                int cols = result.getMetaData().getColumnCount();
                
                for(int i=1;i<=cols;i++){
                    if(i<cols){
                        System.out.print(result.getString(i) + "\t"); 
                    }else{
                        System.out.println(result.getString(i));
                    }
                }
            }
            result.close();   
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }   

	}
	
	
	//Round 4-, 5+
	public static String round2(double money){
		String tmp = Double.toString(money);
		int pos = tmp.indexOf('.');
		if(pos>0 && tmp.length()>pos+3){
			tmp = Double.toString(money+0.005001);
			int posE=tmp.indexOf("E");
			if(posE<0) return tmp.substring(0,pos+3);
			int k = Integer.parseInt(tmp.substring(posE+1));
			int dot=pos+k+1;
			tmp=tmp.substring(0,pos)+tmp.substring(pos+1,dot)+"."+tmp.substring(dot,dot+2);
		}
		return tmp;
	}

	public static String trimAll(String str){
		byte[] bytes = str.getBytes();
		int x = 0;
		boolean skip = true;
		for(int i=0;i<bytes.length;i++){
			boolean space = (bytes[i]==' ' || bytes[i]=='\t');
			if(skip && space) continue;
			if(space){
				bytes[x++]=' ';
				skip = true;
			}else{
				bytes[x++]=bytes[i];
				skip = false;
			}
		}
		if(x>0){
			if(bytes[x-1]==' ')x--;
			if(x>0){
				return new String(bytes).substring(0,x);
			}
		}
		return "";
	}
	
}
