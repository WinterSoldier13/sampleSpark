name := "sparkTest"

version := "0.1"

scalaVersion := "2.11.11"

//For spark
libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "2.3.0" ,
    "org.apache.spark" %% "spark-mllib" % "2.3.0" ,
    "org.apache.spark" %% "spark-sql" % "2.3.0" ,
    "org.apache.spark" %% "spark-hive" % "2.3.0" ,
    "org.apache.spark" %% "spark-streaming" % "2.3.0" ,
    "org.apache.spark" %% "spark-graphx" % "2.3.0",
//    "org.apache.spark" %% "spark-streaming-kafka" % "1.6.3",
    "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.0",
)

//Bahir
libraryDependencies += "org.apache.bahir" %% "spark-sql-streaming-mqtt" % "2.3.0"

