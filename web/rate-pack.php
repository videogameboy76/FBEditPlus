<?php
  include("dbconnect.php");
  $request = explode("|", $_POST['req']);
  $packid = $request[0];
  $aid = $request[1];
  $rating = $request[2];
  
  $query = "SELECT id FROM ratings WHERE androidid = '".$aid."' AND packid = ".$packid.";";
  $result=mysql_query($query);    //citam db
  $id = -1;
  $output = "";
  
	while ($row=mysql_fetch_array($result)) 	{
		$id = $row['id'];
	}
	
	//update rating
	if ($id != -1) {
  	$query = "UPDATE fbedit.ratings SET rating =".$rating." WHERE androidid='".$aid."' AND packid=".$packid;
  	$output = "UPDATE";
  //insert
  } else {   
    $query = "INSERT INTO fbedit.ratings (androidid, packid, rating) VALUES ('".$aid."', ".$packid.", ".$rating.");";
  	$output = "INSERT";
  }
                  
  @mysql_query($query);
  
  
  //update rating in main table
  $query = "SELECT count(androidid) as count, sum(rating) as sum FROM ratings where packid=".$packid;
  $result=mysql_query($query);    //citam db
	while ($row=mysql_fetch_array($result)) 	{
		$count = $row['count'];
		$sum = $row['sum'];
	}

  $rating = $sum / $count;
  $query = "UPDATE fbedit.levelpack SET rating = ".$rating." WHERE id=".$packid;
  mysql_query($query);

  //echo $query;                  
  echo $output;

  
?>
