def dbBackup = {dbServer, dbName, backupPath ->
    def backupFile = "Okay.bak"
    def dbBackupFile = "${backupPath}" + File.separator + "${backupFile}"

    def query = "BACKUP DATABASE ${dbName} TO DISK=N'${dbBackupFile}'"
    return query
}

def test = dbBackup("Test", "Test", "/home/rahul/git")
print("Separator: ${test} ====> ")
