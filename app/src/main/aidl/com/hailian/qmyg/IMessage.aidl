// IMessage.aidl
package com.hailian.qmyg;

// Declare any non-default types here with import statements

interface IMessage {

    List<String> getMessage(String name, long oldTime, long currentTime);
}
