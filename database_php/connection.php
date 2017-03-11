<?php
/**
 * Created by IntelliJ IDEA.
 * User: Vartul
 * Date: 14/02/2017
 * Time: 19:36
 */
    require_once 'config.php';

    class DBConnection {

        private $connect;
        function __construct() {
            $this -> connect = mysqli_connect(hostname, username, password, dbName, port)
            or die("Could not connect to DB");
        }

        public function getConnection() {
            return $this -> connect;
        }
    }

 ?>