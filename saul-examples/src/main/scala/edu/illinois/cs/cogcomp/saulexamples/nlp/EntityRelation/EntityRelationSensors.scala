package edu.illinois.cs.cogcomp.saulexamples.nlp.EntityRelation

import edu.illinois.cs.cogcomp.saulexamples.EntityMentionRelation.datastruct.{ ConllRawToken, ConllRelation, ConllRawSentence }
import edu.illinois.cs.cogcomp.saulexamples.EntityMentionRelation.reader.{ Conll04_Reader, GazeteerReader }

import scala.collection.JavaConverters._

object EntityRelationSensors {
  val path = "../data/"
  val resourcePath = "../saul-examples/src/main/resources/EntityMentionRelation/"

  // Create single instances of Gazeteers and cache then with the object.
  lazy val cityGazetSensor = new GazeteerReader("gazeteer/known_city.lst", "Gaz:City", true, true)
  lazy val personGazetSensor = new GazeteerReader("gazeteer/known_maleFirst.lst", "Gaz:Person", true, true)
  personGazetSensor.addFile("gazeteer/known_femaleFirst.lst", true, true)

  def readConllData(dir: String): (List[ConllRawSentence], List[ConllRelation], List[ConllRawToken]) = {
    val reader = new Conll04_Reader(dir, "Token")
    val sentences = reader.sentences.asScala.toList
    val tokens = sentences.flatMap { a => a.getEntitiesInSentence.asScala }
    (sentences, reader.relations.asScala.toList, tokens)
  }

  lazy val (sentencesAll, relationsAll, entitiesAll) = readConllData(path + "EntityMentionRelation/conll04.corp")
  lazy val (sentencesTrain, relationsTrain, entitiesTrain) = readConllData(path + "EntityMentionRelation/conll04_train.corp")
  lazy val (sentencesTest, relationsTest, entitiesTest) = readConllData(path + "EntityMentionRelation/conll04_test.corp")
  lazy val (sentencesSmallSetTest, testRelationsSmallSetTest, entitiesSmallSetTest) = readConllData(resourcePath + "conll04-smallDocument.txt")

  def sentenceToRelation_GeneratingSensor(s: ConllRawSentence): List[ConllRelation] = {
    s.relations.asScala.toList
  }

  def sentenceToRelations_MatchingSensor(s: ConllRawSentence, t: ConllRelation): Boolean = {
    s.sentId == t.sentId
  }

  def sentenceToTokens_GeneratingSensor(s: ConllRawSentence): List[ConllRawToken] = {
    s.sentTokens.asScala.toList
  }

  def relationToFirstArg_MatchingSensor(r: ConllRelation, t: ConllRawToken): Boolean = {
    r.sentId.equals(t.sentId) && r.e1.wordId == t.wordId
  }

  def relationToSecondArg_MatchingSensor(r: ConllRelation, t: ConllRawToken): Boolean = {
    r.sentId.equals(t.sentId) && r.e2.wordId == t.wordId
  }
}
