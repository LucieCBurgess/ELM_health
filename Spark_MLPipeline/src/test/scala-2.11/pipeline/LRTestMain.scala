package pipeline

import data_load.SparkSessionTestWrapper

/**
  * Created by lucieburgess on 25/08/2017.
  */
object LRTestMain extends SparkSessionTestWrapper {

  def main(args: Array[String]): Unit = {

    /** Define a number of parameters for this logistic regression model using org.apache.spark.ml.classification.LogisticRegression
      * Set the layout for using the parsing the parameters using Scopt, https://github.com/scopt/scopt, simple command line options parsing
      */
    val defaultParams = LRTestParams()

    val parser = new scopt.OptionParser[LRTestParams]("Logistic Regression test using src/main/scala/org/apache/spark/examples/ml/LogisticRegressionExample.scala") {

      head("Logistic Regression parameters")

      opt[Double]("regParam").text(s"regularization parameter, default: ${defaultParams.regParam}")
        .action((x, c) => c.copy(regParam = x))

      opt[Double]("elasticNetParam").text(s"Elastic net parameter in range [0,1], default :${defaultParams.elasticNetParam}")
        .action((x, c) => c.copy(elasticNetParam = x))

      opt[Int]("maxIter").text(s"Maximum number of iterations for gradient descent, default: ${defaultParams.maxIter}")
        .action((x, c) => c.copy(maxIter = x))

      opt[Boolean]("fitIntercept").text(s"Fit intercept parameter, whether to fit an intercept, default: ${defaultParams.fitIntercept}")
        .action((x, c) => c.copy(fitIntercept = x))

      opt[Double]("tol").text(s"Parameter for the convergence tolerance for iterative algorithms, " +
        s" with smaller value leading to higher accuracy but greatest cost of more iterations, default: ${defaultParams.tol}")
        .action((x, c) => c.copy(tol = x))

      opt[Double]("fracTest").text(s"The fraction of the data to use for testing, default: ${defaultParams.fracTest}")
        .action((x, c) => c.copy(fracTest = x))

      opt[String]("dataFormat").text(s"Please enter the dataformat here, default: ${defaultParams.dataFormat}")
        .action((x, c) => c.copy(dataFormat = x))

      checkConfig { params =>
        if (params.fracTest < 0 || params.fracTest >= 1) {
          failure(s"fracTest ${params.fracTest} value is incorrect; it should be in range [0,1).")
        } else {
          success
        }
      }
    }

    parser.parse(args, defaultParams) match {
      case Some(params) => LogisticRegressionTest.run(params)
      case _ => sys.exit(1)
    }

  }

  spark.stop()
}
