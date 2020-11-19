package com.testSpark

import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession};


object CSVReader {
    def main(args: Array[String]): Unit = {
        val spark = SparkSession
            .builder()
            .appName("CSVReader")
            .master("local[*]")
            .getOrCreate()
        println("<><><><><><><><><><><><><><><><><><><><><>>><><><><><><><><><><><><><><><><><><><><><><><><>>><><><><><><><><><><><><><><><><><><><><><><><><>>><><><>")
//        Reading a CSV file
        val df = spark
            .read
            .option("header", "true")
            .csv("src/main/scala/resources/sample.csv")
        df.printSchema()
    
        val studentData = Seq(
            Row("Ayush", "Singh", 20, "Electrical"),
            Row("Pinaki", "Sen", 21, "Electrical"),
            Row("Amit", "Kumar", 21, "Electronics"),
            Row("Harshit", "Jain", 20, "Computer-Science"),
            Row("Aditya", "Tiwari", 21, "Computer-Science")
        )
        val studentSchema = List(
            StructField("fname", StringType, nullable = false),
            StructField("lname", StringType, nullable = false),
            StructField("age", IntegerType, nullable = false),
            StructField("dept", StringType, nullable = true)
            )
        
        val deptDF = spark.createDataFrame(spark.sparkContext.parallelize(studentData), StructType(studentSchema))
        deptDF.printSchema()
        
        
        spark.stop()
        
    }
    
}
