<?php

class DB_Functions {

    private $gpsdata;
    private $con;

    //put your code here
    // constructor
    function __construct() {
        include_once './db_connect.php';
        // connecting to database
        $this->gpsdata = new DB_Connect();
        $this->con = $this->gpsdata->connect();
    }

    // destructor
    function __destruct() {

    /**
     * Storing new user
     * returns user details
     */
	}

    public function getConnection(){
        return $this->con;
    }

    public function storeDevice( $serial_number, $model = '', $os = '', $connection_type = '' )
    {
        $query = "INSERT INTO `devices` (`serial_number`, `model`, `os`, `connection_type`) "
                . " VALUES ('{$serial_number}', '{$model}','{$os}','{$connection_type}' )";
        ////echo '<p>New Device query:'.$query.'</p>';
        $result = mysqli_query($this->con, $query);
        if ($result) {
            ////echo "Successful storage of device data", PHP_EOL;
            return mysqli_insert_id($this->con);
        } else {
            echo "Failed to store device data";
            return false;
        }
    }

    public function storeData($device_id, $created_at, $latitude, $longitude, $speed = '', $hidden_state, $toSend, $TripID) {

        $query = "INSERT INTO `gpslog` (`device_id`, `created_at`, `latitude`, `longitude`, `speed`, `hidden_state`, `real_time_to_send`, `TripID`) "
                . " VALUES ({$device_id}, '{$created_at}','{$latitude}','{$longitude}', '{$speed}', '{$hidden_state}', '{$toSend}', '{$TripID}' )";
        ////echo '<p>New Gpsdata query:'.$query.'</p>';
        $result = mysqli_query($this->con, $query);
        if ($result) {
            return mysqli_insert_id($this->con);
        } else {
            return false;
        }
    }
     /**
     * Getting all users
     */
    public function getAllUsers() {
        $result = mysqli_query($this->con, "select * FROM gpslog");
        return $result;
    }
}

?>
