During my internship at General Mobile, I was assigned the task of developing an Android app centred on augmented reality. The app was developed in Android Studio and involved ARCore as well as an SQLite database.

### Google’s ARCore

ARCore is a software development kit by Google that combines crucial components of an augmented reality system for application developers. The software kit relies on functionalities such as motion tracking, environmental understanding or surface detection, and light estimation to create a developer friendly interface. With the additional use of Sceneform SDK, 3D object rendering within the app is simplified and boilerplate code required to run ARCore is reduced.

Official ARCore site: [https://developers.google.com/ar/discover](https://developers.google.com/ar/discover)

It must be noted that using ARCore has been computationally heavy on the devices used during development (GM9 Pro and Samsung Galaxy S8), most probably due to the real-time 3D object rendering and continuous surface detection involved. At the same time, the code I wrote for the ARCore functionality and/or the 3D objects I used and have may have been suboptimal and inefficient.

### Game Design

The game is played on a horizontal flat surface that is selected by the user upon tapping on a detected surface /plane.
The player steers a 3D car object on the surface using a joystick as well as a “D”/”R” switch button where “D” stands for “Drive” in the sense of forward motion and “R” stands for “Reverse”. 
Upon the start of the game, “box of donut” objects are placed randomly within a certain radius and are given a spin animation. 
A countdown timer and a scoreboard are on display and the aim of the player is to collect as many boxes of donuts as possible by driving the car towards them before time is up. 
A sound is played upon score increases. 
At the end of the game, a 3D object (designed in Tinkercad) falls onto the car and the high score is displayed and updated if necessary.

## Gameplay Screenshots:

<p float="left">
  <img src="/images/gameplay.jpg" width="200"/>
  <img src="/images/timeout.jpg" width="200"/> 
</p>

Significant portions of the source code are briefly explained below:

### **Main Activity**

 **(Runnable) directionChecker:**

* Controls the translation and rotation motions of the car object based on data read from the joystick with periodic updates

* Controls object collision and removal as well as score incrementation

**(method) onCreate:**

* User Interface components are initialised

* Countdown timer, joystick functionality and sound affects set up

**(method) startGame:**

* AR functionality set up

* 3D objects generated and rendered

**(method) endScreen:**

* Gameplay functions stopped

* The 3D object for the ending scene rendered

* Ending Scene animation carried out

* The database holding the high score accessed and written to if high score is bested

**SQLite Database Classes**

* **HighScore:** the object type that is stored within the database

* **HighScoreDao:** Data access object- an interface for data access

* **ScoreDatabase:** Holds HighScoreDao

* **ScoreRepository:** Carries out the asynchronous tasks as commanded by the Dao such as insert and delet
