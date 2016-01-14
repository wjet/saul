package edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.{ Constituent, Relation, TextAnnotation }
import edu.illinois.cs.cogcomp.core.datastructures.trees.Tree
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager
import edu.illinois.cs.cogcomp.edison.features.factory._
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CoNLLColumnFormatReader
import edu.illinois.cs.cogcomp.saul.datamodel.DataModel
import edu.illinois.cs.cogcomp.saulexamples.ExamplesConfigurator
import edu.illinois.cs.cogcomp.saulexamples.data.SRLFrameManager
import edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling.srlClassifiers.{ argumentTypeLearner, argumentXuIdentifierGivenApredicate, predicateClassifier }
import edu.illinois.cs.cogcomp.saulexamples.nlp.SemanticRoleLabeling.srlSensors._

import scala.collection.JavaConversions._

/** Contains all the SRL-related `Nodes`, `Edges` and `Properties` (labels and features)
  * @author Parisa Kordjamshidi
  * @author Christos Christodoulopoulos
  */
object srlDataModel extends DataModel {

  val rm: ResourceManager = new ExamplesConfigurator().getDefaultConfig

  val parseViewName: String = rm.getString(ExamplesConfigurator.SRL_PARSE_VIEW)

  val frameManager: SRLFrameManager = new SRLFrameManager(rm.getString(ExamplesConfigurator.PROPBANK_HOME.key))

  val predicates = node[Constituent]

  val arguments = node[Constituent]

  val relations = node[Relation]

  //val onTheFlyRelationNode= join(predicates, arguments)

  val sentences = node[TextAnnotation]

  val trees = node[Tree[Constituent]]

  val stringTree = node[Tree[String]]

  val tokens = node[Constituent]

  val sentencesToTrees = edge(sentences, trees)
  val sentencesToStringTree = edge(sentences, stringTree)
  val sentencesToTokens = edge(sentences, tokens)
  val sentencesToRelations = edge(sentences, relations)
  val relationsToPredicates = edge(relations, predicates)
  val relationsToArguments = edge(relations, arguments)

  sentencesToRelations.addSensor(textAnnotationToRelation _)
  sentencesToRelations.addSensor(textAnnotationToRelationMatch _)
  relationsToArguments.addSensor(relToArgument _)
  relationsToPredicates.addSensor(relToPredicate _)
  sentencesToStringTree.addSensor(textAnnotationToStringTree _)

  /** This can be applied to both predicates and arguments */
  val address = property(predicates, "add") {
    x: Constituent => x.getTextAnnotation.getCorpusId + ":" + x.getTextAnnotation.getId + ":" + x.getSpan
  }

  // Classification labels
  val isPredicateGold = property(predicates, "p") {
    x: Constituent => x.getLabel.equals("Predicate")
  }
  val predicateSenseGold = property(predicates, "s") {
    x: Constituent => x.getAttribute(CoNLLColumnFormatReader.SenseIdentifer)
  }

  val isArgumentXuGold = property(relations, "aX") {
    x: Relation => !x.getRelationName.equals("candidate")
  }
  val argumentLabelGold = property(relations, "l") {
    r: Relation => r.getRelationName
  }

  // Features properties
  val posTag = property(predicates, "posC") {
    x: Constituent => getPOS(x)
  }

  val subcategorization = property(predicates, "subcatC") {
    x: Constituent => fexFeatureExtractor(x, new SubcategorizationFrame(parseViewName))
  }

  val phraseType = property(predicates, "phraseTypeC") {
    x: Constituent => fexFeatureExtractor(x, new ParsePhraseType(parseViewName))
  }

  val headword = property(predicates, "headC") {
    x: Constituent => fexFeatureExtractor(x, new ParseHeadWordPOS(parseViewName))
  }

  val syntacticFrame = property(predicates, "synFrameC") {
    x: Constituent => fexFeatureExtractor(x, new SyntacticFrame(parseViewName))
  }
  val path = property(predicates, "pathC") {
    x: Constituent => fexFeatureExtractor(x, new ParsePath(parseViewName))
  }

  val subcategorizationRelation = property(relations, "subcat") {
    x: Relation => fexFeatureExtractor(x.getTarget, new SubcategorizationFrame(parseViewName))
  }

  val phraseTypeRelation = property(relations, "phraseType") {
    x: Relation => fexFeatureExtractor(x.getTarget, new ParsePhraseType(parseViewName))
  }

  val headwordRelation = property(relations, "head") {
    x: Relation => fexFeatureExtractor(x.getTarget, new ParseHeadWordPOS(parseViewName))
  }

  val syntacticFrameRelation = property(relations, "synFrame") {
    x: Relation => fexFeatureExtractor(x.getTarget, new SyntacticFrame(parseViewName))
  }

