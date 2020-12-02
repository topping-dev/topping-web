function platformDetect(){
    // This script sets OSName variable as follows:
    // "Windows"    for all versions of Windows
    // "MacOS"      for all versions of Macintosh OS
    // "Linux"      for all versions of Linux
    // "UNIX"       for all other UNIX flavors 
    // "Unknown OS" indicates failure to detect the OS

    var OSName="Unknown";
    if (navigator.appVersion.indexOf("Win")!=-1) OSName="Windows";
    if (navigator.appVersion.indexOf("Mac")!=-1) OSName="MacOS";
    if (navigator.appVersion.indexOf("X11")!=-1) OSName="UNIX";
    if (navigator.appVersion.indexOf("Linux")!=-1) OSName="Linux";

    return OSName;
}

function BrowserInfo(){
    this.name = navigator.appName;
    this.codename = navigator.appCodeName;
    this.version = navigator.appVersion.substring(0, 4);
    this.platform = navigator.platform;
    this.javaEnabled = navigator.javaEnabled();
    this.screenWidth = screen.width;
    this.screenHeight = screen.height;
    this.href = window.location.href;
    this.os = platformDetect();
}