package com.sampleapp.prog2

import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.feature.{IndexToString, OneHotEncoderEstimator, StringIndexer, VectorAssembler, VectorIndexer}
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.sql
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col

object prog2 {
    val spark: SparkSession = SparkSession
        .builder()
        .appName("CSVReader")
        .master("local[*]")
        .getOrCreate()
    
    import spark.implicits._
    
    def main(args: Array[String]): Unit =
    {
        val path_ = "src/main/static/dataset/data.csv";
        val modelPath = "src/main/static/models/out";
        var df = loadData(path = path_)
        df = transformData(df)
    
        var model = loadModel(modelPath)
        
        predict(df, model)
        
        spark.close()
        spark.stop()
    }
    
    def loadModel(path_ : String) : RandomForestClassificationModel={
        var x = RandomForestClassificationModel.load(path_)
        x
    }
    
    def loadData(path: String): sql.DataFrame = {
        val data = spark
            .read
            .option("header", "true")
            .csv(path)
        
        val df = (data.select(data("race").as("label"),
            $"gender", $"lunch", $"math",
            $"reading", $"writing"))
        df
    }
    def transformData(df : sql.DataFrame) :sql.DataFrame =
    {
        var df2 = df.withColumn("gender", col("gender").cast("Float"))
        df2 = df2.withColumn("math", (col("math")/100).cast("Float"))
        df2 = df2.withColumn("reading", (col("reading")/100).cast("Float"))
        df2 = df2.withColumn("writing", (col("writing")/100).cast("Float"))
        df2 = df2.withColumn("lunch", col("lunch").cast("Float"))
        
        var assembler = new VectorAssembler()
            .setInputCols(Array("gender","lunch","reading", "writing", "math"))
            .setOutputCol("features")
        
        var output = assembler.transform(df2)
        output.select("label", "features").show()
        output.select("label", "features")
    }
    
    def predict(df : sql.DataFrame, rmfModel : RandomForestClassificationModel): Unit =
    {
        val labelIndexer = new StringIndexer()
            .setInputCol("label")
            .setOutputCol("indexedLabel")
            .fit(df)
        val featureIndexer = new VectorIndexer()
            .setInputCol("features")
            .setOutputCol("indexedFeatures")
            .fit(df)
        
        val Array(trainingData, testData) = df.randomSplit(Array(0.7, 0.3))
        
        val labelConverter = new IndexToString()
            .setInputCol("prediction")
            .setOutputCol("predictedLabel")
            .setLabels(labelIndexer.labels)
        

        
        var a = labelIndexer.transform(testData)
        a = featureIndexer.transform(a)
        var pred_ = rmfModel.transform(a)
        pred_ = labelConverter.transform(pred_)

        pred_.show()


    }
    

}
