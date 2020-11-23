package com.test.ml

import java.util.Scanner

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{LogisticRegression, RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorIndexer}
import org.apache.spark.sql.SparkSession


object randomForest
{
    val spark: SparkSession = SparkSession
        .builder()
        .appName("CSVReader")
        .master("local[*]")
        .getOrCreate()
    
    def randomForestClass() : Unit ={
        val data = spark.read.format("libsvm").load("src/main/scala/resources/test.txt")
        data.printSchema()
        
        val labelIndexer = new StringIndexer()
            .setInputCol("label")
            .setOutputCol("indexedLabel")
            .fit(data)

        val featureIndexer = new VectorIndexer()
            .setInputCol("features")
            .setOutputCol("indexedFeatures")
            .setMaxCategories(4)
            .fit(data)
        
        val Array(trainingData, testData) = data.randomSplit(Array(0.7, 0.3))
        
        val rf = new RandomForestClassifier()
            .setLabelCol("indexedLabel")
            .setFeaturesCol("indexedFeatures")
            .setNumTrees(10)
        
        val labelConverter = new IndexToString()
            .setInputCol("prediction")
            .setOutputCol("predictedLabel")
            .setLabels(labelIndexer.labels)
        
        val pipeline = new Pipeline()
            .setStages(Array(labelIndexer, featureIndexer, rf, labelConverter))
        
        val model = pipeline.fit(trainingData)
        val predictions = model.transform(testData)
        
        predictions.select("predictedLabel", "label", "features").show(5)
        
        val evaluator = new MulticlassClassificationEvaluator()
            .setLabelCol("indexedLabel")
            .setPredictionCol("prediction")
            .setMetricName("accuracy")
        val accuracy = evaluator.evaluate(predictions)
        println("Test Error = " + (1.0 - accuracy))
    
        val rfModel = model.stages(2).asInstanceOf[RandomForestClassificationModel]
        println("Learned classification forest model:\n" + rfModel.toDebugString)
    }
    
    def regression() : Unit = {
        val training = spark.read.format("libsvm").load("src/main/scala/resources/test.txt")
    
        val lr = new LogisticRegression()
            .setMaxIter(10)
            .setRegParam(0.3)
            .setElasticNetParam(0.8)
        
        val lrModel = lr.fit(training)
        
        println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")
        
        val mlr = new LogisticRegression()
            .setMaxIter(10)
            .setRegParam(0.3)
            .setElasticNetParam(0.8)
            .setFamily("multinomial")
    
        val mlrModel = mlr.fit(training)
        
        println(s"Multinomial coefficients: ${mlrModel.coefficientMatrix}")
        println(s"Multinomial intercepts: ${mlrModel.interceptVector}")
    }
    
    def main(args : Array[String]): Unit =
    {
        val sc : Scanner = new Scanner(System.in)
        println("<><><><><><><><><>\nEnter \n[1] for RandomForest \n[2] for Regression")
        val inp : Int = sc.nextInt()
        
        if(inp == 1)
            {
                randomForestClass()
            }
        else if (inp == 2)
            {
                regression()
            }
        else
            println("Wrong input")
        spark.close()
    }
}
