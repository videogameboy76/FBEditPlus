<?
  include("dbconnect.php");
//	INSERT INTO `a5601452_sql`.`levelpack` (
//			`id` ,
//			`androidid` ,
//			`author` ,
//			`levelpackname` ,
//			`date` ,
//			`previewlevel` ,
//			`levels` ,
//			`rating` 
//			)
//			VALUES (
//			NULL , '0000000000000000', 'Rootko', 'Pokusny rucne zadany', '2009-12-29', '012345678901234567890123456789012345678901234567890123456789012345678901234', '012345678901234567890123456789012345678901234567890123456789012345678901234', '4.89'
//			);

	//at least 15 levels in pack
	if (strlen($_POST['levels']) >= 1125) {
		$username = htmlspecialchars($_POST['authorName']);
		$packname = htmlspecialchars($_POST['levelPackName']);

		//find out if level pack with such levels, author and androidid exists
		$query= "SELECT id FROM levelpack WHERE androidid='".$_POST['androidID']."' AND author='".$username."' AND levels='".$_POST['levels']."'";
		$result=mysql_query($query);    //citam db
	
		//a kym su zaznamy tak ich vypisujem
		$ids = array();
		$i = 0;
		while ($row=mysql_fetch_array($result)) 	{
			$ids[$i] = $row['id'];
		}

		$count = count($ids);
		$output = "";

		//if ($_POST['overwrite'] == 'true') {
		if ($count > 0) {
			//UPDATE `a5601452_sql`.`levelpack` SET `levelpackname` = 'Level Pack 1' WHERE `levelpack`.`id` =8 LIMIT 1 ;
			$query="UPDATE levelpack SET 
								previewlevel='".$_POST['previewLevel']."', 
								levelpackname='".$packname."'
							WHERE
								androidid='".$_POST['androidID']."' AND
								author='".$username."' AND
								levels='".$_POST['levels']."' ";
			$output .= "UPDATE";
		} else {
			  //find out if level pack with such name, author and androidid exists
		    $query= "SELECT id FROM levelpack WHERE androidid='".$_POST['androidID']."' AND author='".$username."' AND levelpackname='".$packname."'";
		    $result=mysql_query($query);    //citam db
	  	  //a kym su zaznamy tak ich vypisujem
		    $ids = array();
		    $i = 0;
		    while ($row=mysql_fetch_array($result)) 	{
			      $ids[$i] = $row['id'];
		    }

		    $count = count($ids);
		    //same androidid, same author, same levelpackname, but other levels
		    if ($count > 0) {
	      		$query="UPDATE levelpack SET 
	      							previewlevel='".$_POST['previewLevel']."', 
	      							levels='".$_POST['levels']."'
	      						WHERE
	      							androidid='".$_POST['androidID']."' AND
	      							author='".$username."' AND
	      							levelpackname='".$packname."' ";
	      		$output .= "UPDATE";
	      // new levelpack name
	      } else {
	      		$query="INSERT INTO levelpack (androidid, author, levelpackname, date, previewlevel, levels, rating) 
	      						VALUES (
	      							'".$_POST['androidID']."', 
	      							'".$username."', 
	      							'".$packname."', 
	      							'".$_POST['date']."', 
	      							'".$_POST['previewLevel']."', 
	      							'".$_POST['levels']."', 
	      							'5.0'
	      						)";
	      		$output .= "INSERT";
	      }
		}

		//insert into database
		if ($username == "" 
	      || $packname == ""
	      || $_POST['androidID'] == ""
	      || $_POST['previewLevel'] == ""
	      || $_POST['levels'] == "") {
	    //do nothing
	  } else {
	    mysql_query($query);
	  }
	  
	//	for ($i=0; $i<$count; $i++) {
	//		$output .= "|".$ids[$i];
	//	}

		//echo $query;
		echo $output;
	} else {
	}
	
	mysql_close();

?>
