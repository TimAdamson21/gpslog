<?php
include_once './db_functions.php';
//Create Object for DB_Functions class
$db = new DB_Functions();
//Get JSON posted by Android Application
//$json = $_POST["usersJSON"];

//Remove Slashes
if (get_magic_quotes_gpc()){
$json = stripslashes($json);
}
//Decode JSON into an Array
$devicesData = json_decode(file_get_contents("data.json"), true);
//echo '<p>devices data <pre>'; var_dump($devicesData); echo '</pre></p>';
foreach($devicesData as $data )
{
    $device_query = mysql_query( "SELECT `id` FROM devices WHERE `serial_number`='". $data['serial'] . "'" );
    $existing_device = mysql_fetch_array($device_query);
    if ( $existing_device )
    {
        echo '<p>got device with row id#'. $existing_device['id'];
        $newgpslog_id = $db->storeData( $existing_device['id'], $data['time'], $data['latitude'], $data['longitude'], $data['speed'] );
        if ( $newgpslog_id )
        {
            echo '<p>gps data added:'.$newgpslog_id.'</p>';
        }
        else
        {
            echo '<p>Gpslog data failed to add</p>';
        }
    }
    else
    {
        $newdevice_id = $db->storeDevice( $data['serial'], $data['model'], $data['os'], $data['connection_type'] );
        if ( $newdevice_id  )
        {
            echo '<p>'.$newdevice_id.' add new device  gps data to gpslog table</p>';
            $newgpslog_id = $db->storeData( $newdevice_id, $data['time'], $data['latitude'], $data['longitude'], $data['speed'] );
            if ( $newgpslog_id)
                echo '<p>new GPSdata#'.$newgpslog_id.' added</p>';
            else
                echo '<p>Failed to add gps data</p>';
        }
        else
            echo '<p>New Device add failed</p>';

    }
    echo '<hr>';
}
/*
//Util arrays to create response JSON

$a=array();
$b=array();
//Loop through an Array and insert data read from JSON into MySQL DB
$serial = (string)$data[0]["serial"];

//  output $serial to make sure it is the correct serial #

$result = mysql_query("SELECT `id` FROM devices WHERE `serial_number`= '{$serial}'");
if($result === FALSE) {
    die(mysql_error()); // TODO: better error handling
	}
	//while ($Device = mysql_fetch_array($result));
		if ($Device):
		{
			for($i=0; $i<count($data) ; $i++)
			//Store User into MySQL DB
			$res = $db->storeData($Device,$data[$i]["time"],$data[$i]["latitude"],$data[$i]["longitude"],$data[$i]["speed"]);
			//Based on insertion, create JSON response
			if($res){
				$b["time"] = $data[$i]["time"];
				$b["status"] = 'yes';
				array_push($a,$b);
			}else{
				$b["time"] = $data[$i]["time"];
				$b["status"] = 'no';
				array_push($a,$b);
				}
				}
	//Post JSON response back to Android Application
	echo json_encode($a);
else:{die;}
 */
