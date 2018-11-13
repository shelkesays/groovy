def dbbackup(dbServer, dbName, backupPath){
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
	return true
}

def dbdrop(dbServer, dbName){
  def dropQuery = "${DBDROP_SCRIPT_PATH}"
	bat "${SQLCMD} -S ${dbServer} -W -Q ${dropQuery}"
	return true
}

def dbrestore(dbServer, dbName, backupPath) {
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

	return true
}

