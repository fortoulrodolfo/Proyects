<?php
	header("Content-type: application/vnd.ms-excel; name='excel'");
	header("Content-Disposition: filename = DataIP_Fichero.ods");
	header("Pragma: no-cache");
	header("Expires: 0");
	echo $_POST['datos_a_enviar3'];
?>