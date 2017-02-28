<?php
        include_once 'db_functions.php';
        $db = new DB_Functions();
        $users = $db->getAllUsers();
        if ($users != false){
            $no_of_users = mysqli_num_rows($users);
          }
        else {
            $no_of_users = 0;
          }
    ?>


<!DOCTYPE html>
<html>
  <head>
  <meta charset="utf-8" />
  <title>View Users</title>
  <link href="viewusers.css" type="text/css" rel="stylesheet" />
  
    <script>
      function refreshPage(){
      location.reload();
      }
    </script>
  </head>

  <body>
    <center>
      <div class="header">
        Android SQLite and MySQL Sync Results
      </div>
    </center>

    <?php
        if ($no_of_users > 0){
    ?>
      <table>
        <tr id="header"><td>Id</td><td>Username</td></tr>
        <?php
            while ($row = mysqli_fetch_array($users)) {
        ?> 

        <tr>
          <td><span><?php echo $row["id"] ?></span></td>
          <td><span><?php echo $row["Name"] ?></span></td>
        </tr>

        <?php } ?>
      </table>
    <?php } else{ 
      ?>
      <div id="norecord">
        No records in MySQL DB
      </div>

      <?php } 
      ?>
      <div class="refresh">
        <button onclick="refreshPage()">Refresh</button>
      </div>
  </body>
</html>