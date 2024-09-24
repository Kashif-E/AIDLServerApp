//IMessageListener.aidl
package com.kashif.aidleventcommunicator.communication;

import com.kashif.aidleventcommunicator.message.IMessage;

interface IMessageListener {
    void onMessageReceived(in IMessage message);
}
