package com.xmlHelper.getAllPath


import scala.collection.mutable.ListBuffer
import scala.xml._

object getAllPaths {
    var map: Map[String, String] = Map[String, String]()
    
    def main(args: Array[String]): Unit= {
    
//        For testing
        val x = <PaymentAmount>
            <Currency>GBP</Currency>
            <Amount>1.0200</Amount>
            <test>
                <h1>Ayush</h1>
            </test>
            </PaymentAmount>

        
        val allPathList = getAllPathAsList(x)
        
        for(x <- allPathList)
            {
                println(x+"**")
            }
        
    }
    
    def getAllPathAsList(x: Elem): List[String] = {
        dfs(x, "")
        
        var allPossiblePaths: ListBuffer[String] = new ListBuffer[String]
        
        for (x <- map) {
            var temp = x._1
            //            println(x._1 + " |||| " + x._2)
            temp = temp.substring(0, temp.length - 9)
            if (x._2.trim != "")
                allPossiblePaths.append(temp)
        }
        allPossiblePaths.toList
    }
    
    def dfs(n: Seq[Node], brc: String): Unit =
        n.foreach(x => {
            if (x.child.isEmpty) {
                if (x.text == "") {
                    map = map + (brc + x.label -> "")
                    dfs(x.child, brc)
                }
                else {
                    map = map + (brc + x.label + " " -> x.text)
                    dfs(x.child, brc)
                }
            }
            else {
                val bc = brc + x.label + "/"
                dfs(x.child, bc)
            }
        }
        )
}


