/*
 * Get remote tag list from git repository.
 */
def getRemoteTagList(String gitUrl) {
  def gettags = ("git ls-remote --tags ${gitUrl}").execute()

  def tagList = gettags.text.readLines()
         .collect { it.split()[1].replaceAll('refs/tags/', '')  }
         .unique()
         .findAll { it.startsWith('hello') }
         .reverse()

  // Removing duplicates and all the entries with ^{} at the end
  tagList.removeAll { it.endsWith('^{}') }
  return tagList
}

/*
 * Get Latest Remote Tag from git repository
 */
def getLatestRemoteTag(String gitUrl) {
  def tagList = getRemoteTagList(gitUrl)
  return tagList?.find { true }
}

/*
 * Get latest tag from local git repository
 */
def getLatestLocalTag() {
  def gettags = ("git tag --sort=-creatordate").execute()

  def latesttag = gettags.text.readLines()

  return latesttag?.find { true }
}

/**
 * Get remote tag map based on the predefined structure
 *
 * Tag must be in the format: {Task-Name}_{Branch-name}_{Build-version}_{Timestamp-of-tag-creation}
 *
 * Tag creation timestamp should be in the format: {year}-{month}-{date}-{TimeInHours}
 *
 * Time in hours should be in the format: {hours}{minutes} => Without any space or special character in
 * between.
 */
def getRemoteTagMap(String gitUrl, boolean sortDesc = false) {
  def remoteTagList = getRemoteTagList(gitUrl)
  def tagMap = [:]
  for(String tag in remoteTagList) {
    def tagSplit = tag.split('_')
    if(tagSplit.length == 4) {
      try{
        Date tagDate = Date.parse('yyyy-mm-dd-HHmm', tagSplit.last())
        tagMap.put(tag, tagDate)
      } catch (java.text.ParseException e) {
        // e.printStackTrace()
      }
    }
  }

  if(sortDesc) {
    tagMap = tagMap.sort { a, b -> b.value <=> a.value }
  }

  return tagMap;
}

/*
 * Get first key from map provided
 */
def getFirstKeyFromMap(tagMap) {
  return tagMap.keySet().iterator().next()
}

def String gitUrl = "https://github.com/srahul07/learning-js.git"
remoteTagMap = getRemoteTagMap(gitUrl, true)
println("Sorted: " + getFirstKeyFromMap(remoteTagMap))
println("Remote: " + getLatestRemoteTag(gitUrl))
println("Local: " + getLatestLocalTag())

