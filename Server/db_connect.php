<?php
 
class DB_Connect {
 
    // constructor
    function __construct() {
    
    }
 
    // destructor
    function __destruct() {
        // $this->close();
    }
 
    // Connecting to database
    public function connect() {
        require_once 'config.php';
        // connecting to mysql

        $con = mysqli_connect(DB_HOST, DB_USER, DB_PASSWORD,DB_DATABASE, DB_PORT);
        if (!$con) {
            die('Connect Error (' . mysqli_connect_errno() . ') '. mysqli_connect_error());
        }
 
        // return database handler
        echo "Host information: " . mysqli_get_host_info($con) . PHP_EOL;

        return $con;
    }
 
    // Closing database connection
    public function close() {
        mysqli_close();
    }
 
} 

$gpsdata = new DB_Connect();
$my_con = $gpsdata->connect();  
$query = "SELECT * from devices";
$result = mysqli_query($my_con, $query);
if(!$result){
    print("The database you attemped to access does not exist"); //Give some info about failure
}
echo $result->num_rows;

?>