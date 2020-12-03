package com.sampleapp.prog1

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{LogisticRegression, RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorIndexer}
import org.apache.spark.sql
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col


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
        //        Normalize the cols
        df = normalizeTheData(df)
        // Print the data
        printData(df)
        
        
        
        // Create a model
        
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
