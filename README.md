[ ![Download](https://api.bintray.com/packages/jcharlesworthuk/maven/jsontextview/images/download.svg) ](https://bintray.com/jcharlesworthuk/maven/jsontextview/_latestVersion)

# JsonTextView
Android view for displaying JSON strings in a nice readable way.


## Gradle integration

You can include the view in your project by adding the following line to your `build.gradle`

```gradle
dependencies {
  compile 'com.jcharlesworth:jsontextview:1.0.1'
}
```

Make sure you are using jcenter for you gradle plugins...


```gradle
buildscript {
    repositories {
        jcenter()
    }
```

## Usage

Add the view to your layout XML, in this example it is also inside a <ScrollView> which helps when you have a large amount of JSON to display


```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:app="http://schemas.android.com/apk/res-auto" android:layout_width="match_parent">
    
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    <com.jcharlesworth.jsontextview.JsonTextView
        android:id="@+id/json_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:minLines="6"
        android:background="@android:color/background_light"
        app:propertyNameColor="@color/colorAccent"
        app:propertyValueColor="@color/colorPrimary"
        app:enquoteStrings="true"
        app:enquotePropertyNames="true"
        app:textSize="16sp"
        />
    </ScrollView>

</RelativeLayout>
```

Then you can set the JSON text from a string in your activity

```java
	String testJson = "{ 'property': 'value' }";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		JsonTextView jsonText = (JsonTextView) findViewById(R.id.json_text);
		jsonText.setJson(testJson);

		...
	}
```

## Attribute Reference

The following attributes are defined for the `JsonTextView` view, they are all optional.

|   Attribute   |   Type   | Default | Description   |
|---------------|----------| --------|---------------|
| propertyNameColor	| colour	| Black | The colour of the property names in a JSON object |
| propertyValueColor	| colour	| Black | The colour of the property values in a JSON object |
| enquoteStrings	| boolean	| true | Displays the quotes around all strings, always displayed as double quotes ("") regardless of the quoting style in the source string |
| enquotePropertyNames	| boolean	| false | Displays the double quotes to property names.  This is false by default for added clarity |
| textSize	| colour	| Black | The base text colour |
