package com.xmlReader

import com.databricks.spark.xml.XmlDataFrameReader
import org.apache.spark.sql.SparkSession


// for XML parsing
//libraryDependencies += "com.databricks" %% "spark-xml" % "0.5.0"
//dependencyOverrides += "com.google.guava" % "guava" % "15.0"


object xmlReader
{
    val spark: SparkSession = SparkSession
        .builder()
        .appName("sampleApp")
        .master("local[*]")
        .getOrCreate()
    import spark.implicits._
    
    def main(args : Array[String]): Unit =
    {
        val df = spark
            .read
            .format("com.databricks.spark.xml")
            .option("rowTag", "CofiResults")
            .xml("src/main/static/dataset/sampleXML.xml")
        
        df.printSchema()
        df.show(10)
        
        spark.close()
    }
}
