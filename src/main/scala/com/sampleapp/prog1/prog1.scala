package com.sampleapp.prog1

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{IndexToString, OneHotEncoder, StringIndexer, VectorAssembler, VectorIndexer}
import org.apache.spark.sql
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression


object prog1 {
    val spark: SparkSession = SparkSession
        .builder()
        .appName("CSVReader")
        .master("local[*]")
        .getOrCreate()
    
    import spark.implicits._
    
    def getTheModel(df : sql.DataFrame): PipelineModel =
    {

        val cols : Array[String] = df.columns
//    TODO seperate labels and features
        val genderIndexer = new StringIndexer().setInputCol("gender").setOutputCol("genderIndex").fit(df)
        val raceIndexer = new StringIndexer().setInputCol("race").setOutputCol("raceIndex").fit(df)
        val lunchIndexer = new StringIndexer().setInputCol("lunch").setOutputCol("lunchIndex").fit(df)
        val mathIndexer = new StringIndexer().setInputCol("math").setOutputCol("mathIndex").fit(df)
        val readingIndexer = new StringIndexer().setInputCol("reading").setOutputCol("readingIndex").fit(df)
        val writingIndexer = new StringIndexer().setInputCol("writing").setOutputCol("writingIndex").fit(df)
    
        val genderEncoder = new OneHotEncoder().setInputCol("genderIndex").setOutputCol("genderVec")
        val raceEncoder = new OneHotEncoder().setInputCol("raceIndex").setOutputCol("raceVec")
        val lunchEncoder = new OneHotEncoder().setInputCol("lunchIndex").setOutputCol("lunchVec")
        val mathEncoder = new OneHotEncoder().setInputCol("mathIndex").setOutputCol("mathVec")
        val readingEncoder = new OneHotEncoder().setInputCol("readingIndex").setOutputCol("readingVec")
        val writingEncoder = new OneHotEncoder().setInputCol("writingIndex").setOutputCol("writingVec")
        
        val assembler = new VectorAssembler()
            .setInputCols(cols)
            .setOutputCol("feature")
    
        val Array(trainingData, testData) = df.randomSplit(Array(0.8, 0.2))
        
        
    
        val lr = new LogisticRegression().setLabelCol("")
        val pipeline = new Pipeline().setStages(Array(genderIndexer, raceIndexer, lunchIndexer, mathIndexer, readingIndexer, writingIndexer, genderEncoder, raceEncoder, lunchEncoder, mathEncoder, readingEncoder, writingEncoder, assembler, lr))
        val model = pipeline.fit(df)
        
        model
    }
    
    def main(args: Array[String]): Unit = {
        //        First load the data
        val path_ = "src/main/static/dataset/data.csv";
        var df = loadData(path_)
        //        Normalize the cols
        df = normalizeTheData(df)
        // Print the data
        printData(df)
        
        
        // Create a model
        val model = getTheModel(df)
        
        //        Then train the model
        
        //        Save the trained model to the disk
        
        spark.close()
        spark.stop()
        
    }
    
    def loadData(path: String): sql.DataFrame = {
        val df = spark
            .read
            .option("header", "true")
            .csv(path)
        df
    }
    
    def normalizeTheData(df: sql.DataFrame): sql.DataFrame = {
        
        var df2 = df.withColumn("math", col("math") / 100)
        df2 = df2.withColumn(colName = "reading", col("reading") / 100)
        df2 = df2.withColumn("writing", col("writing") / 100)
        
        df2
    }
    
    def printData(df: sql.DataFrame): Unit = {
        df.printSchema()
        df.show(10)
    }
}
