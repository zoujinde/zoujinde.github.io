//#include <string.h>
//include stdlib.h for system(...)
//#include <stdlib.h>
#include <stdio.h>

/*
#include <windows.h>
HANDLE m_ProcessID;
HANDLE m_ChildInRd, m_ChildInWr, m_ChildOutRd, m_ChildOutWr;

void CreateChildProcess(char * cmd,BOOL showChild)
{
	PROCESS_INFORMATION pi;
	STARTUPINFO si;

    SECURITY_ATTRIBUTES sa;
	//Set up the security attributes struct.
	sa.nLength= sizeof(SECURITY_ATTRIBUTES);
	sa.lpSecurityDescriptor = NULL;
	sa.bInheritHandle = TRUE;
	// Create the child output pipe.
	if (!CreatePipe(&m_ChildOutRd, &m_ChildOutWr,&sa,0))
		return;
	// Create the child input pipe.
	if (!CreatePipe(&m_ChildInRd, &m_ChildInWr,&sa,0))
		return;
		
	//Create child process and redirect IO
	memset(&si, 0, sizeof(STARTUPINFO)); 
	
    si.cb = sizeof(STARTUPINFO); 
	si.dwFlags = STARTF_USESHOWWINDOW | STARTF_USESTDHANDLES;
    si.wShowWindow = SW_HIDE; 
	si.hStdInput  = m_ChildInRd;
	si.hStdOutput = m_ChildOutWr;
	si.hStdError  = m_ChildOutWr;
	
 	if (CreateProcess(NULL,cmd,NULL,NULL,TRUE,0,NULL,NULL,&si,&pi))
	{
		m_ProcessID=pi.hProcess;
	}
}
*/

#define JAR_END '#'
#define REORG 32
#define BUF_LEN 1024

//reorganize the buffer
void reorg(char * buf_new, const char * buf_old){
	int h , i , j , k;
	i=0;
	j=0;
	k=0;
	/* Old method
	for(i=0;i<BUF_LEN;++i){
		j = i/REORG;     //j = 0 - 31
		k = i - j*REORG; //k = 0 - 31
		h = k*REORG + j;
		buf_new[h]=buf_old[i];
	}*/
	for(j=0;j<REORG;++j){
		for(k=0;k<REORG;++k){
			h = k*REORG + j;
			buf_new[h]=buf_old[i++];
		}
	}
}

//Add jar file to the exe end 
void addJar(char * exeName, char * jarName)
{
	//char buf[1024];
	int len;
	char * buf_tmp;
	char buf_old[BUF_LEN];
	char buf_new[BUF_LEN];
	int buf_size = BUF_LEN;
	FILE * fJar;
	FILE * fExe;
	FILE * fObj;
	char objName[128];

	strcpy(objName,jarName);
	objName[strlen(jarName)-4]=0;
	strcat(objName,".exe");

	fExe = fopen(exeName,"rb");
	if(fExe == NULL)
	{
		printf("Open exe error.\n");
		goto end;
	}

	fJar = fopen(jarName,"rb");
	if(fJar == NULL)
	{
		printf("Open jar error.\n");
		goto end;
	}

	fObj = fopen(objName,"wb");
	if(fObj == NULL)
	{
		printf("Open obj error.\n");
		goto end;
	}

	//Copy the exe to obj
	while(1){
		len = fread(buf_old,1,buf_size,fExe);
		if(len<=0) break;
		if(len<buf_size){
			buf_tmp = buf_old + len;
			memset(buf_tmp,' ',buf_size-len);
		}
		fwrite(buf_old,1,buf_size,fObj);
	}

	//Add the jar to obj
	while(1){
		len = fread(buf_old,1,buf_size,fJar);
		if(len<=0)break;
		if(len<buf_size){
			buf_tmp = buf_old + len;
			memset(buf_tmp,JAR_END,buf_size-len);
		}
		reorg(buf_new,buf_old);
		fwrite(buf_new,1,buf_size,fObj);
	}

end:	
	if(fExe!=NULL) fclose(fExe);
	if(fJar!=NULL) fclose(fJar);
	if(fObj!=NULL) fclose(fObj);
}

