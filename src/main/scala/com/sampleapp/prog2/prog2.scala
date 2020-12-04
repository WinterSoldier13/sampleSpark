package com.sampleapp.prog2

import org.apache.spark.sql.SparkSession

object prog2
{
    val spark: SparkSession = SparkSession
        .builder()
        .appName("CSVReader")
        .master("local[*]")
        .getOrCreate()
    
    import spark.implicits._
}
