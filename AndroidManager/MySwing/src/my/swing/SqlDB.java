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
			this.mDBStruc = new Vector<String>();
			this.runSql(sql, this.mDBStruc);
		}
		return this.mDBStruc;
	}

    private String[] mLocalSql = new String[]{"sqlite3", "db", "sql"};
    private String[] mAdbSql = new String[]{"adb", "-s", "dev", "shell", "sqlite3", "db", "sql"};
    private int mSqlType = -1;

    //Run sql using adb
    public void runSql(String sql, Vector<String> result){
        result.clear();
        sql = sql.trim();//Must trim
        for(char ch : sql.toCharArray()){
            if(ch>127){//If sql includes Chinese, the value will be error
                result.add(SqlDB.ERROR + "Only accept ASCII sql for now.");
                return;
            }
        }
        String header = null;
        String prefix = sql.substring(0,7).toLowerCase();
        if(prefix.equals("select ")){
            // Get the column name as header
            header = this.getColName(sql, result);
            if (header == null){
                header = ERROR + "Invalid SQL";
            }
        }else{ // insert,delete,update,create,drop
            header = sql; // SQL as table header
        }
        this.runSql(mDbName, sql, result);
        if (SqlDB.getError(result) == null){
            result.add(0, header);
        }
    }

	//2019-08-14 runSql
    private void runSql(final String dbname, final String sql, Vector<String> result){
        result.clear();
        if(this.mLocal){
            mLocalSql[1] = dbname;
            mLocalSql[2] = sql;
            mCmd.runCmd(mLocalSql, sql, result);
        } else { // ADB device
            if (mSqlType <= 0) {
                String cmd = "shell " + mAdbSqlite3 + dbname + " \""  + sql + "\"";
                mCmd.adbCmd(cmd, result);
            } else if (mSqlType == 1) {
                String cmd = "shell " + mAdbSqlite3 + dbname + " \\\""  + sql + "\\\"";
                mCmd.adbCmd(cmd, result);
            } else if (mSqlType == 2) {
                mAdbSql[2] = mCmd.getDeviceID();
                mAdbSql[4] = mAdbSqlite3;
                mAdbSql[5] = dbname;
                mAdbSql[6] = sql;
                mCmd.runCmd(mAdbSql, sql, result);
            }

            if (mSqlType < 0) { // init mAdbSqlComma
                //If the default mAdbSqlite3 cannot work, then use prop try again
                if (result.size() >=1 && result.get(0).contains(" sqlite3: not found")){
                    String cmd = MyProp.getProp(MyProp.LOG_INI, MyProp.ADB_SQLITE3, "/data/local/tmp/sqlite3").trim();
                    //System.out.println("runSqlCmd : adb_sqlite3 = " + prop);
                    if (cmd.length() > 0) {
                        this.mAdbSqlite3 = cmd + " ";
                        runSql(dbname, sql, result);
                    }
                }

                if (result.size() >= 1 && result.get(0).startsWith(ERROR)) {
                    System.out.println("init sql : " + result.get(0));
                    mSqlType = 1;
                    runSql(dbname, sql, result);
                    if (result.size() >= 1 && result.get(0).startsWith(ERROR)) {
                        System.out.println("init sql : " + result.get(0));
                        mSqlType = 2;
                        runSql(dbname, sql, result);
                    }
                } else {
                    mSqlType = 0;
                }
                System.out.println("============ init sql type = " + mSqlType);
            }
        }
    }

	//Execute the sql, return string
	private String getSql(String table, Vector<String> result){
		StringBuilder sb = new StringBuilder();
		String cmd = "select sql from sqlite_master where name='"+table+"';";
		this.runSql(mDbName, cmd, result);
		for(String s : result){
			sb.append(s);
			sb.append(' ');
		}
		return sb.toString();
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
	private String getColName(String sql, Vector<String> result){
		int from = sql.toLowerCase().indexOf(" from ");
		if(from<0){
			return null;
		}
		String col = sql.substring(6,from).trim();
		if (col.equals("*")) {
	        //If select * from, then getTabCol
	        String table = getTabName(sql);
	        sql = this.getSql(table, result);
	        col = this.getTabCol(sql, result);
		}
        return col;
	}

	//Get colName according to the table name
	private String getTabCol(String sql, Vector<String> result){
		int pos1 = sql.indexOf("(");
		if(pos1<10){
			return null;
		}
		//2017-6-2 GM table columns have check(...) or default(...), so use the lastIndexOf
		int pos2 = sql.lastIndexOf(")");
		if(pos2<pos1){
			return null;
		}

		sql=sql.substring(pos1+1,pos2);
		String[] cols=sql.split(",");
		result.clear();
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
			result.add(col);
		}

		//Get all PK cols
		for(int i=pk_start;i<cols.length;i++){
			String pk = cols[i].trim();
			pos1 = pk.indexOf('(');
			if(pos1>0){
				pk = pk.substring(pos1+1).trim();
			}
			//Set all PK cols
			for(int x=0; i<result.size(); i++){
			    if (result.get(x).equalsIgnoreCase(pk)){
			        result.set(x, result.get(x) + PK);
			        break;
			    }
			}
		}
		String s = result.toString();
		return s.substring(1, s.length() - 1);//Remove []
	}
}
