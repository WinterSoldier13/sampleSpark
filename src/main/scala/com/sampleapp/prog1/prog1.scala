package com.sampleapp.prog1

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{IndexToString, OneHotEncoder, OneHotEncoderEstimator, StringIndexer, VectorAssembler, VectorIndexer}
import org.apache.spark.sql
import org.apache.spark.sql.{Row, SparkSession, functions}
import org.apache.spark.sql.functions.{array, col}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{LogisticRegression, RandomForestClassifier}
import org.apache.spark.sql.catalyst.ScalaReflection.Schema
import org.apache.spark.sql.types.{StringType, StructField, StructType}


object prog1 {
    val spark: SparkSession = SparkSession
        .builder()
        .appName("CSVReader")
        .master("local[*]")
        .getOrCreate()
    
    import spark.implicits._
    
    def main(args: Array[String]): Unit = {
        //        First load the data
        val path_ = "src/main/static/dataset/data.csv";
        var df = loadData(path_)
        
        
//        TODO this vs that
        df = testFunction(df)
        
//        df = middleMan(df)
//        df = assembleData(df)
//        printData(df)
        
        
        // Create a model

        generateModel(df)
        
        //        Then train the model
        
        //        Save the trained model to the disk
        
        spark.close()
        spark.stop()
        
    }
    
//    def getTheModel(df: sql.DataFrame): PipelineModel = {
//
//        val cols: Array[String] = df.columns
//
//        val genderIndexer = new StringIndexer().setInputCol("gender").setOutputCol("genderIndex").fit(df)
//        val raceIndexer = new StringIndexer().setInputCol("race").setOutputCol("raceIndex").fit(df)
//        val lunchIndexer = new StringIndexer().setInputCol("lunch").setOutputCol("lunchIndex").fit(df)
//        val mathIndexer = new StringIndexer().setInputCol("math").setOutputCol("mathIndex").fit(df)
//        val readingIndexer = new StringIndexer().setInputCol("reading").setOutputCol("readingIndex").fit(df)
//        val writingIndexer = new StringIndexer().setInputCol("writing").setOutputCol("writingIndex").fit(df)
//
//        val genderEncoder = new OneHotEncoder().setInputCol("genderIndex").setOutputCol("genderVec")
//        val raceEncoder = new OneHotEncoder().setInputCol("raceIndex").setOutputCol("raceVec")
//        val lunchEncoder = new OneHotEncoder().setInputCol("lunchIndex").setOutputCol("lunchVec")
//        val mathEncoder = new OneHotEncoder().setInputCol("mathIndex").setOutputCol("mathVec")
//        val readingEncoder = new OneHotEncoder().setInputCol("readingIndex").setOutputCol("readingVec")
//        val writingEncoder = new OneHotEncoder().setInputCol("writingIndex").setOutputCol("writingVec")
//
//        val assembler = new VectorAssembler()
//            .setInputCols(cols)
//            .setOutputCol("feature")
//
//        val Array(trainingData, testData) = df.randomSplit(Array(0.8, 0.2))
//
//
//        val lr = new LogisticRegression().setLabelCol("")
//        val pipeline = new Pipeline().setStages(Array(genderIndexer, raceIndexer, lunchIndexer, mathIndexer, readingIndexer, writingIndexer, genderEncoder, raceEncoder, lunchEncoder, mathEncoder, readingEncoder, writingEncoder, assembler, lr))
//        val model = pipeline.fit(df)
//
//
//        model
//    }
//
    
