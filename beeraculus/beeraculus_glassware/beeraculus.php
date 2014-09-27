<?php

require_once 'config.php';
require_once 'mirror-client.php';
require_once 'google-api-php-client/src/Google_Client.php';
require_once 'google-api-php-client/src/contrib/Google_MirrorService.php';
require_once 'util.php';

$client = get_google_api_client();

// Authenticate if we're not already
if (!isset($_SESSION['userid']) || get_credentials($_SESSION['userid']) == null) {
  header('Location: ' . $base_url . '/oauth2callback.php');
  exit;
} else {
  verify_credentials(get_credentials($_SESSION['userid']));
  $client->setAccessToken(get_credentials($_SESSION['userid']));
}

// A glass service for interacting with the Mirror API
$mirror_service = new Google_MirrorService($client);

$new_timeline_item = new Google_TimelineItem();

switch ($_POST['operation']) {

	case 'deleteTimelineItem':
    delete_timeline_item($mirror_service, $_POST['itemId']);
    $message = "A timeline item has been deleted.";
    break;

}

if(isset($_GET['message'])) {

    $new_timeline_item->setText($_GET['message']);

    $notification = new Google_NotificationConfig();
    $notification->setLevel("DEFAULT");
    $new_timeline_item->setNotification($notification);
    insert_timeline_item($mirror_service, $new_timeline_item, null, null);
    $message = "Timeline Item inserted!";

}

//Load cool stuff to show them.
$timeline = $mirror_service->timeline->listTimeline(array('maxResults'=>'3'));



?>

<!doctype html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Beeraculus Glassware</title>
  <link href="./static/bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
  <link href="./static/bootstrap/css/bootstrap-responsive.min.css" rel="stylesheet" media="screen">
  <link href="./static/main.css" rel="stylesheet" media="screen">
</head>
<body>
<div class="navbar navbar-inverse navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <a class="brand" href="#">Beeraculus Glassware: Luna Hackathon 2014 Edition</a>
    </div>
  </div>
</div>

<div class="container">

  <?php if ($message != "") { ?>
  <div class="alert alert-info"><?php echo $message; ?> </div>
  <?php } ?>

  <h1>Your Recent Timeline</h1>
  <div class="row">

    <div style="margin-top: 5px;">
      <?php if ($timeline->getItems()) { ?>
        <?php foreach ($timeline->getItems() as $timeline_item) { ?>
        <div class="span4">
          <table class="table table-bordered">
            <tbody>
              <tr>
                <th>ID</th>
                <td><?php echo $timeline_item->getId(); ?></td>
              </tr>
              <tr>
                <th>Text</th>
                <td><?php echo htmlspecialchars($timeline_item->getText()); ?></td>
              </tr>
              <tr>
                <th>HTML</th>
                <td><?php echo htmlspecialchars($timeline_item->getHtml()); ?></td>
              </tr>
              <tr>
                <th>Attachments</th>
                <td>
                  <?php
                  if ($timeline_item->getAttachments() != null) {
                    $attachments = $timeline_item->getAttachments();
                    foreach ($attachments as $attachment) { ?>
                        <img src="<?php echo $base_url .
                            '/attachment-proxy.php?timeline_item_id=' .
                            $timeline_item->getId() . '&attachment_id=' .
                            $attachment->getId() ?>" />
                    <?php
                    }
                  }
                  ?>
                </td>
              </tr>
              <tr>
                <td colspan="2">
                  <form class="form-inline" method="post">
                    <input type="hidden" name="itemId" value="<?php echo $timeline_item->getId(); ?>">
                    <input type="hidden" name="operation" value="deleteTimelineItem">
                    <button class="btn btn-danger btn-block" type="submit">Delete Item</button>
                  </form>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <?php 
        }
      } else { ?>
      <div class="span12">
        <div class="alert alert-info">
          You haven't added any items to your timeline yet. Use the controls
          below to add something!
        </div>
      </div>
      <?php
      } ?>
    </div>
  </div>

        </div>

<script
    src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="/static/bootstrap/js/bootstrap.min.js"></script>
</body>
</html>