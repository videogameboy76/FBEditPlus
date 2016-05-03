<?php

  include("dbconnect.php");
  include('allowed_editors.php');

  function formCondition($datefrom, $dateto, $rating, $author, $packname, $sort, $minlevels) {
    //first where condition
    $levels = $minlevels*75;
    
    $where = " WHERE ";
    if ($datefrom != '') {
      $where .= "date >= '".$datefrom."' AND ";
    }
    if ($dateto != '') {
      $where .= "date <= '".$dateto."' AND ";
    }
    if ($rating != '') {
      $where .= "rating >= '".$rating."' AND ";
    }
    if ($author != '') {
      $where .= "author LIKE '".$author."' AND ";
    }
    if ($packname != '') {
      $where .= "levelpackname LIKE '".$packname."' AND ";
    }
    if ($minlevels != '') {
      $where .= "LENGTH(levels) >= ".$levels." AND ";
    }


    //remove last AND
    if ($where != " WHERE ") {
      $where = substr($where, 0, -4);
    } else {
      $where = " ";
    }
    
    $where .= "ORDER BY " . $sort;
    
    return $where;
  }
  
  function getUsedTimes($token) {
    //see DownloadLevelPackActivity.generateToken() for details about parsing token
    $aid = "";
    for ( $i=0; $i<strlen($token); $i++ ) {
    	if ($i%2 == 1) {
        $aid .= $token{$i};
      }
    }

    $query = "SELECT downloaded FROM fbedit.usage WHERE androidid = '".$aid."';";
    $result=mysql_query($query);    //citam db
    $downloaded = 0;
    
    if (mysql_num_rows($result) == 0) {
      //first request for download - insert androidid into database
      $query = "INSERT INTO fbedit.usage (androidid, downloaded) VALUES ('".$aid."',".$downloaded.");";
      $output = $query;  
      //insert /update database
	    mysql_query($query);
    } else {
      //androidid is registered in system - get number of downloads
	    while ($row=mysql_fetch_array($result)) 	{
        $downloaded = $row['downloaded']; 
      }
/*  
      $downloaded++;
      // do not raise number of downloads only if searching...
  		$query="UPDATE fbedit.usage SET downloaded=".$downloaded." WHERE androidid='".$aid."' ";
      $output = $query;  
*/
    }

    //I'm going to overwrite just last digit, but in database I want to know 
    //how often did somebody tried to download something    
    if ($downloaded >= 6) {
      $downloaded = 9;
    }
    return $downloaded;
  }
  
  
  //request - 2010-01-04|2010-01-06|3.5|*oot*||[DAT,RAT,AUT,LNA,COU]|[ASC,DESC]|token
  //          datefrom  |dateto    |rating|author|packname|sortby|sorthow
  //response - [x{5}Y{75}] - 5x - id levelpacku na 5 miest 75Y - preview level
  //po kliknuti na konkretny level v aplikacii sa spusti novy intent
  $package = $_POST['editorID'];
  $request = explode("|", $_POST['req']);
  $datefrom = $request[0];
  $dateto = $request[1];
  $rating = $request[2];
  $author = htmlspecialchars($request[3]);
    $author = str_replace("*", "%", $author);
  $packname = htmlspecialchars($request[4]);
    $packname = str_replace("*", "%", $packname);
  $token = $request[7];
  $minlevels = $request[8];

  if (isAllowed($package)) {

	  //determine order
	  switch ($request[5]) {
	    case 'DAT':
	      $sortby = 'date ';
	      break;
	  
	    case 'RAT':
	      $sortby = 'rating ';
	      break;
	  
	    case 'AUT':
	      $sortby = 'author ';
	      break;
	  
	    case 'LNA':
	      $sortby = 'levelpackname ';
	      break;
	  
	    case 'COU':
	      $sortby = 'length ';
	      break;
	  
	    default:
	  	  $sortby = 'date ';
	  	  break;
	  }
	  
	  switch ($request[6]) {
	    case 'ASC':
	      $sortby .= 'ASC';
	      break;
	    
	    default:
	  	  $sortby .= 'DESC';
	  	  break;
	  }
	  
		//find out if level pack with such levels, author and androidid exists
		$query= "SELECT id, previewlevel, LENGTH(levels) as length FROM levelpack "
		  . formCondition($datefrom, $dateto, $rating, $author, $packname, $sortby, $minlevels)
		  ." LIMIT 999";
	  
	  //echo $query;
	
	  $result=mysql_query($query);    //citam db
	
		//prva cast je timestamp ale s poslednym cislom pocet downloadov
		//$output = $token.substr(mktime(), 0, -1) . getUsedTimes($token);
    $output = "00000000000000000000000000000000000000000";     //41 chars
	
		//a kym su zaznamy tak ich vypisujem
		while ($row=mysql_fetch_array($result)) 	{
			$output .= str_pad($row['id'], 5, "0", STR_PAD_LEFT)  //id of levelpack 5 numbers filled with zeroes
		       .str_pad($row['length'], 6, "0", STR_PAD_LEFT) //count of chars in levels - how many levels are in pack?
		       .$row['previewlevel'];                     //preview level data
		}

	  echo $output;

  //package not allowed
  } else {
		//Return only "UPDATE EDITOR levels"
		$query= "SELECT id, previewlevel, LENGTH(levels) as length FROM levelpack "
		  ." WHERE id >= 3768 AND id <= 3776 ORDER BY id ASC LIMIT 9";
//		$query= "SELECT id, previewlevel, LENGTH(levels) as length FROM levelpack "
//		  ." WHERE id >= 3768 AND id <= 3776 ORDER BY id ASC LIMIT 0";
	  
	  $result=mysql_query($query);    //citam db
	
		//prva cast je timestamp ale s poslednym cislom pocet downloadov
		$output = $token.substr(mktime(), 0, -1) . getUsedTimes($token);
	
		//a kym su zaznamy tak ich vypisujem
		while ($row=mysql_fetch_array($result)) 	{
			$output .= str_pad($row['id'], 5, "0", STR_PAD_LEFT)  //id of levelpack 5 numbers filled with zeroes
		       .str_pad($row['length'], 6, "0", STR_PAD_LEFT) //count of chars in levels - how many levels are in pack?
		       .$row['previewlevel'];                     //preview level data
		}

	  echo $output;
  }

  mysql_close();

  ?>
