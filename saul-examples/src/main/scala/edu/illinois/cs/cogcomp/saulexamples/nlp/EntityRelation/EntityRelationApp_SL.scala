package edu.illinois.cs.cogcomp.saulexamples.nlp.EntityRelation

import edu.illinois.cs.cogcomp.saul.classifier.SL_model._
import edu.illinois.cs.cogcomp.saulexamples.nlp.EntityRelation.EntityRelationConstrainedClassifiers._
import edu.illinois.cs.cogcomp.saulexamples.nlp.EntityRelation.EntityRelationDataModel._
/** Created by Parisa on 12/8/15.
  */
object EntityRelationApp_SL extends App {

  EntityRelationDataModel.populateWithConllSmallSet()

  val cls = List(PerConstrainedClassifier, OrgConstrainedClassifier, LocConstrainedClassifier,
    LivesIn_PerOrg_relationConstrainedClassifier, WorksFor_PerOrg_ConstrainedClassifier)

  val m = StructuredLearning(pairs, cls)
  StructuredLearning.Evaluate(pairs, cls, "")
}

