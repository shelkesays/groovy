#!/usr/bin/env groovy

/*
=================================================================================
Method : dbbackup
Description: Backup a db
Parameter:
	dbServer - Database server name
	dbName - Database whose backup should be taken
	backuppath - where the backup file to be stored

env variables:
	env.SQLCMD - SQLCMD path
return -

==================================================================================
*/
def dbbackup(dbServer, dbName, backupPath){
	node('L5-WINDOWSSLAVE') {
        def backupFile = new File("${backupPath}")
        // If parent directory exist, create backup
        if( backupFile.getParentFile().exists() ) {
            def backupQuery = /"backup database [${dbName}] to disk=N'${backupPath}' with COPY_ONLY, STATS = 5"/
		    bat "${SQLCMD} -S ${dbServer} -W -Q ${backupQuery}"

            echo "Sucessfully restored ${dbName}."
        } else {
            echo "Backup process encountered and error while restoring ${dbName}."
            echo "Possibility of error are as follows: "
            echo "1. Backup location ${backupFile.getParentFile().getPath()} does not exists."
            echo "2. Backup location ${backupPath} is not accessible."
        }
	}
	return true
}

/*
=================================================================================
Method : dbdrop
Description: Drop a db
Parameter:
	dbServer - Database server name
	dbName - Database that should be dropped

==================================================================================
*/
def dbdrop(dbServer, dbName){
	node('L5-WINDOWSSLAVE'){
		def dropQuery = "${DBDROP_SCRIPT_PATH}"
		bat "${SQLCMD} -S ${dbServer} -W -Q ${dropQuery}"
	}
	return true
}


/*
=================================================================================
Method : dbdropSnapshot
Description: Drop a dbsnapshot
Parameter:
	dbServer - Database server name
	dbName - Database that should be dropped

==================================================================================
*/
def dbdropSnapshot(dbServer, dbName){
	def returnstat = 'true'
	try{
		node('L5-WINDOWSSLAVE'){
			def dropQuery = /" DECLARE @Database varchar(50) = '${dbName}'
							  DECLARE @Snapshot varchar(150) = (SELECT TOP 1 name FROM sys.databases d

							  WHERE d.source_database_id /*Snapshot*/ IS NOT NULL
							  AND name LIKE @Database + '%%')

							  IF (@Snapshot IS NOT NULL)
							  Begin
								 EXEC ('DROP DATABASE[' + @Snapshot + ']')
							  End"/

			bat "${SQLCMD} -S ${dbServer} -W -Q ${dropQuery}"
		}
	}
	catch(e){
		echo "${e}"
		returnstat = 'false'

	}
	return returnstat
}

/*
=================================================================================
Method : dbrestore
Description: Restore a db
Parameter:
	dbServer - Database server name
	dbName - Database whose backup should be taken
	backuppath - where the backup file is stored

env variables:
	env.SQLCMD - SQLCMD path
return -

==================================================================================
*/
def dbrestore(dbServer, dbName, backupPath) {
    node('L5-WINDOWSSLAVE'){
        def backupFile = new File("${backupPath}")
        // If it exist, restore backup
        if( backupFile.exists() ) {
            def restoreQuery = /"RESTORE DATABASE [${dbName}] FROM DISK=N'${backupPath}' WITH FILE=1, NORECOVERY"/
		    bat "${SQLCMD} -S ${dbServer} -W -Q ${restoreQuery}"

            echo "Sucessfully restored ${dbName}."
        } else {
            echo "Backup restore process encountered and error while restoring ${dbName}."
            echo "Possibility of error are as follows: "
            echo "1. Backup file ${backupPath} does not exists."
            echo "2. Backup location ${backupPath} is not accessible."
        }
	}

	return true
}


/*
=================================================================================
Description: Upload the property file in artifactory
Parameter:
	appName - Application Name
	serverproperties - Path of the properties file


return - nothing

==================================================================================
*/
def uploadproperties(appName, serverproperties){

	echo "Inside uploadproperties: serverproperties is ${serverproperties}"
	//def server = Artifactory.server "SA_artifactory"
	def server = Artifactory.server "StateAuto_Artifactory"
	def buildInfo = Artifactory.newBuildInfo()
	buildInfo.env.capture = true
	buildInfo.env.collect()

	def uploadSpec = """{
	"files": [
	  {
		"pattern": "${serverproperties}",
		"target": "StateAuto-Artifacts/${appName}/"
	  }
	]
	}"""
	server.upload(uploadSpec)
	server.upload spec: uploadSpec, buildInfo: buildInfo
	server.publishBuildInfo buildInfo

	return

}
