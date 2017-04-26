<?php
include_once './db_functions.php';
//Create Object for DB_Functions class
$db = new DB_Functions();

//Get JSON posted by Android Application
$json = $_POST["usersJSON"];

//Remove Slashes
if (get_magic_quotes_gpc()){
$json = stripslashes($json);
}
//Decode JSON into an Array
//$devicesData = json_decode(file_get_contents("data.json"), true); //This is to test with browser
$devicesData = json_decode($json,true); //This is for android
//echo '<p>devices data <pre>'; var_dump($devicesData); echo '</pre></p>';


function addData($id,$data){
    global $db, $jsonResponse;
    $newgpslog_id = $db->storeData( $id, $data['time'], $data['latitude'], $data['longitude'], $data['speed'], $data['hidden_state'] );
        if ( $newgpslog_id )
        {
            ////echo '<p>gps data added:'.$newgpslog_id.'</p>';
            $jsonData = array( //Creates an array for this data entry
            "time" => $data["time"],
            "status" => "yes",
            );

            array_push($jsonResponse,$jsonData);

        }
        else
        {
            echo '<p>Failed to add gpslog data</p>';
            print("The data that we tried to use was ");
            var_dump($data);
        }
}

//var_dump($devicesData);
$jsonResponse = array();
foreach($devicesData as $data )
{
    $device_query = mysqli_query($db->getConnection(),"SELECT `id` FROM devices WHERE `serial_number`='". $data['serial'] . "'" );
    $existing_device = mysqli_fetch_array($device_query);
    if ( $existing_device )
    {
        ////echo '<p>got device with row id#'. $existing_device['id'];
        addData($existing_device['id'],$data);
    }
    else
    {
        ////print("Attempting to create a new device \n");
        $newdevice_id = $db->storeDevice( $data['serial'], $data['model'], $data['os'], $data['connection_type'] ); //Adds the device, and returns an sql id, or false
        ////print("The new device ID is $newdevice_id");
        if ( $newdevice_id  ) //If new device successfully created
        {
            ////echo '<p>'.$newdevice_id.' add new device  gps data to gpslog table</p>';
            addData($newdevice_id,$data);
        }
        else{
            ////echo '<p>New Device add failed</p>';
        }

    }
    ////echo '<hr>';
}
print(json_encode($jsonResponse));
/*
//Util arrays to create response JSON

$a=array();
$b=array();
//Loop through an Array and insert data read from JSON into MySQL DB
$serial = (string)$devicesData[0]["serial"];

//  output $serial to make sure it is the correct serial #

$result = mysqli_query($db->getConnection(),"SELECT `id` FROM devices WHERE `serial_number`= '{$serial}'");
if($result === FALSE) {
    die(mysqli_error()); // TODO: better error handling
	}
	//while ($Device = mysqli_fetch_array($result));
    $Device = mysqli_fetch_array($result);
		if ($Device):
		{
			for($i=0; $i<count($DevicesData[0]) ; $i++)
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
