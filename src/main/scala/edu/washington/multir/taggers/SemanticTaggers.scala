package edu.washington.multir.taggers

import edu.knowitall.tool.chunk.ChunkedToken
import edu.knowitall.tool.stem.Lemmatized
import edu.knowitall.taggers.tag.TaggerCollection
import edu.knowitall.taggers.Type
import edu.knowitall.tool.stem.MorphaStemmer

object SemanticTaggers {
    
  private lazy val EducationalOrganizationTagger = loadTagger("/edu/washington/multir/taggers/EducationalOrganizationTaggers")

  private lazy val ReligionTagger = loadTagger("/edu/washington/multir/taggers/ReligionTaggers")

  private lazy val JobTitleTagger = loadTagger("/edu/washington/multir/taggers/JobTitleTaggers")
    
  private lazy val CrimeTagger = loadTagger("/edu/washington/multir/taggers/CrimeTaggers")
    
  private def loadTagger(resourcePath: String) = {
    val url = getClass.getResource(resourcePath)
    require(url != null, "Could not find resource: " + resourcePath)
    TaggerCollection.fromPath(url.getPath())
  }
  
}