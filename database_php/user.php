<?php
/**
 * Created by IntelliJ IDEA.
 * User: Vartul
 * Date: 14/02/2017
 * Time: 19:37
 */

// place all files in database_php on Webserver (/Library/WebServer/Documents/database_php)
require_once 'connection.php';
header('Content-Type: application/json');

class User {

    private $db;
    private $connection;

    function __construct() {
        $this -> db = new DBConnection();
        $this -> connection = $this->db->getConnection();
    }

    public function does_user_exist($email,$password)
    {
        $query = "Select * from User where email='$email' and password = '$password'";
        $result = mysqli_query($this->connection, $query);
        if(mysqli_num_rows($result)>0){
            $json['success'] = ' Welcome '.$email;
            echo json_encode($json);
            mysqli_close($this -> connection);
        }else{
            $query = "insert into User (email, password) values ( '$email','$password')";
            $inserted = mysqli_query($this -> connection, $query);
            if($inserted == 1 ){
                $json['success'] = 'Account created';
            }else{
                $json['error'] = 'Wrong password';
            }
            echo json_encode($json);
            mysqli_close($this->connection);
        }

    }

}

$user = new User();
if(isset($_POST['email'],$_POST['password'])) {
    $email = $_POST['email'];
    $password = $_POST['password'];

    if(!empty($email) && !empty($password)){

        $encrypted_password = md5($password);
        $user-> does_user_exist($email,$encrypted_password);

    }else{
        echo json_encode("you must type both inputs");
    }

} else {
    echo json_encode("you must type both inputs");
}


?>