# sunmi-printer-java
java class for printing to sunmi inner printer on sunmi v2 

I created this one because i didnt find any good code for the sunmi inner printer and needed a native printing service for a app i made.


You need to add this into your build.gradle `(module :app)` under dependencies

`implementation 'com.sunmi:printerlibrary:1.0.14'`

Also edit your manifest and add this under application or right below the main activity just remember to change the package name into your apps package name.

```
<service  
    android:name=".PrinterService"  
    android:enabled="true"  
    android:exported="true">  
    <intent-filter>  
        <action android:name="se.munker.yourPackageName.PRINT_TEXT" />  
        <action android:name="se.munker.yourPackageName.PRINT_IMAGE" />  
    </intent-filter>  
</service>
```

Then in your code where you want to call the printer from you can send a intent like this, this one makes so you can do print() and add base64 inside and it will print that base64 image

```
public void print(String base64Data) {  
    Intent intent = new Intent();  
    intent.setAction("se.munker.yourPackageName.PRINT_IMAGE");  
    intent.setClassName("se.munker.yourPackageName", "se.munker.yourPackageName.PrinterService");  
    intent.putExtra("PRINT_IMAGE", base64Data);  
    startService(intent);  
    Log.d("PrintIntent", "Printing with base64 image data received.");  
}
```

but if you want to print text you can do something like this 
```
public void printText(String textData) {
    Intent intent = new Intent();
    intent.setAction("se.munker.yourPackageName.PRINT_TEXT");
    intent.setClassName("se.munker.yourPackageName", "se.munker.yourPackageName.PrinterService");
    intent.putExtra("PRINT_TEXT", textData);
    startService(intent);
    Log.d("PrintIntent", "Printing with text data received.");
}

```

**Known Bugs**
- Wont print for the first time needs to be sent twice or wait before sending the printing
