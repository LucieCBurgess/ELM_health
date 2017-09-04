package dev.elm

import org.apache.spark.ml.classification.ClassificationModel
import org.apache.spark.ml.linalg.{Vector, DenseMatrix => SDM, DenseVector => SDV}
import breeze.linalg.{DenseMatrix => BDM, DenseVector => BDV, Matrix => BM, Vector => BV}
import dev.data_load.SparkSessionWrapper
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.DefaultParamsWritable

/**
  * Created by lucieburgess on 27/08/2017.
  * This class is a Transformer according to the Spark Pipeline model, and extends ClassificationModel
  * The Classifier is trained with ELMClassifier from the algorithm ELMClassifierAlgo
  *
  * This uses the default implementation of transform(), which reads column "features" and
  * outputs columns "prediction". 'rawPrediction" is calculated for each labelled point in the dataset
  * from the parameters passed into ELMClassifier, based on the ELMClassifier trained using column "features".
  * It also uses the default implementation of transformSchema(schema: StructType).
  *
  * This uses the default implementation of predict(), which chooses the label corresponding to
  * the maximum value returned by [[predictRaw()]], as stated in the Classifier Model API
  *
  */
class ELMModel(override val uid: String, val modelWeights: BDM[Double], val modelBias: BDM[Double], val modelBeta: BDV[Double], val modelHiddenNodes: Int, val modelAF: ActivationFunction)
  extends ClassificationModel[Vector, ELMModel] with SparkSessionWrapper
    with ELMParams with DefaultParamsWritable {

  import spark.implicits._

  override def copy(extra: ParamMap): ELMModel = {
    val copied = new ELMModel(uid, modelBias, modelWeights, modelBeta, modelHiddenNodes, modelAF)
    copyValues(copied, extra).setParent(parent)
  }

  /** Number of classes the label can take. 2 indicates binary classification */
  override val numClasses: Int = 2

  /**
    * @param features, the vector of features being input into the model
    * @return vector where element i is the raw prediction for label i. This raw prediction may be any real number,
    *         where a larger value indicates greater confidence for that label
    *         The underlying method in ClassificationModel then predicts raw2prediction, which given a vector of raw predictions
    *         selects the predicted labels. raw2prediction can be overridden to support thresholds which favour particular labels.
    */
  override protected def predictRaw(features: Vector): Vector = {

      val array: Array[Double] = features.toArray
      val X: BDM[Double] = new BDM(features.size, numFeatures, array) //numFeatures is calculated by Classifier and passed to ClassificationModel as part of the API
      val beta = modelBeta
      val L = modelHiddenNodes
      val bias = modelBias
      val w = modelWeights
      val predictedLabels = new Array[Double](features.size) // length of features vector should be number of training samples
//      for (i <- 0 until features.size) { // for i <- 0 until N, for j <- 0 until L
//      val node: IndexedSeq[Double] = for (j <- 0 until L) yield (beta(j) * modelAF.function(w(j, ::) * X(i, ::).t + bias(j)))
//        predictedLabels(i) = node.sum.round.toDouble
//      }
      new SDV(predictedLabels)
  }
}
