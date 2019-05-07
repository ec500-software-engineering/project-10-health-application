# Health Application

In this project, we will build a health advisor based on patterns your phone knows about you. We will build an Android App and utilize the AI kernel model developed in [Kaggle](https://www.kaggle.com/morrisb/what-does-your-smartphone-know-about-you) to analyse your daily activities, then give you health advices.

<img src="./images/running.gif" height="30%" width="30%">


<img src="./images/goal.gif" height="30%" width="30%">

## System Design & Architecture
<img src="./images/system_diagram.PNG">  

## User Story
As an user of Health app, I will most likely to use it to monitor my daily activities:
- Display six activities (WALKING, WALKING_UPSTAIRS, WALKING_DOWNSTAIRS, SITTING, STANDING, LAYING) if possible
- Monitor user activity continuously in the background
- Obtain the distance of performing each activity
- Calculate the corresponding calories consumption for each activity
- Sum up the calories for all activity and generate a final score
- Able to set up a health goal(plan) for a period of time
- Compare the goal and actual score in real life to see if accomplishment checks.

## Development Environment setup

### Data gathering
- Using its embedded accelerometer and gyroscope, we captured 3-axial linear acceleration and 3-axial angular velocity at a constant rate of 50Hz. 

### Data processing
- The AI Model from [Kaggle](https://www.kaggle.com/morrisb/what-does-your-smartphone-know-about-you) will process data and categorize six activities (WALKING, WALKING_UPSTAIRS, WALKING_DOWNSTAIRS, SITTING, STANDING, LAYING).

### UI design
- The user interface is developed in [Android Studio](https://developer.android.com/studio/?gclid=EAIaIQobChMIh9Dg9_CY4QIVLrCzCh1FhAj9EAAYASAAEgJ_TPD_BwE). The XML files will correlated with Java files.
- The emulator is temporarily set as "Nexus 4 API23".

## Iterations Plan

### Sprint 1
system design  
user story  

### Sprint 2
development environment set up  
CI/CD pipeline set up  
learning android development  

### Sprint 3
UI design  
data model design  
implement AI module  

### Sprint 4
implement&test android application  

## Final
- UI Background thread and sensor thread initialized after opening the app.
- Sensor start gathering data from accelerometer and gyroscope.
- After data processing from AI model, the time value of performaning each activity will be passed to the UI and displayed in calories after computing calories & dist formula.  
<img src="./images/UI.1.png" height="30%" width="30%">

- Stop Sensor thread after pressing "NEW GOAL".   
- Passing the new goal value from pop-out dialog to UI after clicking "ok". No value changes if "cancel".  
- The sensor thread will resume again after clicking "ok" or "cancel".  
<img src="./images/UI.2.png" height="30%" width="30%">

- Animated "GOAL" will explode after user reaches designated goal value.  
<img src="./images/UI.3.png" height="30%" width="30%">

## Team Members
* Leyang Shen
* Yuxuan Su
