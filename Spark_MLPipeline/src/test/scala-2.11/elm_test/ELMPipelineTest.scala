package elm_test

import data_load_test.SparkSessionTestWrapper
import dev.data_load.DataLoad
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.{Estimator, Pipeline, PipelineStage}
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler, VectorSlicer}
import org.apache.spark.sql.functions.{col, monotonically_increasing_id, when}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.scalatest.FunSuite
import scala.collection.mutable

/**
  * Created by lucieburgess on 02/09/2017.
  * Tests for ELM Pipeline.
  */
class ELMPipelineTest extends FunSuite {

    lazy val spark: SparkSession = SparkSession
      .builder()
      .master("local[4]")
      .appName("ELMPipelineTest")
      .getOrCreate()

    import spark.implicits._

    val fileName: String = "smalltest.txt"

    /** Load training and test data and cache it */
    val data = DataLoad.createDataFrame(fileName) match {
      case Some(df) => df
        .filter($"activityLabel" > 0)
        .withColumn("binaryLabel", when($"activityLabel".between(1, 3), 0).otherwise(1))
        .withColumn("uniqueID", monotonically_increasing_id())
      case None => throw new UnsupportedOperationException("Couldn't create DataFrame")
    }

    val datasetSize: Int = data.count().toInt
    val featureCols = Array("acc_Chest_X", "acc_Chest_Y", "acc_Chest_Z")
    val featureAssembler = new VectorAssembler().setInputCols(featureCols).setOutputCol("features")
    val dataWithFeatures: DataFrame = featureAssembler.transform(data)

    test("[01] Vector assembler adds output column of features and can be added to pipeline") {

      val featureCols = Array("acc_Chest_X", "acc_Chest_Y", "acc_Chest_Z", "acc_Arm_X", "acc_Arm_Y", "acc_Arm_Z")
      val featureAssembler = new VectorAssembler().setInputCols(featureCols).setOutputCol("features")
      val preparedData = featureAssembler.transform(data)
      assert(preparedData.select("features").head.get(0).asInstanceOf[Vector].size == 6)

      preparedData.printSchema()
      val pipelineStages = new mutable.ArrayBuffer[PipelineStage]()
      pipelineStages += featureAssembler

      assert(pipelineStages.head == featureAssembler)
    }

    test("[02] Can recreate features using Vector slicer") {

      val featureCols = Array("acc_Chest_X", "acc_Chest_Y", "acc_Chest_Z", "acc_Arm_X", "acc_Arm_Y", "acc_Arm_Z")
      val featureColsIndex = featureCols.map(c => s"${c}_index")

      val indexers = featureCols.map(
        c => new StringIndexer().setInputCol(c).setOutputCol(s"${c}_index")
      )

      val assembler = new VectorAssembler().setInputCols(featureColsIndex).setOutputCol("features")
      val slicer = new VectorSlicer().setInputCol("features").setOutputCol("double_features").setNames(featureColsIndex.init)
      val transformed = new Pipeline().setStages(indexers :+ assembler :+ slicer)
        .fit(data)
        .transform(data)
      transformed.show()
    }
}