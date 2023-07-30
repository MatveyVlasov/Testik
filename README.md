# Testik

Testik - это приложение для создания и прохождения тестов. Приложение находится в разработке, однако часть функционала уже доступна:
<ul>
    <li>Регистрация, авторизация и восстановление пароля</li>
    <li>Главный экран со списком тестов, разбитых по категориям</li>
    <li>Профиль пользователя</li>
    <li>Добавление, обрезка и просмотр изображений</li>
    <li>Создание теста</li>
        <ul>
            <li>Добавление вопросов</li>
            <li>Настройка теста (ограничение по времени, возможность навигации, доступ к результатам и др.)</li>
            <li>Добавление системы оценивания</li>
            <li>Просмотр результатов пользователей</li>
        </ul>
    <li>Прохождение теста</li>
    <li>Просмотр результатов теста</li>
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
            <li>Dynamic Links</li>
        </ul>
</ul>


# Описание

При первом входе в приложение пользователь попадает на экран авторизации. Здесь можно войти в аккаунт, изменить язык интерфейса, перейти к восстановлению пароля или созданию нового аккаунта

<p align="center">
    <img src="https://i.imgur.com/1jtXnlM.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/NFx3PCr.png" width="250"/>
</p>

Перейдём на экран регистрации. Здесь пользователь вводит почту, пароль, а также имя пользователя

<p align="center">
    <img src="https://i.imgur.com/G0LjU7r.png" width="300"/>
</p>

После успешной регистрации (а также авторизации и при повторном входе в приложение) пользователь попадает на главный экран с опубликованными тестами,
разделёнными по категориям

<p align="center">
    <img src="https://i.imgur.com/X3qD19n.png" width="300"/>
</p>

Для получения полного списка тестов в определённой категории нужно кликнуть по нужной категории. Здесь же можно осуществлять поиск по автору

<p align="center">
  <img src="https://i.imgur.com/USi6C9e.png" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/YbhHZ6q.png" width="250" />
</p>

По клику на аватарку в правом верхнем углу главного экрана можно перейти к профилю. Здесь можно поменять информацию о пользователе и аватар, сменить пароль, выйти из аккаунта или вообще удалить его

<p align="center">
  <img src="https://i.imgur.com/jszXOtz.png" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/tQkIgnz.png" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/hZYhQ9W.png" width="250" />
</p>

Здесь же можно изменить изображение пользователя

<p align="center">
  <img src="https://i.imgur.com/0D2iOLh.png" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/wraS1Hv.png" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/krenG4Q.png" width="250" />
</p>

С помомощью нижней навигационной панели можно перейти к списку созданных тестов. По клику на тест можно перейти к его редактированию.
Дополнительные действия доступны по клику на три точки справа

<p align="center">
    <img src="https://i.imgur.com/Fvsyvqz.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/v1ETZgO.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/o14ZahN.png" width="250"/>
</p>

Далее представлен экран создания теста, где нужно выбрать название и категорию теста, а также - по желанию - его изображение, описание и пароль для доступа к прохождению теста. После создания теста появляется большое количество дополнительных настроек

<p align="center">
    <img src="https://i.imgur.com/UTGPD1N.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/1BnxDoT.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/fGizwSX.png" width="250"/>
</p>

Перейдём к созданию вопросов

<p align="center">
    <img src="https://i.imgur.com/9dMIBZd.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/aOI7wfF.png" width="250"/>
</p>

В вопросах определённых типов могут появляться дополнительные настройки

<p align="center">
    <img src="https://i.imgur.com/QymmhZr.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/fBniJ5c.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/bM6AhTO.png" width="250"/>
</p>

А так выглядит список вопросов

<p align="center">
  <img src="https://i.imgur.com/Vq8wmkn.png" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/x3tNin9.png" width="250" />
</p>

Создатель теста может управлять системой оценивания теста. Если система оценивания включена, пользователи получают оценки после прохождения теста.
Если система оценивания выключена или отсутствует оценка для определённого количества баллов, показывается процент правильных ответов

<p align="center">
  <img src="https://i.imgur.com/T0aDJ0E.png" width="250" /> &nbsp;&nbsp;
  <img src="https://i.imgur.com/LRNWfV5.png" width="250" />
</p>

При клике на карточку теста на главном экране открывается диалоговое окно с информацией о тесте

<p align="center">
    <img src="https://i.imgur.com/pmYwxYd.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/2qmbVQ7.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/8iMkMY6.png" width="250"/>
</p>

Общий вид экрана прохождения теста зависит от настроек навигации. Если создатель теста сделал навигацию возможной, то экран будет выглядеть так, как показано слева, при выключенной навигации - как показану по центру, а экран, где показываются правильные ответы после каждого вопроса, представлен справа

<p align="center">
    <img src="https://i.imgur.com/WZJD2uy.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/4UBzha5.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/pnWmaO9.png" width="250"/>
</p>

Каждый вопрос содержит порядковый номер, количество баллов за правильный ответ, заголовок и описание. Остальное содержимое экрана
зависит от типа вопроса. Ниже представлены вопрос с множественным выбором, на соотношение, а также типа «Правда или ложь»

<p align="center">
    <img src="https://i.imgur.com/ql7QxkI.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/ZWLgLT7.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/QCLpTZ9.png" width="250"/>
</p>

По завершении теста пользователь попадает на экран с результатами теста

<p align="center">
    <img src="https://i.imgur.com/erXa9nR.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/finJeAy.png" width="250"/>
</p>

Переход к списку пройденных тестов осуществляется через нижнюю навигационную панель

<p align="center">
    <img src="https://i.imgur.com/4P8KcV2.png" width="300"/>
</p>

По клику на тест пользователь переходит к экрану с подробной информации о пройденном тесте, где при нажатии на конкретный вопрос можно перейти к подробной информации о данном вопросе

<p align="center">
    <img src="https://i.imgur.com/Wdpiher.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/HwATbB7.png" width="250"/> &nbsp;&nbsp;
    <img src="https://i.imgur.com/LtADcUl.png" width="250"/>
</p>
