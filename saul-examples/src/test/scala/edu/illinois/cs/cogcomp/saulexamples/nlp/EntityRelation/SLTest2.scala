package edu.illinois.cs.cogcomp.saulexamples.nlp.EntityRelation
import edu.illinois.cs.cogcomp.lbjava.infer.OJalgoHook
import edu.illinois.cs.cogcomp.saul.classifier.SL_model._
import edu.illinois.cs.cogcomp.saul.classifier.{ConstrainedClassifier, Learnable, SparseNetworkLBP}
import edu.illinois.cs.cogcomp.saul.datamodel.DataModel
import edu.illinois.cs.cogcomp.saul.datamodel.property.Property
import edu.illinois.cs.cogcomp.saulexamples.EntityMentionRelation.datastruct.ConllRelation
import edu.illinois.cs.cogcomp.sl.core.SLParameters
import edu.illinois.cs.cogcomp.sl.learner.LearnerFactory
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by Parisa on 7/2/16.
 */
class SLTest2 extends FlatSpec with Matchers {
  object testModel extends DataModel {
    val tokens = node[String]
    val pairs = edge(tokens, tokens)
    val testLabel = property(tokens) { x:String => x.equals("candidate") }
    val word = property(tokens) { x:String => x }
    val biWord = property(tokens) { x:String => x + "-" + x }
  }
  // Testing the original functions with real classifiers
 // "integration test" should "work" in {
    // Initialize toy model
    import testModel._
    object TestClassifier extends Learnable(tokens) {
      def label: Property[String] = testLabel

      override def feature = using(word)

      override lazy val classifier = new SparseNetworkLBP
    }
    object TestBiClassifier extends Learnable(tokens) {
      def label: Property[String] = testLabel

      override def feature = using(word, biWord)

      override lazy val classifier = new SparseNetworkLBP
    }
    object TestConstraintClassifier extends ConstrainedClassifier[String, String](TestClassifier) {
      def subjectTo = null

      override val pathToHead = Some(-pairs)

      override def filter(t: String, h: String): Boolean = t.equals(h)

      override val solver = new OJalgoHook
    }
    object TestBiConstraintClassifier extends ConstrainedClassifier[String, String](TestBiClassifier) {
      def subjectTo = null

      override val pathToHead = Some(-pairs)

      override def filter(t: String, h: String): Boolean = t.equals(h)

      override val solver = new OJalgoHook
    }
    val words_train = List("this", "is", "a", "candidate")
    val words_test = List("this" , "was", "not", "true")
    tokens.populate(words_train)
    tokens.populate(words_test, train = false)

    val cls = List(TestConstraintClassifier, TestBiConstraintClassifier)
    val cls_base = List(TestClassifier,TestBiClassifier)

    // This should combine the weights
    val m = StructuredLearning(tokens, cls, initialize = false)



    val SLProblem = SL_IOManager.makeSLProblem(tokens, cls)
    "Structured output learning (SL)" should "get correct number of instances." in {
      SLProblem.goldStructureList.size() should be(4)
      SLProblem.instanceList.size() should be(4)
    }

    val model = Initialize(SLProblem, new SaulSLModel(cls))
    "Structured output learning (SL) initialization" should "work." in {
      model.Factors.size should be(2)
      model.LTUWeightTemplates.size should be(4)
      model.wv.getLength should be(24)
      model.wv.getWeightArray.filter(p => (p > 0.00)).isEmpty should be(true)
    }

    val xGold = SLProblem.instanceList.get(0)
    val yGold = SLProblem.goldStructureList.get(0)
    model.featureGenerator = new SL_FeatureGenerator(model)

    "Structured output learning (SL) Feature extraction" should "work." in {
      model.featureGenerator.getFeatureVector(xGold, yGold).getValues.sum should be(3.0)
    }

    val para = new SLParameters
    para.loadConfigFile("./config/DCD.config")
    model.para = para
    model.infSolver = new Saul_SL_Inference[String](model.Factors.toList, model.LTUWeightTemplates)
    val learner = LearnerFactory.getLearner(model.infSolver, model.featureGenerator, para)

    "Structured output learning's loss" should " be calculate correctly." in {

      val yTest = new Saul_SL_Label_Structure[String](cls, yGold.asInstanceOf[Saul_SL_Label_Structure[String]].head.asInstanceOf[String])
      yTest.labels = for (
        l <- yGold.asInstanceOf[Saul_SL_Label_Structure[ConllRelation]].labels
      ) yield "true"

      model.infSolver.getLoss(xGold, yGold, yGold) should be(0.00)
      model.infSolver.getLoss(xGold, yGold, yTest) >= (0.8) should be(true)
    }

    val weight = learner.train(SLProblem, model.wv)
    "Structured output learning" should " have a correctly working inference module." in {
      val yPredicted = model.infSolver.getBestStructure(model.wv, xGold)
      val yMostViolated = model.infSolver.getLossAugmentedBestStructure(model.wv, xGold, yGold)
      model.infSolver.getLoss(xGold, yGold, yPredicted) should be(0.00)
      model.infSolver.getLoss(xGold, yPredicted, yMostViolated) should be(0.00)
      (yPredicted.asInstanceOf[Saul_SL_Label_Structure[ConllRelation]].equals(yMostViolated.
        asInstanceOf[Saul_SL_Label_Structure[ConllRelation]])) should be(true)
    }

    "Structured output learning (SL)" should "really work in Saul." in {

    }

 // }
}
