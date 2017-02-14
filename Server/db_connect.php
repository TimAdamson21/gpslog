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
        $con = mysqli_connect(DB_HOST, DB_USER, DB_PASSWORD);
        // selecting database
        mysqli_select_db($con, DB_DATABASE);
 
        // return database handler
        echo "Host information: " . mysqli_get_host_info($con) . PHP_EOL;

        return $con;
    }
 
    // Closing database connection
    public function close() {
        mysql_close();
    }
 
} 

$gpsdata = new DB_Connect();
$my_con = $gpsdata->connect();  
$query = "SELECT * from gpslog";
$result = mysqli_query($my_con, $query);
echo $result->num_rows;

?>