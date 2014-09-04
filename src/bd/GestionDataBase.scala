package bd

import java.sql.{Connection, DriverManager, ResultSet};
import scala.collection.mutable.ListBuffer
object GestionDataBase {
  
  // Change to Your Database Config
  val conn_str = "jdbc:mysql://localhost/scalaplayer?user=root&password="
  // Load the driver
  classOf[com.mysql.jdbc.Driver]

   def getAll() : ListBuffer[Audio] = {
     
	     var mylist=ListBuffer[Audio]()
	      // Setup the connection
	  val conn = DriverManager.getConnection(conn_str)
	  try {
	      // Configure to be Read Only
	      val statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
	
	      // Execute Query
	      val rs = statement.executeQuery("SELECT * FROM audio ")
	
	      // Iterate Over ResultSet
	      while (rs.next) {
	       var a= new Audio(rs.getString("name"),rs.getString("path"),rs.getString("autor"),rs.getString("genre"),rs.getString("album"),rs.getString("year").toInt)
	       mylist.+=(a)
	      }
	  }
	  finally {
	      conn.close
	  }
	     println(mylist)
	      mylist
	    
	  
  }
   def putAudio(a:Audio) : Int = {
     println("put appelé")
     
	 // Setup the connection
	  val conn = DriverManager.getConnection(conn_str)
	  try {	            
	       var sqlStmt = "INSERT INTO audio VALUES (default,?,?,?,?,?,?);"
			System.out.println("SQL Statement:\n\t" + sqlStmt)
			var prepStmt = conn.prepareStatement(sqlStmt)
			prepStmt.setString(1, a.name)	
			prepStmt.setString(2, a.autor)
			prepStmt.setString(3, a.album)
			prepStmt.setString(4, a.album)
			prepStmt.setInt(5, a.year)
			prepStmt.setString(6, a.path)
			var rs = prepStmt.executeUpdate()   
	      println("INSERTION REUSSIE")
	      1
	      
	  }
     catch {  case e:Exception => println(e.getMessage())
    	-1}
	  finally { conn.close	  }	     
	  1
  }
   def delete(name:String) : Int = {     
	      // Setup the connection
	  val conn = DriverManager.getConnection(conn_str)
	  try {
      
	       var sqlStmt = "DELETE FROM audio"+" WHERE audio.name=?;"
			System.out.println("SQL Statement:\n\t" + sqlStmt);
			var prepStmt = conn.prepareStatement(sqlStmt);
			System.out.println("Prepared Statement before bind variables set:\n\t" + prepStmt.toString());
			prepStmt.setString(1, name)			
			System.out.println("Prepared Statement after bind variables set:\n\t" + prepStmt.toString());
			var rs = prepStmt.executeUpdate()      
	      
		println("delete succeded")
		1

	  }
	  catch {
	    
	    case e:Exception => {
	      println("failed :"+e.getMessage())
	      -1
	    }
	  }
	  finally {
	      conn.close
	  }     

  }
   def modifAudio(a:Audio) : Int = {
     
	     
	      // Setup the connection
	  val conn = DriverManager.getConnection(conn_str)
	  try {

	      var sqlStmt = "UPDATE audio SET  path= ?, autor = ?, genre = ?, album = ?, year = ? WHERE name = ?;"
			var prepStmt = conn.prepareStatement(sqlStmt)
			prepStmt.setString(1, a.path)	
			prepStmt.setString(2, a.autor)
			prepStmt.setString(3, a.genre)
			prepStmt.setString(4, a.album)
			prepStmt.setInt(5, a.year)
			prepStmt.setString(6, a.name)
			var rs = prepStmt.executeUpdate()   
	      println("UPDATE SUCCECED")
	      1
	  }
	  catch {
	    
	    case e:Exception => {
	      println("failed :"+e.getMessage())
	      -1
	    }
	  }
	  finally {
	      conn.close
	  }     
      1  
  }

}