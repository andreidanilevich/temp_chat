<?php // сохранить в utf-8 !
// ---------------------------------------------------------- эти значения задавались при создании БД на сервере
$mysql_host = "localhost"; // sql сервер
$mysql_user = "l29340eb_chat"; // пользователь
$mysql_password = "123456789"; // пароль
$mysql_database = "l29340eb_chat"; // имя базы данных chat

// ---------------------------------------------------------- проверяем переданные в строке запроса параметры
// например ...chat.php?action=select
//-----------------------------------------------------------
// переменная action может быть:
// select - формируем содержимое таблицы chat в JSON и отправляем назад
// insert - встваляем новую строку в таблицу chat, так же нужны 4 параметра : автор/получатель/время создания/сообщение
// ВАЖНО время создания мы не передаем в параметрах, его берем текущее на сервере
// delete - удаляет ВСЕ записи из таблицы chat - пусть будет для быстрой очистки

// ------------------------------------------- получим переданный action
if (isset($_GET["action"])) { 
    $action = $_GET['action'];
}
// ------------------------------------------- если action=insert тогда получим еще author|client|text
if (isset($_GET["author"])) { 
    $author = $_GET['author'];
}
if (isset($_GET["client"])) { 
    $client = $_GET['client'];
}
if (isset($_GET["text"])) { 
    $text = $_GET['text'];
}
// ------------------------------------------- если action=select тогда получим еще data - от после какого времени передавать ответ
if (isset($_GET["data"])) { 
    $data = $_GET['data'];
}



mysql_connect($mysql_host, $mysql_user, $mysql_password); // коннект к серверу SQL
mysql_select_db($mysql_database); // коннект к БД на сервере
mysql_set_charset('utf8'); // кодировка
// ------------------------------------------------------------ обрабатываем запрос если он был
if($action == select){ // если действие SELECT

if($data == null){
// выберем из таблицы chat ВСЕ данные что есть и вернем их в JSON
$q=mysql_query("SELECT * FROM chat");


}else{
	
// выберем из таблицы chat ВСЕ данные ПОЗНЕЕ ОПРЕДЕЛЕННОГО ВРЕМЕНИ и вернем их в JSON
$q=mysql_query("SELECT * FROM chat WHERE data > $data");	
	
}
while($e=mysql_fetch_assoc($q))
        $output[]=$e;
print(json_encode($output));

}


if($action == insert && $author != null && $client != null && $text != null){ // если действие INSERT и есть все что нужно

// время = время сервера а не клиента !
$current_time = round(microtime(1) * 1000);
// пример передачи скрипту данных:
// http://andreidanilevich.comoj.com/chat.php?action=insert&author=author&client=client&text=text
// вставим строку с переданными параметрами
mysql_query("INSERT INTO `chat`(`author`,`client`,`data`,`text`) VALUES ('$author','$client','$current_time','$text')");

}


if($action == delete){ // если действие DELETE
// полностью обнулим таблицу записей
mysql_query("TRUNCATE TABLE `chat`");	
}

mysql_close();
?>