  val pathRelation = property(relations, "path") {
    x: Relation => fexFeatureExtractor(x.getTarget, new ParsePath(parseViewName))
  }

  val predPosTag = property(relations, "pPos") {
    x: Relation => getPOS(x.getSource)
  }

  val predLemmaR = property(relations, "pLem") {
    x: Relation => getLemma(x.getSource)
  }

  val predLemmaP = property(predicates, "pLem") {
    x: Constituent => getLemma(x)
  }

  val linearPosition = property(relations, "position") {
    x: Relation => fexFeatureExtractor(x.getTarget, new LinearPosition())
  }

  val voice = property(predicates, "voice") {
    x: Constituent => fexFeatureExtractor(x, new VerbVoiceIndicator(parseViewName))
  }

  val predWordWindow = property(predicates, "predWordWindow") {
    x: Constituent => fexContextFeats(x, WordFeatureExtractorFactory.word)
  }

  val predPOSWindow = property(predicates, "predPOSWindow") {
    x: Constituent => fexContextFeats(x, WordFeatureExtractorFactory.pos)
  }

  val argWordWindow = property(relations, "argWordWindow") {
    rel: Relation => fexContextFeats(rel.getTarget, WordFeatureExtractorFactory.word)
  }

  val argPOSWindow = property(relations, "argPOSWindow") {
    rel: Relation => fexContextFeats(rel.getTarget, WordFeatureExtractorFactory.pos)
  }

  val verbClass = property(predicates, "verbClass") {
    x: Constituent => frameManager.getAllClasses(getLemma(x)).toList
  }

  val constituentLength = property(relations, "constLength") {
    rel: Relation => rel.getTarget.getEndSpan - rel.getTarget.getStartSpan
  }

  val chunkLength = property(relations, "chunkLength") {
    rel: Relation => rel.getTarget.getTextAnnotation.getView(ViewNames.SHALLOW_PARSE).getConstituentsCovering(rel.getTarget).length
  }

  val chunkEmbedding = property(relations, "chunkEmbedding") {
    rel: Relation => fexFeatureExtractor(rel.getTarget, new ChunkEmbedding(ViewNames.SHALLOW_PARSE))
  }

  val chunkPathPattern = property(relations, "chunkPath") {
    rel: Relation => fexFeatureExtractor(rel.getTarget, new ChunkPathPattern(ViewNames.SHALLOW_PARSE))
  }

  /** Combines clause relative position and clause coverage */
  val clauseFeatures = property(relations, "clauseFeats") {
    rel: Relation =>
      val clauseViewName = if (parseViewName.equals(ViewNames.PARSE_GOLD)) "CLAUSES_GOLD" else ViewNames.CLAUSES_STANFORD
      fexFeatureExtractor(rel.getTarget, new ClauseFeatureExtractor(parseViewName, clauseViewName))
  }

  val containsNEG = property(relations, "containsNEG") {
    rel: Relation => fexFeatureExtractor(rel.getTarget, ChunkPropertyFeatureFactory.isNegated)
  }

  val containsMOD = property(relations, "containsMOD") {
    rel: Relation => fexFeatureExtractor(rel.getTarget, ChunkPropertyFeatureFactory.hasModalVerb)
  }

  // Frame properties
  val legalSenses = property(relations, "legalSens") {
    x: Relation => frameManager.getLegalSenses(predLemmaR(x)).toList

  }

  val legalArguments = property(predicates, "legalArgs") {
    x: Constituent => frameManager.getLegalArguments(predLemmaP(x)).toList
  }

  // Classifiers as properties
  val isPredicatePrediction = property(predicates, "isPredicatePrediction") {
    x: Constituent => predicateClassifier(x)
  }

  val isArgumentPrediction = property(relations, "isArgumentPrediction") {
    x: Relation => argumentXuIdentifierGivenApredicate(x)
  }

  val isArgumentPipePrediction = property(relations, "isArgumentPipePrediction") {
    x: Relation =>
      predicateClassifier(x.getSource) match {
        case "false" => "false"
        case _ => argumentXuIdentifierGivenApredicate(x)

      }
  }

  val typeArgumentPrediction = property(relations, "typeArgumentPrediction") {
    x: Relation =>
      argumentTypeLearner(x)
  }

  val typeArgumentPipePrediction = property(relations, "typeArgumentPipePrediction") {
    x: Relation =>
      val a: String = predicateClassifier(x.getSource) match {
        case "false" => "false"
        case _ => argumentXuIdentifierGivenApredicate(x)
      }
      val b = a match {
        case "false" => "false"
        case _ => argumentTypeLearner(x)
      }
      b
  }
}
