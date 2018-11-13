def getRemoteTagList = { gitUrl ->
  def gettags = ("git ls-remote --tags ${gitUrl}").execute()

  def tagList = gettags.text.readLines()
         .collect { it.split()[1].replaceAll('refs/tags/', '')  }
         .unique()
         .findAll { it.startsWith('hello') }
         .reverse()

  tagList.removeAll { it.endsWith('^{}') }
  return tagList
}

def getLatestRemoteTag = {gitUrl ->
  def tagList = getRemoteTagList(gitUrl)
  return tagList?.find { true }
}

def getLatestLocalTag = {->
  def gettags = ("git tag --sort=-creatordate").execute()

  def latesttag = gettags.text.readLines()

  return latesttag?.find { true }
}

/**
 * Tag must be in the format: {Task-Name}_{Branch-name}_{Build-version}_{Timestamp-of-tag-creation}
 *
 * Tag creation timestamp should be in the format: {year}-{month}-{date}-{TimeInHours}
 *
 * Time in hours should be in the format: {hours}{minutes} => Without any space or special character in
 * between.
 */
def getRemoteTagMap = {gitUrl, sortDesc = false ->
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

def getFirstKeyFromMap = {tagMap ->
  return tagMap.keySet().iterator().next()
}

def gitUrl = "https://github.com/srahul07/learning-js.git"
remoteTagMap = getRemoteTagMap(gitUrl, true)
println("Sorted: " + getFirstKeyFromMap(remoteTagMap))
println("Remote: " + getLatestRemoteTag(gitUrl))
println("Local: " + getLatestLocalTag())

