package com.testVersion

import com.databricks.spark.xml.functions.from_xml
import com.databricks.spark.xml.schema_of_xml
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.ArrayType

object usingSparkXML {
    val spark: SparkSession = SparkSession
        .builder()
        .appName("sampleApp")
        .master("local[*]")
        .getOrCreate()
    
    spark.sparkContext.setLogLevel("ERROR")
    
    import spark.implicits._
    
    def main(args: Array[String]): Unit = {
        
        val brokerUrl_ : String = "tcp://localhost:1883"
        val topicName_ : String = "sample_topic"
        val username_ : String = "username"
        val password_ : String = "password"
        val path2XML_ : String = "src/main/static/dataset/smallXML.xml"
        val tempTable_ : String = "tempTable"
        val parqOutPath: String = "/home/wintersoldier/Documents/tookitaki/sparkTest/src/main/static/output/fromSparkXML"
        
        //    Format of dataframe:
        //        id : integer, topic: String, payload : binary, timestamp : timestamp
        
        val df = spark
            .readStream
            .format("org.apache.bahir.sql.streaming.mqtt.MQTTStreamSourceProvider")
            .option("brokerUrl", brokerUrl_)
            .option("topic", topicName_)
            .option("persistence", "memory")
            .option("cleanSession", "false")
            .option("username", username_)
            .option("password", password_)
            .option("localStorage", "/home/wintersoldier/Desktop/tempS")
            .option("clientId", "ayush")
            .load()
        
        
        val payload_ = df.select("payload")
            .as[Array[Byte]]
            .map(payload => {
                new String(payload)
            })
            .toDF("payload")
        
        payload_.writeStream
            .foreachBatch((batchDF: DataFrame, batchID: Long) => {
                println(s"Processing batch number: $batchID")
                val payloadSchemaXML = schema_of_xml(batchDF.select("payload").as[String])
                val parsed = batchDF.withColumn("parsed", from_xml(col("payload"), payloadSchemaXML))
                //                parsed.show(true)
                
                if (!parsed.isEmpty) {
                    parsed.select("parsed")
                        .write
                        .format("json")
                        .mode("overwrite")
                        .save(parqOutPath)
                    val queryString = "*"
                    executeQuery(parsed.select("parsed"), queryString)
                }
            })
            .start
            .awaitTermination()
        
        spark.close()
        spark.stop()
    }
    
    import org.apache.spark.sql.Column
    import org.apache.spark.sql.types.StructType
    import org.apache.spark.sql.functions.col
    
    def flattenStructSchema(schema: StructType, prefix: String = null): Array[Column] = {
        schema.fields.flatMap(f => {
            val columnName = if (prefix == null) f.name else (prefix + "." + f.name)
            
            f.dataType match {
                case st: StructType => flattenStructSchema(st, columnName)
                case _ => Array(col(columnName).as(columnName.replace(".", "||")))
            }
        })
    }
    
    def executeQuery(parsed: DataFrame, queryString: String): Unit = {
        println("<<EXECUTED>>")
        //        parsed.printSchema()
        val a = parsed.select(flattenStructSchema(parsed.schema): _*)
        //        val a = parsed.select(col("parsed.*"))
        //        a.show()
        a.printSchema()
        //        parsed.printSchema()
        //        parsed.show()
        //
        //        val filteredDF = parsed.collect()
        //
        //        for (x <- filteredDF.toList) {
        //            println(x)
        //        }
        
    }
    
}


