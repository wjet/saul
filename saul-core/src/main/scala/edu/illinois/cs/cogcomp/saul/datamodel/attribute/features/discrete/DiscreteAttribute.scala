package edu.illinois.cs.cogcomp.saul.datamodel.attribute.features.discrete

import edu.illinois.cs.cogcomp.lbjava.classify.{ DiscretePrimitiveStringFeature, Feature, FeatureVector, Classifier }
import edu.illinois.cs.cogcomp.saul.datamodel.attribute.TypedAttribute
import edu.illinois.cs.cogcomp.saul.datamodel.attribute.features.ClassifierContainsInLBP

import scala.reflect.ClassTag

case class DiscreteAttribute[T <: AnyRef](
  name: String,
  sensor: T => String,
  range: Option[List[String]]
)(implicit val tag: ClassTag[T]) extends TypedAttribute[T, String] {
  override def makeClassifierWithName(name: String): Classifier = range match {
    case Some(r) =>
      new ClassifierContainsInLBP() {
        private var __allowableValues: Array[String] = r.toArray.asInstanceOf[Array[String]]

        this.containingPackage = "LBP_Package"
        this.name = name

        def getAllowableValues: Array[String] = {
          __allowableValues
        }

        override def allowableValues: Array[String] = {
          __allowableValues
        }

        def classify(instance: AnyRef): FeatureVector = {
          new FeatureVector(featureValue(instance))
        }

        override def featureValue(instance: AnyRef): Feature = {
          val result: String = discreteValue(instance)
          new DiscretePrimitiveStringFeature(containingPackage, this.name, "", result,
            valueIndexOf(result), allowableValues.length.asInstanceOf[Short])
        }

        override def discreteValue(instance: AnyRef): String = {
          // TODO: catching errors (type checking)
          _discreteValue(instance)
        }

        private def _discreteValue(__example: AnyRef): String = {
          val t: T = __example.asInstanceOf[T]
          self.sensor(t).mkString("")
        }
      }
    case _ => new ClassifierContainsInLBP {

      this.containingPackage = "LBP_Package"
      this.name = name

      def classify(instance: AnyRef): FeatureVector = {
        new FeatureVector(featureValue(instance))
      }

      override def featureValue(instance: AnyRef): Feature = {
        val result: String = discreteValue(instance)
        new DiscretePrimitiveStringFeature(containingPackage, this.name, "", result,
          valueIndexOf(result), allowableValues.length.toShort)
      }

      override def discreteValue(__example: AnyRef): String = {
        val d: T = __example.asInstanceOf[T]
        "" + sensor(d)
      }

      override def classify(examples: Array[AnyRef]): Array[FeatureVector] = {
        super.classify(examples)
      }
    }
  }

  override def addToFeatureVector(t: T, featureVector: FeatureVector): FeatureVector = {
    featureVector.addFeature(this.classifier.featureValue(t))
    featureVector
  }

  def addToFeatureVector(t: T, featureVector: FeatureVector, nameOfClassifier: String): FeatureVector = {
    featureVector.addFeature(makeClassifierWithName(nameOfClassifier).featureValue(t))
    featureVector
  }
}