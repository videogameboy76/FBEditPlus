<?php
  include("dbconnect.php");

  //raise downloaded in a5601452_sql.usage
  $query = "SELECT downloaded FROM a5601452_sql.usage WHERE androidid = '".$aid."';";
  $result=mysql_query($query);    //citam db
  $downloaded = 0;
	while ($row=mysql_fetch_array($result)) 	{
		$downloaded .= $row['downloaded'];
	}
  $downloaded++;
	$query="UPDATE a5601452_sql.usage SET downloaded=".$downloaded." WHERE androidid='".$aid."' ";
  @mysql_query($query);    //citam db
                  
                  
	$query= "SELECT levels FROM levelpack WHERE id=".$_POST['id'].";";
	//echo $query;
  @$result=mysql_query($query);    //citam db
	
	//a kym su zaznamy tak ich vypisujem
	while ($row=mysql_fetch_array($result)) 	{
		$output .= $row['levels'];
	}

  echo $output;

  mysql_close();

?>
