name := "sparkTest"

version := "0.1"

scalaVersion := "2.11.11"

val sparkVH = "2.4.0"
val sparkVL = "2.3.4"
val sparkV = sparkVH
val bahirVer = "2.4.0"

//For spark
libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" %  sparkV,
    "org.apache.spark" %% "spark-mllib" % sparkV ,
    "org.apache.spark" %% "spark-sql" % sparkV ,
    "org.apache.spark" %% "spark-hive" % sparkV ,
    "org.apache.spark" %% "spark-streaming" % sparkV ,
    "org.apache.spark" %% "spark-graphx" % sparkV,
//    "org.apache.spark" %% "spark-tags" % sparkVH
    
    //    "org.apache.spark" %% "spark-streaming-kafka" % "1.6.3",
//    "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.0",
)

//Bahir
//libraryDependencies += "org.apache.bahir" %% "spark-sql-streaming-mqtt" % "2.3.2"
libraryDependencies += "org.apache.bahir" %% "bahir-spark-distribution" % "2.4.0"

//Hadoop
//libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.0.2"
//libraryDependencies += "org.apache.hadoop" % "hadoop-hdfs" % "3.0.2"


libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.7.2"
libraryDependencies += "org.apache.hadoop" % "hadoop-hdfs" % "2.7.2"

// for XML parsing
//libraryDependencies += "com.databricks" %% "spark-xml" % "0.11.0"
//dependencyOverrides += "com.google.guava" % "guava" % "15.0"

//XML to JSON
//libraryDependencies += "org.json" % "json" % "20201115"

//libraryDependencies += "com.github.wnameless.json" % "json-flattener" % "0.11.1"

//libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.6.7.1"
//should be 2.6.5

//2.2.0-cdh6.0.1

//|SPARK|- |BAHIR|
//2.3.3 => 2.3.2
//2.2.0 => 2.2.3



//NEdd to add this to the transistor
//"org.apache.bahir" %% "spark-sql-streaming-mqtt" % sparkV,
//"org.apache.hadoop" % "hadoop-common" % "2.7.2",
//"org.apache.hadoop" % "hadoop-hdfs" % "2.7.2",
//"org.slf4j" % "slf4j-simple" % "1.6.6"