# Testik

Testik - это приложение для создания и прохождения тестов. Приложение находится в разработке, однако часть функционала уже доступна:
<ul>
    <li>Регистрация, авторизация и восстановление пароля.</li>
    <li>Главный экран со списком тестов, разбитых по категориям.</li>
    <li>Профиль пользователя.</li>
    <li>Добавление, обрезка и просмотр изображений.</li>
    <li>Создание теста.</li>
        <ul>
            <li>Добавление вопросов.</li>
            <li>Добавление системы оценивания.</li>
            <li>Просмотр результатов пользователей.</li>
        </ul>
    <li>Прохождение теста.</li>
    <li>Просмотр результатов теста.</li>
</ul>
  


# Скачать

Скачать APK файл можно по <a href="testik.apk">ссылке</a>

# Стек технологий

<ul>
    <li>Kotlin</li>
    <li>MVVM</li>
    <li>Clean Architecture</li>
    <li>Hilt</li>
    <li>Firebase</li>
        <ul>
            <li>Authentication</li>
            <li>Firestore Database</li>
            <li>Storage</li>
            <li>Functions</li>
        </ul>
</ul>


# Описание

При первом входе в приложение пользователь попадает на экран авторизации. Здесь можно войти в аккаунт, изменить язык, перейти к восстановлению пароля 
или созданию нового аккаунта

<p align="center">
    <img src="https://i.imgur.com/bJAXd4W.jpg" width="300"/>
</p>

Перейдём на экран регистрации. Здесь пользователь вводит почту, пароль, а также имя пользователя

<p align="center">
    <img src="https://i.imgur.com/gUjEkOO.jpg" width="300"/>
</p>

После успешной регистрации (а также авторизации и при повторном входе в приложение) пользователь попадает на главный экран с опубликованными тестами,
разделёнными по категориям (в данный момент для примера доступно 3 категории)

<p align="center">
    <img src="https://i.imgur.com/o5zsNDM.jpg" width="300"/>
</p>

По клику на аватарку в правом верхнем углу можно перейти на экран профиля. Здесь можно поменять информацию о пользователе и аватар, сменить пароль,
выйти из аккаунта или вообще удалить его

<p align="center">
  <img src="https://i.imgur.com/JYp1Moh.jpg" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/bltVqRO.jpg" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/3GMwd5I.jpg" width="250" />
</p>

С помомощью нижней навигационной панели можно перейти к списку созданных тестов. По клику на тест можно перейти к его редактированию.
Дополнительные действия доступны по клику на три точки справа

<p align="center">
    <img src="https://i.imgur.com/NI76eU5.jpg" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/Ruefgh2.jpg" width="250"/>
</p>

Экран создания/редактирования теста представлен ниже. Рядом - экраны выбора и обрезки изображения

<p align="center">
  <img src="https://i.imgur.com/jwmQYMx.jpg" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/699AryW.jpg" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/yLuAHrq.jpg" width="250" />
</p>

Пришло время создавать вопросы. Длинный экран получился, правда? :)

<p align="center">
    <img src="https://i.imgur.com/oWBIQlo.jpg" width="300"/>
</p>

Ниже представлено возможное уведомление об ошибке, а также список вопросов

<p align="center">
  <img src="https://i.imgur.com/5X7iSNQ.jpg" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/pT0Cxzj.jpg" width="250" />
</p>

Тест готов - самое время протестировать. Это можно сделать с помощью демо теста: всё как по-настоящему, но запись о прохождении теста долго не хранится

<p align="center">
    <img src="https://i.imgur.com/9m3F5Rq.jpg" width="300"/>
</p>

Все подробности видны на скриншотах, но для разнообразия - интересный факт: осуществлять навигацию между вопросами можно тремя способами: 
кликом по номеру вопроса в верхней части экрана, свайпом вправо или влево, а также с помощью стрелочек в нижней части экрана

<p align="center">
  <img src="https://i.imgur.com/3TiQidu.jpg" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/WMzDQxM.jpg" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/qmnLPFG.jpg" width="250" />
</p>

После прохождения теста мы попадаем на экран с записями о всех пройденных тестах, где можно посмотреть результаты как по тесту в целом,
так и отдельно по каждому вопросу

<p align="center">
  <img src="https://i.imgur.com/ELtWEUV.jpg" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/vXmTYtZ.jpg" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/VdOhJZm.jpg" width="250" />
</p>

Помимо всего прочего, создатель теста может добавить систему оценивания. В таком случае в результатах теста будет указан не процент правильных ответов, а оценка

<p align="center">
  <img src="https://i.imgur.com/BH6h2af.jpg" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/opdRc9e.jpg" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/cHo4G29.jpg" width="250" />
</p>

Эта же оценка появляется в списке пройденных пользователем тестов, а также в результатах теста, доступных его создателю

<p align="center">
  <img src="https://i.imgur.com/0gNYUU8.jpg" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/dhbn5gt.jpg" width="250" />
</p>
