package edu.illinois.cs.cogcomp.saulexamples.nlp.EntityRelation

import edu.illinois.cs.cogcomp.lbjava.infer.OJalgoHook
import edu.illinois.cs.cogcomp.saul.classifier.SL_model.{Saul_SL_Inference, StructuredLearning}
import edu.illinois.cs.cogcomp.saul.classifier.{ConstrainedClassifier, Learnable, SparseNetworkLBP}
import edu.illinois.cs.cogcomp.saul.datamodel.DataModel
import edu.illinois.cs.cogcomp.saul.datamodel.property.Property
import edu.illinois.cs.cogcomp.sl.util.WeightVector
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ListBuffer

/**
 * Created by Parisa on 6/30/16.
 */
class weightTest extends FlatSpec with Matchers
{

  "weights initializer" should "join multiple weight vectors" in {
    var lt: ListBuffer[Array[Float]] = ListBuffer()
    var wvLength = 0
    // Add 3 factors (size 3, 4, 7)
    val length1 = 3
    val length2 = 4
    val length3 = 7
    val t1: Array[Float] = Array(4.0f, 6.0f, 8.0f)
    wvLength = wvLength + length1
    val t2: Array[Float] = Array(-8.0f, -6.0f, -4.0f, 0.0f)
    wvLength = wvLength + length2
    val t3: Array[Float] = Array(14.0f, 16.0f, 18.0f, 24.0f, 35.5f, 78.32f, 567.865f)
    wvLength = wvLength + length3

    // Append the individual vectors to the buffer
    lt = lt :+ t1
    lt = lt :+ t2
    lt = lt :+ t3

    // Create the complete weight vector
    val myWeight = Array(lt.flatten: _*)
    val wv = new WeightVector(myWeight)
    wv.getWeightArray should be (Array(4.0f, 6.0f, 8.0f, -8.0f, -6.0f, -4.0f, 0.0f, 14.0f, 16.0f, 18.0f, 24.0f, 35.5f, 78.32f, 567.865f))
  }

  "weight update" should "distribute the joint weight vector" in {
    var lt: ListBuffer[Array[Float]] = ListBuffer()
    var wvLength = 0
    // Add 3 factors (size 3, 4, 7)
    val t1: Array[Float] = Array(4.0f, 6.0f, 8.0f)
    wvLength = wvLength + t1.length
    val t2: Array[Float] = Array(-8.0f, -6.0f, -4.0f, 0.0f)
    wvLength = wvLength + t2.length
    val t3: Array[Float] = Array(14.0f, 16.0f, 18.0f, 24.0f, 35.5f, 78.32f, 567.865f)
    wvLength = wvLength + t3.length

    // Append the individual vectors to the buffer
    lt = lt :+ t1
    lt = lt :+ t2
    lt = lt :+ t3

    // Create the complete weight vector
    val myWeight = Array(lt.flatten: _*)
    val wv = new WeightVector(myWeight)

    var offset = 0
    val factorWeight1 = wv.getWeightArray.slice(offset, offset + t1.length)
    factorWeight1 should be (Array(4.0f, 6.0f, 8.0f))
    offset = offset + t1.length

    val factorWeight2 = wv.getWeightArray.slice(offset, offset + t2.length)
    factorWeight2 should be (Array(-8.0f, -6.0f, -4.0f, 0.0f))
    offset = offset + t2.length

    val factorWeight3 = wv.getWeightArray.slice(offset, offset + t3.length)
    factorWeight3 should be (Array(14.0f, 16.0f, 18.0f, 24.0f, 35.5f, 78.32f, 567.865f))
  }

  // Testing the original functions with real classifiers
  "integration test" should "work" in {
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

    // THIS DOESN'T WORK
    val lex = TestConstraintClassifier.onClassifier.classifier.asInstanceOf[SparseNetworkLBP].getLexicon
    print(lex.size())
    val words = List("this", "is", "a", "candidate")
    tokens.populate(words)
    val cls = List(TestConstraintClassifier, TestBiConstraintClassifier)
    // This should combine the weights
    val m = StructuredLearning(tokens, cls, initialize = false)

    m.Factors.size should be (2)

    m.wv.getLength should be (24)

   // This should distribute the weights
    m.infSolver.asInstanceOf[Saul_SL_Inference[String]].updateWeights(m.wv)
//
  }

  object testModel extends DataModel {
    val tokens = node[String]
    val pairs = edge(tokens, tokens)
    val testLabel = property(tokens) { x:String => x.equals("candidate") }
    val word = property(tokens) { x:String => x }
    val biWord = property(tokens) { x:String => x + "-" + x }
  }
}
