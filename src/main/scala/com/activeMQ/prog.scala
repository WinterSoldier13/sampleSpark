package com.activeMQ

import org.apache.spark.sql.SparkSession

object prog
{
    val spark: SparkSession = SparkSession
        .builder()
        .appName("CSVReader")
        .master("local[*]")
        .getOrCreate()
    
    import spark.implicits._
}
