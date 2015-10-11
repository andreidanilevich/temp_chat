<?php // сохранить utf-8 !
// -------------------------------------------------------------------------- логины пароли
$mysql_host = "localhost"; // sql сервер
$mysql_user = "l29340eb_chat"; // пользователь
$mysql_password = "123456789"; // пароль
$mysql_database = "l29340eb_chat"; // имя базы данных chat
// -------------------------------------------------------------------------- если база недоступна
if (!mysql_connect($mysql_host, $mysql_user, $mysql_password)){
	
echo "<h2>База недоступна!</h2>";
exit;
}else{
// -------------------------------------------------------------------------- если база доступна
echo "<h2>База доступна!</h2>";


mysql_select_db($mysql_database);
mysql_set_charset('utf8');
// -------------------------------------------------------------------------- выведем JSON
$q=mysql_query("SELECT * FROM chat");
echo "<h3>Json ответ:</h3>";
// Выводим json
while($e=mysql_fetch_assoc($q))
        $output[]=$e;
print(json_encode($output));

// -------------------------------------------------------------------------- выведем таблицу
$q=mysql_query("SELECT * FROM chat");
echo "<h3>Табличный вид:</h3>";

echo "<table border=\"1\" width=\"100%\" bgcolor=\"#999999\">";
echo "<tr><td>_id</td><td>author</td>";
echo "<td>client</td><td>data</td><td>text</td></tr>";

for ($c=0; $c<mysql_num_rows($q); $c++){

$f = mysql_fetch_array($q);
echo "<tr><td>$f[_id]</td><td>$f[author]</td><td>$f[client]</td><td>$f[data]</td><td>$f[text]</td></tr>";

}
 echo "</tr></table>";

}
mysql_close();
// -------------------------------------------------------------------------- разорвем соединение с БД
?>