//restore the jar file data, remove the last char 
int restoreData(char * exeName, char * dataName)
{
	int len,exe_len,sum;
	char buf_old[BUF_LEN];
	char buf_new[BUF_LEN];
	int buf_size = BUF_LEN;
	FILE * fData;
	FILE * fExe;

	fExe = fopen(exeName,"rb");
	if(fExe == NULL)
	{
		printf("Open exe error.\n");
		goto end;
	}

	fData = fopen(dataName,"wb");
	if(fData == NULL)
	{
		printf("Open data error.\n");
		goto end;
	}

	fseek(fExe,0,SEEK_END);
	exe_len = ftell(fExe);
	if(exe_len <= 0){
		printf("exe len error = %d \n",exe_len);
		goto end;
	}
	fseek(fExe,0,SEEK_SET);

	//Copy the exe to data file
	sum = 0;
	while(1){
		len = fread(buf_old,1,buf_size,fExe);
		if(len<=0) break;
		
		if(len!=buf_size){
			printf("file size error\n");
			goto end;
		}
		reorg(buf_new,buf_old);
		sum+=buf_size;
		if(sum==exe_len){
			for(len=buf_size;len>0;--len){
				//Find the jar end 
				if(buf_new[len-1]==0)	break;
			}
		}
		fwrite(buf_new,1,len,fData);
	}
	len = 100;
end:	
	if(fExe!=NULL) fclose(fExe);
	if(fData!=NULL) fclose(fData);
	return len;
}

//Start javaw.exe fix
void JavaLauncher(char * exeName,char * logName,char * tmp_dir)
{
	//Why cmd_len >= 269, no error. Once <=268, then error?
	char buf_1[BUF_LEN];
	char buf_2[BUF_LEN];
	int i;
	int n;
	char * s_path="/";
	if(tmp_dir[0]!='/'){
		s_path="\\";
	}

	//revert the exe name
	i = strlen(exeName)-1;
	if(strstr(exeName,".exe")>1){
		i-=4;
	}
	n = 0;
	while(i>=0 && exeName[i]!=s_path[0]){
		buf_1[n]=exeName[i];
		--i;
		++n;
	}
	buf_1[n]=0;

	strcpy(buf_2,tmp_dir);
	strcat(buf_2,s_path);
	strcat(buf_2,".");
	strcat(buf_2,buf_1);
	//printf(buf_2);
	//printf("\n");
	if(restoreData(exeName,buf_2)!=100) return;

	if(*s_path=='/'){
		strcpy(buf_1,"java -Xmx512m -jar ");
	}else{
		strcpy(buf_1,"start javaw -Xmx512m -jar ");
	}
	strcat(buf_1,"\"");
	strcat(buf_1,buf_2);//Add jar name
	strcat(buf_1,"\"");
	if(strlen(logName)>0){
		strcat(buf_1," \"");
		strcat(buf_1,logName);//Add logName
		strcat(buf_1,"\"");
	}

	/*
	m_ProcessID=0;
 	CreateChildProcess(cmd,1);//Or use the QProcess (class)
	if(m_ProcessID==0)
	{
		printf("Fail to Launch java program. \n");
		exit(-1);
	}*/
	//printf(buf_1);
	system(buf_1);
}

//Substr funciton
void substr(char* dest, const char* src, unsigned int start, unsigned int cnt){
	strncpy(dest, src + start, cnt);
	dest[cnt] = 0;
}

//Main function
int main( int argc, char** argv, char** env )
{
	char * tmp_dir=""; //tmp_dir[128];
	char buf_arg[BUF_LEN];
	int i=0;
	int j=0;
	
	//Check the java
	i = system("java -version");
	if(i!=0){
		printf("Please install java : %d \n",i);
		return i;
	}
	
	//Check the tmp_dir
	while(*env){
		substr(buf_arg, *env, 0 , 6); //Find USERPROFILE= or HOME=/
		if(strcmp(buf_arg,"USERPR")==0 || strcmp(buf_arg,"HOME=/")==0){
			//Avoid other path, such as JAVA_HOME
			tmp_dir = strstr(*env,"=") + 1;
			break;
		}
		*env++;
	}
	if(strlen(tmp_dir)==0){
		printf("Cannot find the tmp dir.\n");
		return 0;
	}
	//printf("tmp_dir='%s'\n", tmp_dir);
	
	//Check the exe extname
	if(tmp_dir[0]!='/' && strstr(argv[0],".exe")==0){//Windows OS
		strcat(argv[0],".exe");
	}

	if(strstr(argv[0],"LogMerger")>0){
		if(argc>=8){
			printf("Too many files.");
			return 1;
		}
		strcpy(buf_arg,"");
		for(i=1;i<argc;i++){
			strcat(buf_arg,"\t");
			strcat(buf_arg,argv[i]);
		}
		//printf(buf_arg);
		//printf("\n");
		JavaLauncher(argv[0],buf_arg,tmp_dir);
		return 0;
	}

	if(argc==1){
		JavaLauncher(argv[0],"",tmp_dir);
	}else if(argc>=2){
		//dbg(argv[1]);
		if(strstr(argv[1],".jar")>1 || strstr(argv[1],".dat")>1){
			addJar(argv[0],argv[1]);
		}else{
			JavaLauncher(argv[0],argv[1],tmp_dir);
		}
	}

	//readJavaIni();
	//Terminate child process
	//TerminateProcess(m_ProcessID,0);
	//TerminateThread(&m_threadID,0);
	//exit(0);
	return 0;
}
