<?php
/**
 * Created by IntelliJ IDEA.
 * User: Vartul
 * Date: 15/03/2017
 * Time: 06:37
 */

// place all files in database_php on Webserver (/Library/WebServer/Documents/database_php)
require_once 'connection.php';
header('Content-Type: application/json');

class Potholes {

    private $db;
    private $connection;

    function __construct() {
        $this -> db = new DBConnection();
        $this -> connection = $this->db->getConnection();
    }

    public function add_pothole($email, $aacX, $accY, $accZ, $latitude, $longitude) {
        $query = "INSERT INTO Potholes(email, acc_x, acc_y, acc_z, latitude, longitude) VALUES ('$email', '$aacX', '$accY', '$accZ', '$latitude', '$longitude')";
        $inserted = mysqli_query($this -> connection, $query);
        if($inserted == 1 ){
            $json['success'] = 'Pothole Saved';
        }else{
            $json['error'] = 'Pothole not saved';
        }
        echo json_encode($json);
        mysqli_close($this->connection);

    }

    public function get_location($email) {
        $query = "SELECT latitude, longitude FROM Potholes WHERE email = '$email'";
        $result = mysqli_query($this->connection, $query);
        if(mysqli_num_rows($result)>0){
            $json['Potholes'] = array();
            while($row = mysqli_fetch_assoc($result)) {
                $row_array['latitude'] = $row['latitude'];
                $row_array['longitude'] = $row['longitude'];
                array_push($json['Potholes'], $row_array);
            }
            echo json_encode($json);
            mysqli_close($this->connection);
        }
    }

}

$potholes = new Potholes();
if(isset($_POST['email'],$_POST['aacX'],$_POST['accY'],$_POST['accZ'], $_POST['latitude'],$_POST['longitude'])) {
    $email = $_POST['email'];
    $aacX = $_POST['aacX'];
    $accY = $_POST['accY'];
    $accZ = $_POST['accZ'];
    $latitude = $_POST['latitude'];
    $longitude = $_POST['longitude'];

    if(!empty($email) && !empty($aacX) && !empty($accY) && !empty($accZ) && !empty($latitude) && !empty($longitude)){

        $potholes-> add_pothole($email, floatval($aacX), floatval($accY), floatval($accZ), floatval($latitude), floatval($longitude));

    }else{
        echo json_encode("a input is empty");
    }

} else if(isset($_POST['email'], $_POST['getLocation'])){
    $email = $_POST['email'];
    $getLocation = $_POST['getLocation'];

    if($getLocation == '1') {
        $potholes-> get_location($email);
    }
} else {
    echo json_encode("you must type all inputs");
}


?>