/*
* root
 |-- parsed||ExecutionTime: long (nullable = true)
 |-- parsed||FilterClass: string (nullable = true)
 |-- parsed||FilterData||Entity: array (nullable = true)
 |    |-- element: struct (containsNull = true)
 |    |    |-- Field: array (nullable = true)
 |    |    |    |-- element: struct (containsNull = true)
 |    |    |    |    |-- _VALUE: string (nullable = true)
 |    |    |    |    |-- _id: long (nullable = true)
 |    |    |    |    |-- _path: string (nullable = true)
 |    |    |    |    |-- _type: long (nullable = true)
 |    |    |-- _extraction: string (nullable = true)
 |    |    |-- _id: long (nullable = true)
 |-- parsed||Hit: array (nullable = true)
 |    |-- element: struct (containsNull = true)
 |    |    |-- Algorithm: string (nullable = true)
 |    |    |-- Channel: string (nullable = true)
 |    |    |-- CheckDescription: string (nullable = true)
 |    |    |-- CheckName: string (nullable = true)
 |    |    |-- Explanation: string (nullable = true)
 |    |    |-- ListId: struct (nullable = true)
 |    |    |    |-- _VALUE: string (nullable = true)
 |    |    |    |-- _format: string (nullable = true)
 |    |    |    |-- _type: string (nullable = true)
 |    |    |    |-- _version: string (nullable = true)
 |    |    |-- Matches: struct (nullable = true)
 |    |    |    |-- Field: array (nullable = true)
 |    |    |    |    |-- element: struct (containsNull = true)
 |    |    |    |    |    |-- Pattern: array (nullable = true)
 |    |    |    |    |    |    |-- element: struct (containsNull = true)
 |    |    |    |    |    |    |    |-- _VALUE: string (nullable = true)
 |    |    |    |    |    |    |    |-- _filterFieldId: long (nullable = true)
 |    |    |    |    |    |    |    |-- _from: long (nullable = true)
 |    |    |    |    |    |    |    |-- _invalid: boolean (nullable = true)
 |    |    |    |    |    |    |    |-- _to: long (nullable = true)
 |    |    |    |    |    |-- _explanation: string (nullable = true)
 |    |    |    |    |    |-- _invalid: boolean (nullable = true)
 |    |    |    |    |    |-- _path: string (nullable = true)
 |    |    |    |    |    |-- _score: double (nullable = true)
 |    |    |    |    |    |-- _text: string (nullable = true)
 |    |    |    |    |    |-- _type: long (nullable = true)
 |    |    |-- Score: double (nullable = true)
 |    |    |-- State: struct (nullable = true)
 |    |    |    |-- _VALUE: string (nullable = true)
 |    |    |    |-- _date: long (nullable = true)
 |    |    |    |-- _state: string (nullable = true)
 |    |    |    |-- _user: string (nullable = true)
 |    |    |-- Threshold: double (nullable = true)
 |    |    |-- _entityId: long (nullable = true)
 |    |    |-- _id: long (nullable = true)
 |-- parsed||InputData||_format: string (nullable = true)
 |-- parsed||InputData||_id: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||FraudRequestSegment||Direction: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||FraudRequestSegment||ScanInd: long (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||FraudRequestSegment||UniqueId: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||FraudRequestSegment||_xmlns: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||HeaderSegment||SequenceNb: long (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||HeaderSegment||Version: long (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||HeaderSegment||_xmlns: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Bnf||AdrLine: array (nullable = true)
 |    |-- element: string (containsNull = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Bnf||CountryCode: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Bnf||Id: long (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Bnf||IdCode: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Bnf||Nm: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Cdt: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||ChargesInfo||ChargesCode: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||DebitAccount||AccNb: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||DebitAccount||AccType: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||DebitAccount||Office: long (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||ExtRef: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Fx||ExchRate: double (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||InstructedAmount||Amount: double (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||InstructedAmount||Currency: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Org||AdrLine: array (nullable = true)
 |    |-- element: string (containsNull = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Org||CountryCode: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Org||Id: long (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Org||IdCode: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Org||Nm: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||PaymentAmount||Amount: double (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||PaymentAmount||Currency: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||PmtMethod||Mop: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||PmtMethod||MsgType: long (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||PmtMethod||ProdCode: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||ReceiverBank||BankId: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||ReceiverBank||BankName: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||ReceiverCorr||AdrLine: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||ReceiverCorr||Id: long (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||ReceiverCorr||IdCode: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||ReceiverCorr||Nm: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Reference: long (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||SenderBank||BankId: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||SenderBank||BankName: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||SenderCorr||AdrLine: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||SenderCorr||Id: long (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||SenderCorr||IdCode: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||SenderCorr||Nm: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Service: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||Template: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||TranCode: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||ValueDate: long (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||PaymentSegment||_xmlns: string (nullable = true)
 |-- parsed||InputData||ns2:FrdReq||_ns2: string (nullable = true)
 |-- parsed||State: array (nullable = true)
 |    |-- element: struct (containsNull = true)
 |    |    |-- _VALUE: string (nullable = true)
 |    |    |-- _date: long (nullable = true)
 |    |    |-- _state: string (nullable = true)
 |    |    |-- _user: string (nullable = true)

*/