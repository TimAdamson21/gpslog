<?php

class DB_Functions {

    private $gpsdata;

    //put your code here
    // constructor
    function __construct() {
        include_once './db_connect.php';
        // connecting to database
        $this->gpsdata = new DB_Connect();
        $this->gpsdata->connect();
    }

    // destructor
    function __destruct() {

    /**
     * Storing new user
     * returns user details
     */
	}

    public function storeDevice( $serial_number, $model = '', $os = '', $connection_type = '' )
    {
        $query = "INSERT INTO `devices` (`serial_number`, `model`, `os`, `connection_type`) "
                . " VALUES ('{$serial_number}', '{$model}','{$os}','{$connection_type}' )";
        echo '<p>New Device query:'.$query.'</p>';
        $result = mysqli_query($query);
        if ($result) {
            return mysqli_insert_id();
            echo "Successful storage of device data";
        } else {
            return false;
            echo "Failed to store device data";
        }
    }

    public function storeData($device_id, $created_at, $latitude, $longitude, $speed = '') {

        $query = "INSERT INTO `gpslog` (`device_id`, `created_at`, `latitude`, `longitude`, `speed`) "
                . " VALUES ({$device_id}, '{$created_at}','{$latitude}','{$longitude}', '{$speed}' )";
        echo '<p>New Gpsdata query:'.$query.'</p>';
        $result = mysqli_query($query);
        if ($result) {
            return mysqli_insert_id();
        } else {
            return false;
        }
    }
     /**
     * Getting all users
     */
    public function getAllUsers() {
        $result = mysqli_query("select * FROM gpslog");
        return $result;
    }
}

?>
