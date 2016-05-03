<?
	include('allowed_editors.php');

	$package = 'sk.halmi.fbedit';
	if (isAllowed($package)) {
		echo $package.' is allowed'.'\n';
	} else {
		echo $package.' NOT allowed'.'\n';
	}

	$package = 'sk.halmi.fbedita';
	if (isAllowed($package)) {
		echo $package.' is allowed'.'\n';
	} else {
		echo $package.' NOT allowed'.'\n';
	}

	$package = 'com.skitapps.bbeditor';
	if (isAllowed($package)) {
		echo $package.' is allowed'.'\n';
	} else {
		echo $package.' NOT allowed'.'\n';
	}

?>
