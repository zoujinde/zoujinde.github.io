/**
 * 
 */
package my.swing;

import java.util.Vector;

/**
 * @author mjq836
 *
 */
public class SqlDB{
	
	public static final String ERROR = "Error: ";
	public static final String PK = " *";
	
	private CMD mCmd = null;
	private String mDbName = null;
	private Vector<String> mDBStruc = null;
	private boolean mLocal = false;
	private String mAdbSqlite3 = "sqlite3 ";
	
	//constructor
	public SqlDB(CMD cmd, String dbName, boolean local){
		this.mCmd = cmd;
		this.mDbName = dbName;
		this.mLocal = local;
	}

	public static String getError(Vector<String> data){
		String s = null;
		if(data.size() > 0 && data.get(0).startsWith(ERROR)){
			s = data.get(0);
		}
		return s;
	}

	public boolean hasError(Vector<String> data) {
	    return getError(data) != null;
	}

	//Get DB structures
	public Vector<String> getDBStruc(){
		if(mDBStruc==null){//Only select name, cannot select name,sql. Since the sql includes \n
			String sql = "select name from sqlite_master where type='table';";
			this.mDBStruc = this.runSql(sql);
		}
		return this.mDBStruc;
	}

    private String[] mLocalSql = new String[]{"sqlite3", "db", "sql"};
    private String[] mAdbSql = new String[]{"adb", "-s", "dev", "shell", "sqlite3", "db", "sql"};
    private int mSqlType = -1;

	//2019-08-14 runSql
    private Vector<String> runSql(final String dbname, final String sql){
        Vector<String> v = null;
        if(this.mLocal){
            mLocalSql[1] = dbname;
            mLocalSql[2] = sql;
            v = mCmd.runCmd(mLocalSql, sql);
        } else { // ADB device
            if (mSqlType <= 0) {
                String cmd = "shell " + mAdbSqlite3 + dbname + " \""  + sql + "\"";
                v = mCmd.adbCmd(cmd);

            } else if (mSqlType == 1) {
                String cmd = "shell " + mAdbSqlite3 + dbname + " \\\""  + sql + "\\\"";
                v = mCmd.adbCmd(cmd);

            } else if (mSqlType == 2) {
                mAdbSql[2] = mCmd.getDeviceID();
                mAdbSql[4] = mAdbSqlite3;
                mAdbSql[5] = dbname;
                mAdbSql[6] = sql;
                v = mCmd.runCmd(mAdbSql, sql);
            }

            if (mSqlType < 0) { // init mAdbSqlComma
                //If the default mAdbSqlite3 cannot work, then use prop try again
                if (v.size() >=1 && v.get(0).contains(" sqlite3: not found")){
                    String cmd = MyProp.getProp(MyProp.LOG_INI, MyProp.ADB_SQLITE3, "/data/local/tmp/sqlite3").trim();
                    //System.out.println("runSqlCmd : adb_sqlite3 = " + prop);
                    if (cmd.length() > 0) {
                        this.mAdbSqlite3 = cmd + " ";
                        v = runSql(dbname, sql);
                    }
                }

                if (v.size() >= 1 && v.get(0).startsWith("Error: ")) {
                    System.out.println("init sql : " + v.get(0));
                    mSqlType = 1;
                    v = runSql(dbname, sql);
                    if (v.size() >= 1 && v.get(0).startsWith("Error: ")) {
                        System.out.println("init sql : " + v.get(0));
                        mSqlType = 2;
                        v = runSql(dbname, sql);
                    }
                } else {
                    mSqlType = 0;
                }
                System.out.println("============ init sql command type = " + mSqlType);
            }
        }
        return v;
    }

	//Execute the sql, return string
	private String getSql(String table){
		StringBuilder sb = new StringBuilder();
		String cmd = "select sql from sqlite_master where name='"+table+"';";
		//cmd = this.mDbName + " \"" +cmd+"\"";
		Vector<String> vec = this.runSql(mDbName, cmd);
		for(String s:vec){
			sb.append(s);
			sb.append(' ');
		}
		vec.removeAllElements();
		vec=null;
		return sb.toString();
	}
	
	//Run sql using adb
	public Vector<String> runSql(String sql){
        sql = sql.trim();//Must trim
        for(char ch : sql.toCharArray()){
            if(ch>127){//If sql includes Chinese, the value will be error
                Vector<String> result = new Vector<String>();
                result.add(SqlDB.ERROR + "Only accept ASCII sql for now.");
                return result;
        	}
        }
		//String cmd =  this.mDbName + " \"" +sql+"\"";
		Vector<String> result = this.runSql(mDbName, sql);
		if(SqlDB.getError(result) != null){
			return result;
		}
		String prefix = sql.substring(0,7).toLowerCase();
		if(prefix.equals("select ")){
			//Add the column names
			String colName = this.getColName(sql);
			//System.out.println("colName=" + colName);
			if(colName==null){
			    result.add(0, ERROR + "Invalid SQL.");
			} else {
			    result.add(0, colName);
			}
		}else{//Such as insert,delete,update,create,drop
			result.add(0, sql);//Add SQL as table header
		}
		return result;
	}

	//Get the table name
	public static String getTabName(String sql){
		int from = sql.toLowerCase().indexOf(" from ");
		if(from<0){
			return null;
		}
		String table = sql.substring(from+6).trim();
		int end = table.indexOf(' ');
		if(end>0){
			table = table.substring(0,end);
		}
		return table;
	}	

	//Get the column names
	private String getColName(String sql){
		int from = sql.toLowerCase().indexOf(" from ");
		if(from<0){
			return null;
		}
		String col = sql.substring(6,from).trim();
		if (col.equals("*")) {
	        //If select * from, then check DBStruc
	        String table = sql.substring(from+6).trim();
	        int end = table.indexOf(' ');
	        if(end>0){
	            table = table.substring(0,end);
	        }
	        col = this.getTabCol(table);
		}
        return col;
	}

	//Get colName according to the table name
	private String getTabCol(String table){
		String sql = this.getSql(table);
		//System.out.println("getTabCol : sql=" + sql);
		//Get the colNames
		int pos1 = sql.indexOf("(");
		if(pos1<10){
			return null;
		}
		
		//2017-6-2 GM table columns have check(...) or default(...), so use the lastIndexOf
		//int pos2 = sql.indexOf(")");
		int pos2 = sql.lastIndexOf(")");
		if(pos2<pos1){
			return null;
		}
		
		sql=sql.substring(pos1+1,pos2);
		
		String[] cols=sql.split(",");
		Vector<String> list = new Vector<String>();
		int pk_start = 1000;
		for(int i=0;i<cols.length;i++){
			String col = cols[i].trim();
			String lower = col.toLowerCase();
			if(lower.startsWith("primary ")){//PK is start
				pk_start = i;
				break;
			}
			pos1 = col.indexOf(' ');
			if(pos1>0){
				col = col.substring(0,pos1);
			}
			if(lower.contains(" primary ")){
				col+=PK;
			}
			list.add(col);
		}

		//Get all PK cols
		for(int i=pk_start;i<cols.length;i++){
			String pk = cols[i].trim();
			pos1 = pk.indexOf('(');
			if(pos1>0){
				pk = pk.substring(pos1+1).trim();
			}
			//Set all PK cols
			for(int x=0;i<list.size();i++){
			    if(list.get(x).equalsIgnoreCase(pk)){
			        list.set(x, list.get(x)+PK);
			        break;
			    }
			}
		}
		String s = list.toString();
		return s.substring(1, s.length() - 1);//Remove []
	}
}
