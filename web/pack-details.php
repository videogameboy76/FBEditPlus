<?php

  include("dbconnect.php");

	$query= "SELECT author, levelpackname, date, rating, androidid FROM levelpack WHERE id=".$_POST['id'].";";
	//echo $query;
  @$result=mysql_query($query);    //citam db
	
	//a kym su zaznamy tak ich vypisujem
	while ($row=mysql_fetch_array($result)) 	{
		$output .= htmlspecialchars_decode($row['author']) ."|"
               .htmlspecialchars_decode($row['levelpackname']) ."|"
               .$row['date']."|"
               .$row['rating']."|"
               .$row['androidid'];
	}

  echo $output;
  mysql_close();

?>