    def testFunction(df : sql.DataFrame) :sql.DataFrame =
    {
//        var input_data = df.rdd.map(x => Row(x(0), //TODO ) )
//        val schema = StructType( Array(
//            StructField("labels", StringType),
//            StructField("feature", StringType)
//        ))
//        var df2 = spark.createDataFrame(input_data, schema)
//
//        df2.show(10)
//        df2
        
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
    def generateModel(df : sql.DataFrame): Unit =
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
    
        val rf = new RandomForestClassifier()
            .setLabelCol("indexedLabel")
            .setFeaturesCol("indexedFeatures")
            .setNumTrees(10)
    
        val labelConverter = new IndexToString()
            .setInputCol("prediction")
            .setOutputCol("predictedLabel")
            .setLabels(labelIndexer.labels)
    
        
//        TODO this vs that
//        val pipeline = new Pipeline()
//            .setStages(Array(labelIndexer, featureIndexer, rf, labelConverter))
            
//        Training without pipeline
        var a = labelIndexer.transform(trainingData)
        a = featureIndexer.transform(a)
        val rmfModel = rf.fit(a)
        
        var pred_ = rmfModel.transform(a)
        pred_ = labelConverter.transform(pred_)
        
        pred_.show()
        rmfModel.write
            .overwrite()
            .save("src/main/static/models/out")
        
        
        
        
//        val model = pipeline.fit(trainingData)
//
//        val predictions = model.transform(testData)
//
//        predictions.show()
        
//        rf.write
//            .overwrite()
//            .save("src/main/static/models/out")
    }
    
    def loadData(path: String): sql.DataFrame = {

        val data = spark
            .read
            .option("header", "true")
            .csv(path)
    
        val df = (data.select(data("race").as("label"),
            $"gender", $"lunch", $"math",
            $"reading", $"writing"))
        
        println("Loaded Data ")
        df.show(10)
        df
    }
    

    
    def middleMan(df : sql.DataFrame) : sql.DataFrame =
        {
            // string indexing
            val indexer1 = new StringIndexer().
                setInputCol("gender").
                setOutputCol("genderIndex").
                setHandleInvalid("keep")
            val indexed1 = indexer1.fit(df).transform(df)
    
            val indexer2 = new StringIndexer().
                setInputCol("lunch").
                setOutputCol("lunchIndex").
                setHandleInvalid("keep")
            val indexed2 = indexer2.fit(indexed1).transform(indexed1)
            
            val indexer3 = new StringIndexer()
                .setInputCol("math")
                .setOutputCol("mathIndex")
//                .setHandleInvalid("keep")
            val indexed3 = indexer3.fit(indexed2).transform(indexed2)
    
            val indexer4 = new StringIndexer()
                .setInputCol("reading")
                .setOutputCol("readingIndex")
                .setHandleInvalid("keep")
            val indexed4 = indexer4.fit(indexed3).transform(indexed3)
    
            val indexer5 = new StringIndexer()
                .setInputCol("writing")
                .setOutputCol("writingIndex")
                .setHandleInvalid("keep")
            val indexed5 = indexer5.fit(indexed4).transform(indexed4)
            
    
            // one hot encoding
            val encoder = new OneHotEncoderEstimator().
                setInputCols(Array("genderIndex", "lunchIndex", "mathIndex", "readingIndex", "writingIndex")).
                setOutputCols(Array("genderVec", "lunchVec", "mathVec", "readingVec", "writingVec"))
            val encoded = encoder.fit(indexed5).transform(indexed5)
            
            encoded
        }
    
    def assembleData(df : sql.DataFrame): sql.DataFrame =
    {
        val assembler = new VectorAssembler()
            .setInputCols(Array("genderVec", "lunchVec", "mathVec", "readingVec", "writingVec"))
            .setOutputCol("features")
        
        val output = assembler.transform(df).select($"label", $"features")
        output
    }
    
    def normalizeTheData(df: sql.DataFrame): sql.DataFrame = {
        
        var df2 = df.withColumn("math", col("math") / 100)
        df2 = df2.withColumn(colName = "reading", col("reading") / 100)
        df2 = df2.withColumn("writing", col("writing") / 100)
        
        df2 = df2.withColumn("features", array("gender", "lunch", "math", "reading", "writing"))
        df2 = df2.withColumn("labels", col("race"))
        
        df2 = df2
            .drop("gender")
            .drop("race")
            .drop("math")
            .drop("writing")
            .drop("reading")
            .drop("lunch")
        
        df2
    }
    
    def printData(df: sql.DataFrame): Unit = {
        df.printSchema()
        df.show(10)
    }
}
