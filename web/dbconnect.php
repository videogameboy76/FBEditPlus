<?php
  $mysql_host     = "mysql12.000webhost.com";
  $mysql_database = "a5601452_sql";
  $mysql_user     = "a5601452_fbedit";
  $mysql_password = "fbeditlevels3";

  $connect = @mysql_connect($mysql_host, $mysql_user, $mysql_password)or die(mysql_error());

	mysql_query("SET character_set_client=utf8");
	mysql_query("SET character_set_connection=utf8");
 	mysql_query("SET character_set_results=utf8");

  $db = @mysql_select_db($mysql_database,$connect)or die(mysql_error());
?>