<!DOCTYPE html>

<?php
	/* Se incluye la clase de genera los pdf
	 */
    include('extensiones/ezpdf/Cezpdf.php');
	
	header("Content-type:application/pdf");
	header("Content-Disposition:attachment;filename=LibroCitas.pdf");
	
	$conexiones = simplexml_load_file('conexiones/conexiones.xml');
	$link = $conexiones->link;
	$userBD = $conexiones->sistemas->usuario;
	$claveBD = $conexiones->sistemas->contrasena;
	$servicioBD = $conexiones->sistemas->servicio;
	
	/* Se lleva a cabo la conexión con la base de datos.
		 */
	$conec = oci_connect($userBD, $claveBD, $servicioBD, 'AL32UTF8');

	$pdf =& new Cezpdf('LETTER', 'landscape');
	$pdf->selectFont('fonts/Helvetica.afm');
	$pdf->setColor(0/255,0/255,0/255);
	
	
	/* Setear el número de páginas, y el pie de página.
	 */
	$pdf->addText(330,520,20,"<b>Libro de Citas</b>\n", 'center');
	$pdf->addText(600,580,8,"<b>Fecha:</b> ".date("d/m/Y"));
	$pdf->addText(600,570,8,"<b>Hora:</b> ".date("H:i:s")."\n\n"); 
	$pdf->ezStartPageNumbers(775,12,10,'right','<b>Página {PAGENUM} de {TOTALPAGENUM}</b>',1);
	$pdf->ezStartPageNumbers(615,12,10,'right','Av. Panteón con calle Alameda, San Bernardino - Caracas - 1010 Teléfonos: (212)508.61.11',1);
	$pdf->ezStartPageNumbers(670,25,10,'right','_____________________________________________________________________________________________',1);
	 
	$datacreator = array (
						'Title'=>'Libro de Citas',
						'Subject'=>'PDF',
						'Author'=>'HCC',
						);
	$pdf->addInfo($datacreator);
	
	if ($conec){
	$query = "SELECT CITMED.T_CITA.ID_CITA,
					TO_CHAR(CITMED.T_CITA.FECHA_INICIO_CITA, 'DD-MM-YYYY HH24:MI:SS'),
					TO_CHAR(CITMED.T_CITA.FECHA_FIN_CITA, 'DD-MM-YYYY HH24:MI:SS'),
					CITMED.T_CITA.ID_PACIENTE,
					CITMED.T_PACIENTE.NOMBRE_PACIENTE,
					CITMED.T_PACIENTE.APELLIDO1_PACIENTE,
					CITMED.T_CITA.ID_MEDICO,
					CITMED.T_MEDICO.NOMBRE1_MEDICO,
					CITMED.T_MEDICO.APELLIDO1_MEDICO,
					CITMED.T_CITA.ID_PROCEDIMIENTO,
					CITMED.T_PROCEDIMIENTO_BASICO.NOMBRE_PROCEDIMIENTO,
					CITMED.T_CITA.ID_SALA,
					CITMED.T_SALA.NOMBRE_SALA,
					CITMED.T_CITA.STATUS_CITA,
					CITMED.T_CITA.ID_EQUIPO,
					CITMED.T_EQUIPO.NOMBRE_EQUIPO,
					CITMED.T_CITA.FORMA_PAGO_CITA,
					CITMED.T_CITA.ANESTESIOLOGO_CITA,
					CITMED.T_CITA.STATUS_PACIENTE_CITA,
					CITMED.T_CITA.TIPO_ID
					FROM CITMED.T_CITA, CITMED.T_PACIENTE, CITMED.T_MEDICO, CITMED.T_PROCEDIMIENTO_BASICO, CITMED.T_SALA, CITMED.T_EQUIPO
					WHERE CITMED.T_CITA.TIPO_ID=CITMED.T_PACIENTE.TIPO_ID AND CITMED.T_CITA.ID_PACIENTE=CITMED.T_PACIENTE.ID_PACIENTE AND CITMED.T_CITA.ID_EQUIPO=CITMED.T_EQUIPO.ID_EQUIPO AND CITMED.T_CITA.ID_SALA=CITMED.T_SALA.ID_SALA AND CITMED.T_CITA.TIPO_ID=CITMED.T_PACIENTE.TIPO_ID AND CITMED.T_CITA.ID_MEDICO=CITMED.T_MEDICO.ID_MEDICO AND CITMED.T_CITA.ID_PROCEDIMIENTO=CITMED.T_PROCEDIMIENTO_BASICO.ID_PROCEDIMIENTO";		  
					
					$query=$query." ORDER BY CITMED.T_CITA.FECHA_INICIO_CITA";
					$stmt = oci_parse($conec, $query);
					oci_execute($stmt);
					$citas = array();
					while($row = oci_fetch_array ($stmt, OCI_NUM)){
						
						if($row[17]!= "on"){
							$anes="No";
							}
						else
						{
							$anes="Si";
							}
						
						$nombre_paciente= $row[4]." ".$row[5];
						$nombre_medico = $row[7]." ".$row[8]; 
						
						$date1 = date_create($row[1]);
						$date2 = date_create($row[2]);
						$fecha= date_format($date1, 'd-m-Y');
						$fecha2= date_format($date1, 'G:ia');
						$fecha3= date_format($date2, 'G:ia');
						
						$citas[] = array("Fecha"=>$fecha,"Inicio Cita"=>$fecha2, "Final Cita" => $fecha3, "Status" => $row[13], "Sala" => $row[12],
						 "Procedimiento" => $row[10], "Equipo" => $row[15], "Pago" => $row[16], "Aneste." => $anes,
						  "Medico" => $nombre_medico, "Paciente" => $nombre_paciente, "Tipo" => $row[18]);
						
						}
						
						$titles = array('Fecha'=>'<b>Fecha</b>', 'Inicio Cita'=>'<b>Inicio Cita</b>' , 'Final Cita'=>'<b>Final Cita</b>' , 'Status'=>'<b>Status</b>' , 'Sala'=>'<b>Sala</b>' , 'Procedimiento'=>'<b>Procedimiento</b>' ,
	'Equipo'=>'<b>Equipo</b>','Pago'=>'<b>Pago</b>', 'Aneste.'=>'<b>Aneste.</b>', 'Medico'=>'<b>Medico</b>', 'Paciente'=>'<b>Paciente</b>', 'Tipo'=>'<b>Tipo</b>');
	 
	}
	
	$pdf->ezImage('images/HCCInforme.png',0, 130, 'none', 'left', '');
	
	$options = array( 
                'shadeCol'=>array(0.9,0.9,0.9), 
                'xOrientation'=>'center', 
                'width'=>750, 
                'fontSize'=>9 
            ); 
	$pdf->ezText("\n\n\n",3);
	$pdf->ezTable($citas,$titles,'',$options );
	 
	$pdf->ezText("\n\n\n",10);
	
	
	ob_end_clean();
	$pdf->ezStream();
?>