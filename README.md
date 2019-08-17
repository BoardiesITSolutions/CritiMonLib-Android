<img src="https://critimon.com/images/logo.png" width="150">


# Introduction
The Android CritiMon library allows you to send both handled and
unhandled crashes to the CritiMon Crash Monitoring service 
(https://critimon.boardiesitsolutions.com). 

# Installing
The CritiMon android library, can be installed directly from 
[GitHub](https://github.com) using [JitPack](https://jitpack.io). 

In your build.gradle within your app module (not the project scope
gradle file), you need to add the following:
```
repositories {
        maven { url 'https://jitpack.io' }
    }
```

Then in the dependencies section add the library as below:
```
dependencies {
        implementation 'com.github.BoardiesITSolutions:CritiMonLib-Android:TAG'
    }
```

Where TAG is the latest tagged release version number at https://github.com/BoardiesITSolutions/CritiMonLib-Android/releases.

You should then be able to start using the library. 

# Using the Library
If your android project has multiple different activities, then every
activity will need to initialise CritiMon. Therefore the easiest thing to
do is to create a class of type activity and set up the CritiMon library
in this class, and then in your main activity class(es) extend this new class. 

An example is below:

BaseActivity.java
```
class BaseActivity extends AppCompatActivity
{
    protected CritiMon;
    
    protected void onCreate(Bundle savedInstanceParams)
    {
        super.onCreate(savedInstanceState);
        //Initialise CritiMon here
    }
}
``` 

MainActivity.java
```
class MainActivity extends BaseActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Your main activity application logic
    }
}
```

To initialise CritiMon you can do the following

```
CritiMon.Initialise(MainActivity.this, <api_key>, <app_id>, <app_version>);
```

The `<api_key>` can be found from the settings page. There is a button next to the API key that
copies the API key directly to your clipboard. 

The `<app_id>` can be found on the application list page and is an 8 digit number. Again there
is a button next to the app id so the app id can be copied to your clipboard.

The `<app_version>` is a string representation of the version of your app. The best way to do this
would be to add your app version to your strings.xml file and then use the strings.xml version
for the app version parameter.

If you are only interested in receive fatal crashes - i.e. crashes where it will
cause Android to force close your app, then the above is all you need to do. 

However, if you want to report on an error that has been handled, i.e. within a try/catch block then
you can use the `ReportCrash` method. An example of this is done below:

```
try
{
    EditText myTextBox = null;
    myTextBox.setText("Hello World"); //This will throw an exception
}
catch (NullPointerException ex)
{
    CritiMon.ReportCrash(ex, CritiMon.CrashSeverity.Medium);
    //Send a basic key/value pair for extra debug information
    CritiMon.ReportCrash(ex, CritiMon.CrashSeverity.Medium, "my_key", "my_value");
    //Send a more complex JSON object for extra debug information
    JSONObject jsonObject = new JsonObject();
    jsonObject.put("Item 1", "Value 1");
    jsonObject.put("Item 2", "Value 2");
    CritiMon.ReportCrash(ex, CritiMon.CrashSeverity.Medium, jsonObject);
}
```

The following values are supported to be used for the second parameter to the `CritiMon.CrashSeverity`:
* Low
* Medium
* Major
* Critical

If you send anything other than the above, you will get an error response
back from CritiMon. 

Sign up for a free account by visiting https://critimon.boardiesitsolutions.com

CritiMon - Copyright &copy; 2019 - Boardies IT Solutions

<img src="https://boardiesitsolutions.com/images/logo.png"> 
