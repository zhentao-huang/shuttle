Shuttle is an Android application.
It's based on i-Jettty, zxing and net.sf.webdav-servlet.
You can build it with Maven 3.0 and above, Android SDK level 8 and above.
You may encounter build failure. i.e. aapt tool can't be found, just make a softlink
to where it requests. The build environment is easy to be satisfied with a little
modification. just trust me and yourself.

Shuttle essentially is an http server host two servlets, one for sharing application
and the other to sharing files.

You may find applications or files with a QR code. just let other devices scan the
QR. Compare to other share tools,  
 * It is not internet based. but only works on local wifi, even hot spot on mobile devices.
 * It is not require receiver to install Shuttle. and QR scanner could be it's client. 
 Recommendation is "Barcode Scanner". if your client don't have a QR scanner. web browser 
 is OK, just input the url list on the sharing UI.
 * It is able to share itself to others.

Any comments are welcome. just send me a mail
 zhentao_huang#hotmail.com
please replace # with @

