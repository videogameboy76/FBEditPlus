<?php
	mysql_pconnect("db.halmi.sk", "fbedit", "fbeditlevels3") or die("Nedá sa napojiť na databázový server");
	mysql_query("SET character_set_client=utf8");
	mysql_query("SET character_set_connection=utf8");
 	mysql_query("SET character_set_results=utf8");

	mysql_select_db("fbedit") or die("Nedá sa pripojiť na databázu");
?>
