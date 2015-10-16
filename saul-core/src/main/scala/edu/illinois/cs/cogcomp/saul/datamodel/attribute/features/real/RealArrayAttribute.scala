package edu.illinois.cs.cogcomp.saul.datamodel.attribute.features.real

import edu.illinois.cs.cogcomp.lbjava.classify.{ RealArrayStringFeature, FeatureVector, Classifier }
import edu.illinois.cs.cogcomp.saul.datamodel.attribute.TypedAttribute
import edu.illinois.cs.cogcomp.saul.datamodel.attribute.features.ClassifierContainsInLBP

import scala.reflect.ClassTag

case class RealArrayAttribute[T <: AnyRef](
  name: String,
  sensor: T => List[Double]
)(implicit val tag: ClassTag[T]) extends RealAttributeCollection[T] with TypedAttribute[T, List[Double]] {

  // TODO: shouldn't this be this.name?
  val ra = this

  override def makeClassifierWithName(name: String): Classifier = new ClassifierContainsInLBP() {
    this.containingPackage = "LBP_Package"

    this.name = name

    override def realValueArray(instance: AnyRef): Array[Double] = {
      classify(instance).realValueArray
    }

    override def classify(instance: Array[AnyRef]): Array[FeatureVector] = {
      super.classify(instance)
    }

    def classify(example: AnyRef): FeatureVector = {

      val d: T = example.asInstanceOf[T]
      val values = sensor(d)

      var featureVector = new FeatureVector

      values.zipWithIndex.foreach {
        case (value, idx) => featureVector.addFeature(new RealArrayStringFeature(this.containingPackage, this.name, "", value, idx, 0))
      }

      // TODO: Commented by Daniel. Make sure this does not create any bugs
      //(0 to values.size) foreach { x => featureVector.getFeature(x).setArrayLength(values.size) }

      featureVector
    }
  }

  override def addToFeatureVector(t: T, fv: FeatureVector): FeatureVector = {
    fv.addFeatures(this.classifier.classify(t))
    fv
  }

  def addToFeatureVector(t: T, fv: FeatureVector, nameOfClassifier: String): FeatureVector = {
    fv.addFeatures(makeClassifierWithName(nameOfClassifier).classify(t))
    fv
  }
}