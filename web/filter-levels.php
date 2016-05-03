<?php

  include("dbconnect.php");


  $query = "SELECT * FROM levelpack ORDER by id"; // WHERE id >= 0 AND id <= 50";
  $result=mysql_query($query);    //citam db
  $filteredLevels = array();
  
	while ($row=mysql_fetch_array($result)) 	{
		$id =  $row['id'];
		$levels = $row['levels'];
		//split level by 75 and make array where identifier will be whole level
		$to = strlen($levels)/75;
		$position = 0;
    for ($i=0; $i<$to; $i++) {
      $level = substr($levels, $position, 75);
      $filteredLevels[$level] = $level;
      $position += 75;
    }
    
    echo $id . " - before levels = " . $to . ", after = " . count($filteredLevels) . "<br>";
    
    $levels = "";
    foreach ($filteredLevels as $value) {
      $levels .= $value;
    }
    
    $query2="UPDATE levelpack SET levels='".$levels."' WHERE id=".$id.";";
    mysql_query($query2);
    //echo $query2 . "<br>";
    
    $filteredLevels = array(); 
	}
   
?>
