/*
package ml.wolfe.examples.parisa.iJLIS2;

import LBJ2.classify.DiscretePrimitiveStringFeature;
import LBJ2.classify.FeatureVector;
import LBJ2.classify.ScoreSet;
import LBJ2.learn.Learner;

import java.io.PrintStream;

public abstract class work_forClassifier extends Learner
{
  public work_forClassifier() { this(""); }
  public work_forClassifier(String n) { super(n); }

  public void learn(int[] exampleFeatures, double[] exampleValues,
                    int[] exampleLabels, double[] labelValues) { }
  public FeatureVector classify(int[] exampleFeatures, double[] exampleValues)
  { return null; }
  public ScoreSet scores(int[] f, double[] v) { return scores(null); }

  public FeatureVector classify(Object example) {
    String prediction = scores(example).highScoreValue();
    return
      new FeatureVector(
          new DiscretePrimitiveStringFeature(
              containingPackage, name, "", prediction,
              valueIndexOf(prediction), (short) allowableValues().length));
  }

  public void write(PrintStream out) { } 

  public int hashCode() { return name.hashCode(); }
  public boolean equals(Object o) { return getClass().equals(o.getClass()); }
}

*/